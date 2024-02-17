package com.willcro.folderdb.files;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import com.willcro.folderdb.config.ConfigReader;
import com.willcro.folderdb.config.FileConfiguration;
import com.willcro.folderdb.config.GlobalConfig;
import com.willcro.folderdb.dao.FolderDbDao;
import com.willcro.folderdb.entity.FolderDbTable;
import com.willcro.folderdb.exception.FolderDbException;
import com.willcro.folderdb.files.readers.ReaderFactory;
import com.willcro.folderdb.service.update.HashUpdateChecker;
import com.willcro.folderdb.service.update.HybridUpdateChecker;
import com.willcro.folderdb.service.update.TimestampUpdateChecker;
import com.willcro.folderdb.service.update.UpdateChecker;
import com.willcro.folderdb.sql.TableV2;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.lang3.StringUtils;
import org.flywaydb.core.Flyway;
import org.sqlite.SQLiteDataSource;
import reactor.core.publisher.Flux;

@Slf4j
public class DatabaseBuilder implements Closeable {

  private final String directoryPath;
  private final Connection connection;
  private final File[] files;
  private final GlobalConfig config;
  private final ReaderFactory factory;
  private final FolderDbDao folderDbDao;
  private final UpdateChecker updateChecker;
  private final Thread watcherThread = new Thread(this::watchFiles);
  private Boolean watching = true;

  public DatabaseBuilder(String directoryPath) throws SQLException {
    this.directoryPath = directoryPath;
    log.info("Starting database build");
    var dir = Path.of(directoryPath).toFile();

    if (!dir.exists()) {
      throw new RuntimeException("Directory does not exist");
    }

    this.files = dir.listFiles();
    if (!dir.isDirectory() || files == null) {
      throw new RuntimeException(directoryPath + " is not a directory");
    }

    this.config = ConfigReader.readConfigFile(dir.toPath());
    this.factory = new ReaderFactory(ConfigReader.readConfigFile(dir.toPath()));

    var dataSource = createDatabase();
    this.connection = dataSource.getConnection();
    this.folderDbDao = new FolderDbDao(this.connection);
    createSchema(dataSource);
    createFileWatcher();

    if ("hash".equalsIgnoreCase(this.config.getUpdateChecker())) {
      this.updateChecker = new HashUpdateChecker();
    } else if ("timestamp".equalsIgnoreCase(this.config.getUpdateChecker())) {
      this.updateChecker = new TimestampUpdateChecker();
    } else if ("hybrid".equalsIgnoreCase(this.config.getUpdateChecker())) {
      this.updateChecker = new HybridUpdateChecker();
    } else {
      throw new RuntimeException(
          "Unrecognized updateChecker '" + this.config.getUpdateChecker() + "'");
    }
  }

  /**
   * Build SQLite base and populate with contents of this file
   *
   * @return path to sqlite database
   */
  public Connection build() throws SQLException {

    // slightly increase performance, but I am not sure it is worth it
//        connection.prepareStatement( "PRAGMA synchronous = OFF").execute();
//        connection.prepareStatement( "PRAGMA journal_mode = MEMORY").execute();

    var fileList = Arrays.asList(files);
    fileList.forEach(this::loadDatabaseWithFile);

    log.info("Finished database build");
    return connection;
  }

  private File getFile(String fileName) {
    return Path.of(directoryPath, fileName).toFile();
  }

  private DataSource createDatabase() throws SQLException {
    var path = createDotFolder().resolve("folderdb.db");
    var jdbcUrl = "jdbc:sqlite:" + path.toString();
    var datasource = new SQLiteDataSource();
    datasource.setUrl(jdbcUrl);
    return datasource;
  }

  private Path createDotFolder() {
    var path = Path.of(directoryPath, ".folderdb");
    path.toFile().mkdirs();
    return path;
  }

  private void loadDatabaseWithFile(File file) {
    var startTime = System.currentTimeMillis();
    log.info("Starting processing of {}", file.getName());
    try {
      folderDbDao.insertFile(file.getName());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    try {
      var dbFileOp = folderDbDao.getFile(file.getName());

      if (dbFileOp.isPresent()) {
        var dbFile = dbFileOp.get();
        var currentState = updateChecker.getState(file);
        var dbTables = folderDbDao.getTablesForFile(file.getName());
        var allLoaded = dbTables.stream().allMatch(FolderDbTable::getLoadedData);

        if (updateChecker.isUpdated(dbFile.getUpdateState(), currentState)) {
          log.info("{} was changed", file.getName());
          folderDbDao.deleteFile(file.getName());
          folderDbDao.insertFile(file.getName());
          folderDbDao.updateState(file.getName(), currentState);
        } else {
          if (allLoaded) {
            log.info("{} was unchanged and already loaded", file.getName());
            return;
          }
          log.info("{} was unchanged but not loaded", file.getName());
        }
      } else {
        var currentState = updateChecker.getState(file);
        log.info("New file {}", file.getName());
        folderDbDao.updateState(file.getName(), currentState);
      }
      var reader = factory.createReader(file);
      var tables = reader.readFile(file,
          config.getFiles().getOrDefault(file.getName(), new FileConfiguration()));

      var usedNames = new HashSet<String>();
      for (TableV2 table : tables) {
        var fullName = getFullName(file.getName(), table.getSubName(), usedNames);
        usedNames.add(fullName);
        var dbTable = folderDbDao.getTable(fullName);
        if (dbTable.isEmpty()) {
          var dedupedColumns = dedupeColumns(table.getColumns());
          createTable(fullName, dedupedColumns);
          folderDbDao.insertTable(file.getName(), fullName, table.getSubName(), table.getColumns(), dedupedColumns, table.getMetadata());
        }
      }
    } catch (Exception e) {
      try {
        log.error("Error occurred while processing {}", file.getName(), e);
        folderDbDao.saveErrorForFile(file.getName(), e);
      } catch (SQLException ex) {
        log.error("Saving the error for {} failed", file.getName(), ex);
      }
    }
    var endTime = System.currentTimeMillis();
    log.info("Completed processing of {} in {}ms", file.getName(), (endTime - startTime));
  }

  private String getFullName(String fileName, String subName, Set<String> usedNames) {
    var desiredName = subName == null ? fileName : fileName + "/" + subName;

    var actualName = desiredName;
    var index = 1;

    while (usedNames.contains(actualName)) {
      actualName = String.format("%s (%d)", desiredName, index);
      index++;
    }

    return actualName;
  }

  private void createTable(String name, List<String> columns) throws FolderDbException {
    log.info("Creating table {}", name);
    var body = columns.stream().map(c -> "\"" + c + "\" TEXT COLLATE NOCASE").collect(Collectors.joining(", "));
    var statement = "CREATE TABLE \"" + name + "\" ( " + body + " );";
    try {
      connection.prepareStatement(statement).execute();
    } catch (SQLException e) {
      throw new FolderDbException("Error occurred while processing file", e);
    }
  }

  private List<String> dedupeColumns(List<String> columns) {
    var usedColumns = new HashSet<>();
    var out = new ArrayList<String>(columns.size());

    for (String desiredName : columns) {
      var actualName = desiredName;
      var index = 1;
      while (usedColumns.contains(actualName)) {
        actualName = String.format("%s (%d)", desiredName, index);
        index++;
      }
      usedColumns.add(actualName);
      out.add(actualName);
    }

    return out;
  }

  private static String getFilenameFromTableName(String tableName) {
    if (tableName.contains("/")) {
      return tableName.substring(0, tableName.lastIndexOf('/'));
    } else {
      return tableName;
    }
  }

  public void handleTableNotFound(String tableName) throws SQLException {
    var fileName = getFilenameFromTableName(tableName);
    var fileOp = folderDbDao.getFile(fileName);
    if (fileOp.isEmpty()) {
      log.debug("File {} doesn't exist. Letting SQLite handle this", tableName);
      return;
    }

    var file = fileOp.get();
    if (StringUtils.isNotBlank(file.getError())) {
      throw new SQLException(file.getError());
    }

    // file is known and has no errors, but this table isn't found
    throw new SQLException("Table " + tableName + " doesn't exist within " + fileName);
  }

  public void loadTableData(String tableName) throws SQLException, FolderDbException {
    var dbTable = folderDbDao.getTable(tableName);

    if (dbTable.isEmpty()) {
      handleTableNotFound(tableName);
      return;
    }

    if (dbTable.get().getLoadedData()) {
      log.info("Table {} already loaded", tableName);
      return;
    }

    var table = dbTable.get();
    var file = getFile(table.getFilename());
    var tableV2 = TableV2.builder().columns(table.getColumnsAsList()).subName(table.getSubName()).metadata(table.getMetadataAsMap()).build();
    var rows = factory.createReader(file).getData(file, tableV2, config.getFileConfig(file.getName()));

    insertData(table.getTableName(), table.getStorageColumnsAsList(), rows);
  }

  private void insertData(String name, List<String> columns, Stream<List<String>> rows) throws SQLException {
    log.info("Starting insert into {}", name);
    var formattedColumns = columns.stream().map(col -> "\"" + col + "\"")
        .collect(Collectors.joining(","));

    var placeholders = createValuesPlaceholder(columns.size());
    var sql = String.format("INSERT INTO \"%s\" (%s) VALUES %s", name, formattedColumns,
        placeholders);

    var flux = Flux.using(() -> rows.filter(Objects::nonNull), Flux::fromStream, BaseStream::close);

    connection.setAutoCommit(false);

    try {
      flux.filter(row -> row != null && row.size() == columns.size())
          .buffer(1000)
          .filter(Objects::nonNull)
          .doOnNext(batch -> {
            try {
              String[][] rowData = batch.stream().map(row -> row.toArray(new String[]{}))
                  .collect(Collectors.toList()).toArray(new String[][]{});
              new QueryRunner().insertBatch(connection, sql, new MapHandler(), rowData);
            } catch (SQLException e) {
              e.printStackTrace();
            }
          }).blockLast();
      connection.commit();
    } catch (Exception e) {
      connection.rollback();
      throw e;
    } finally {
      connection.setAutoCommit(true);
    }

    folderDbDao.setLoadedData(name);
    log.info("Completed insert into {}", name);
  }

  /**
   * Create "(?,?)" with a variable number of ?
   *
   * @param n number of placeholders
   * @return string
   */
  private String createValuesPlaceholder(Integer n) {
    return "(" + IntStream.range(0, n).mapToObj(it -> "?").collect(Collectors.joining(",")) + ")";
  }

  private void createSchema(DataSource dataSource) {
    var flyway = new Flyway(Flyway.configure().dataSource(dataSource));
    flyway.migrate();
  }

  private void createFileWatcher() {
    this.watcherThread.start();
  }

  private void watchFiles() {
    log.info("Running file watcher");
    var path = Path.of(directoryPath);
    WatchKey key = null;
    WatchService watcher;
    try {
      watcher = FileSystems.getDefault().newWatchService();
      key = path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    } catch (IOException e) {
      log.error("File watcher failed to start", e);
      // todo
      return;
    }
    while (watching) {
      try {
        var events = watcher.take().pollEvents();
        events.forEach(this::handleEvent);
        key.reset();
      } catch (InterruptedException ex) {
        // todo
        ex.printStackTrace();
      }
    }
  }

  private void handleEvent(WatchEvent<?> event) {
    if (event.kind() == OVERFLOW) {
      return;
    }

    WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
    var filename = pathEvent.context().toFile().getName();
    var kind = pathEvent.kind();

    if (filename.equals(".folderdb")) {
      return;
    }

    log.info("{} was {}", filename, kind.name());

    if (kind.equals(ENTRY_MODIFY) || kind.equals(ENTRY_CREATE)) {
      try {
        loadDatabaseWithFile(Path.of(directoryPath).resolve((Path) event.context()).toFile());
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else if (kind.equals(ENTRY_DELETE)) {
      try {
        folderDbDao.deleteFile(Path.of(directoryPath).resolve((Path) event.context()).toFile().getName());
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void close() throws IOException {
    try {
      connection.close();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    watching = false;

  }
}
