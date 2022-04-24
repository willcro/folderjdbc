package com.willcro.folderdb.files;

import com.willcro.folderdb.config.ConfigReader;
import com.willcro.folderdb.config.FileConfiguration;
import com.willcro.folderdb.config.GlobalConfig;
import com.willcro.folderdb.exception.ConfigurationException;
import com.willcro.folderdb.files.readers.ReaderFactory;
import com.willcro.folderdb.sql.Table;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.sqlite.JDBC;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DatabaseBuilder {

    private final String directoryPath;

    public DatabaseBuilder(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    /**
     * Build SQLite base and populate with contents of this file
     * @return path to sqlite database
     */
    public Connection build() throws SQLException {
        var dir = Path.of(directoryPath).toFile();

        if (dir == null || !dir.exists()) {
            throw new RuntimeException("Directory does not exist");
        }

        var files = dir.listFiles();
        if (!dir.isDirectory() || files == null) {
            throw new RuntimeException(directoryPath + " is not a directory");
        }

        var config = ConfigReader.readConfigFile(dir.toPath());
        var factory = new ReaderFactory(ConfigReader.readConfigFile(dir.toPath()));

        var connection = createDatabase();
        createSchema(connection);
        var fileList = Arrays.asList(files);
        fileList.forEach(f -> loadDatabaseWithFile(connection, f, factory, config));

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
        var q = new QueryRunner();
        try {
            var hash = FileUtils.createSha1(file);
            var fileHash = q.query(connection, "SELECT * FROM \"_folderdb_files\" WHERE filename = ?", new MapHandler(), file.getName());
            if (fileHash != null) {
                if (fileHash.get("hash").equals(hash)) {
                    // do nothing
                    System.out.println(file.getName() + " was unchanged");
                    return;
                } else {
                    System.out.println(file.getName() + " was changed");
                    q.update(connection, "DROP TABLE \"" + file.getName() + "\"");
                    q.update(connection, "DELETE FROM _folderdb_files WHERE filename = ?", file.getName());
                }
            }
            var reader = readerFactory.createReader(file);
            var tables = reader.readFile(file, config.getFiles().getOrDefault(file.getName(), new FileConfiguration()));

            for (Table table : tables) {
                createTable(table.getName(), table.getColumns(), connection);
                insertData(table.getName(), table.getColumns(), table.getRows(), connection);
                insertHash(connection, file.getName(), hash);
            }
        } catch (SQLException | ConfigurationException e) {
            // todo
            e.printStackTrace();
        }
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

    private void insertData(String name, List<String> columns, Stream<List<String>> rows, Connection connection) {
        var formattedColumns = columns.stream().map(col -> "\"" + col + "\"").collect(Collectors.joining(","));

        rows.filter(row -> row != null && row.size() == columns.size())
                .forEach(row -> {
                    var placeholders = createValuesPlaceholder(columns.size());
                    var sql = String.format("INSERT INTO \"%s\" (%s) VALUES %s", name, formattedColumns, placeholders);
                    try {
                        new QueryRunner().insert(connection, sql, new MapHandler(), row.toArray());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
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
}
