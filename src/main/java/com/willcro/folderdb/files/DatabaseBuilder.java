package com.willcro.folderdb.files;

import com.willcro.folderdb.config.ConfigReader;
import com.willcro.folderdb.config.FileConfiguration;
import com.willcro.folderdb.config.GlobalConfig;
import com.willcro.folderdb.exception.ConfigurationException;
import com.willcro.folderdb.files.readers.ReaderFactory;
import com.willcro.folderdb.sql.Table;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.sqlite.JDBC;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.nio.file.StandardWatchEventKinds.*;

@Slf4j
public class DatabaseBuilder {

    private Map<String, Table> tables = new HashMap<>();

    private final String directoryPath;
    private final Connection connection;
    private final File[] files;
    private final GlobalConfig config;
    private final ReaderFactory factory;
    private Thread watcherThread = new Thread(this::watchFiles);

    public DatabaseBuilder(String directoryPath) throws SQLException {
        this.directoryPath = directoryPath;
        log.info("Starting database build");
        var dir = Path.of(directoryPath).toFile();

        if (dir == null || !dir.exists()) {
            throw new RuntimeException("Directory does not exist");
        }

        this.files = dir.listFiles();
        if (!dir.isDirectory() || files == null) {
            throw new RuntimeException(directoryPath + " is not a directory");
        }

        this.config = ConfigReader.readConfigFile(dir.toPath());
        this.factory = new ReaderFactory(ConfigReader.readConfigFile(dir.toPath()));

        this.connection = createDatabase();
        createSchema(connection);
        createFileWatcher();

    }

    /**
     * Build SQLite base and populate with contents of this file
     * @return path to sqlite database
     */
    public Connection build() throws SQLException {

        // slightly increase performance, but I am not sure it is worth it
//        connection.prepareStatement( "PRAGMA synchronous = OFF").execute();
//        connection.prepareStatement( "PRAGMA journal_mode = MEMORY").execute();

        var fileList = Arrays.asList(files);
        fileList.forEach(f -> loadDatabaseWithFile(connection, f, factory, config));

        log.info("Finished database build");
        return connection;
    }

    private Connection createDatabase() throws SQLException {
        var path = createDotFolder().resolve("folderdb.db");
        var jdbcUrl = "jdbc:sqlite:" + path.toString();
        var driver = new JDBC();
        return driver.connect(jdbcUrl, new Properties());
    }

    private Path createDotFolder() {
        var path = Path.of(directoryPath, ".folderdb");
        path.toFile().mkdirs();
        return path;
    }

    private void loadDatabaseWithFile(Connection connection, File file, ReaderFactory readerFactory, GlobalConfig config) {
        var startTime = System.currentTimeMillis();
        log.info("Starting processing of {}", file.getName());
        var q = new QueryRunner();
        var shouldCreateTable = true;
        try {
            var hash = FileUtils.createSha1(file);
            var fileHash = q.query(connection, "SELECT * FROM \"_folderdb_files\" WHERE filename = ?", new MapHandler(), file.getName());
            if (fileHash != null) {
                if (fileHash.get("hash").equals(hash) && fileHash.get("loaded_data").equals(1)) {
                    // do nothing
                    log.info("{} was unchanged", file.getName());
                    return;
                } else if (fileHash.get("hash").equals(hash)) {
                    log.info("{} was unchanged but not yet loaded", file.getName());
                    shouldCreateTable = false;
                } else {
                    log.info("{} was changed", file.getName());
                    q.update(connection, "DROP TABLE \"" + file.getName() + "\"");
                    q.update(connection, "DELETE FROM _folderdb_files WHERE filename = ?", file.getName());
                }
            } else {
                log.info("New file {}", file.getName());
            }
            var reader = readerFactory.createReader(file);
            var tables = reader.readFile(file, config.getFiles().getOrDefault(file.getName(), new FileConfiguration()));

            for (Table table : tables) {
                if (shouldCreateTable) {
                    createTable(table.getName(), table.getColumns(), connection);
                }
                // commenting out to change load to lazy
//                insertData(table.getName(), table.getColumns(), table.getRows(), connection);
                insertHash(connection, file.getName(), hash);
                this.tables.put(table.getName(), table);
            }
        } catch (SQLException | ConfigurationException e) {
            // todo
            e.printStackTrace();
        }
        var endTime = System.currentTimeMillis();
        log.info("Completed processing of {} in {}ms", file.getName(), (endTime - startTime));
    }

    private void createTable(String name, List<String> columns, Connection connection) {
        var body = columns.stream().map(c -> c + " TEXT").collect(Collectors.joining(", "));
        var statement = "CREATE TABLE \"" + name + "\" ( " + body + " );";
        try {
            connection.prepareStatement(statement).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadTableDate(String tableName) throws SQLException {

        var table = tables.get(tableName);
        // todo: handle table doesn't exist

        if (table == null) {
            log.info("Table {} already loaded", tableName);
            return;
        }

        insertData(table.getName(), table.getColumns(), table.getRows(), connection);

        tables.remove(tableName);
    }

    private void insertData(String name, List<String> columns, Stream<List<String>> rows, Connection connection) throws SQLException {
        log.info("Starting insert into {}", name);
        var formattedColumns = columns.stream().map(col -> "\"" + col + "\"").collect(Collectors.joining(","));

        var placeholders = createValuesPlaceholder(columns.size());
        var sql = String.format("INSERT INTO \"%s\" (%s) VALUES %s", name, formattedColumns, placeholders);

        var flux = Flux.using(() -> rows.filter(Objects::nonNull), Flux::fromStream, BaseStream::close);

        connection.setAutoCommit(false);

        flux.filter(row -> row != null && row.size() == columns.size())
                .buffer(1000)
                .filter(Objects::nonNull)
                .subscribe(batch -> {
                    try {
                        String[][] rowData = batch.stream().map(row -> row.toArray(new String[]{})).collect(Collectors.toList()).toArray(new String[][]{});
                        new QueryRunner().insertBatch(connection, sql, new MapHandler(), rowData);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });

        setLoadedData(name);

        connection.commit();
        connection.setAutoCommit(true);

        log.info("Completed insert into {}", name);
    }

    /**
     * Create "(?,?)" with a variable number of ?
     * @param n number of placeholders
     * @return string
     */
    private String createValuesPlaceholder(Integer n) {
        return "(" + IntStream.range(0, n).mapToObj(it -> "?").collect(Collectors.joining(",")) + ")";
    }

    private void createSchema(Connection connection) {
        FileUtils.runQuery("/queries/create_folderdb_files.sql", connection);
    }

    private void insertHash(Connection connection, String filename, String hash) throws SQLException {
        var sql = "INSERT INTO _folderdb_files (filename,hash) VALUES (?,?)";
        new QueryRunner().insert(connection, sql, new MapHandler(), filename, hash);
    }

    private void updateHash(Connection connection, String filename, String hash) throws SQLException {
        var sql = "UPDATE _folderdb_files SET hash = ? WHERE filename = ?";
        new QueryRunner().insert(connection, sql, new MapHandler(), hash, filename);
    }

    private void setLoadedData(String filename) throws SQLException {
        var sql = "UPDATE _folderdb_files SET loaded_data = 1 WHERE filename = ?";
        new QueryRunner().insert(connection, sql, new MapHandler(), filename);
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
            e.printStackTrace();
            log.error("File watcher failed to start");
            // todo
            return;
        }
        while(true) {
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

        if (kind.equals(ENTRY_MODIFY)) {
            try {
                loadDatabaseWithFile(connection, Path.of(directoryPath).resolve((Path) event.context()).toFile(), factory, config);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
