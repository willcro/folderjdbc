package com.willcro.folderdb.files;

import org.sqlite.JDBC;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class DatabaseBuilder {

    private final String directoryPath;

    public static void main(String[] args) throws SQLException {
        new DatabaseBuilder("C:\\Users\\Will\\Documents\\folderdbtest").build();
    }

    public DatabaseBuilder(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    /**
     * Build SQLite base and populate with contents of this file
     * @return path to sqlite database
     */
    public Connection build() throws SQLException {
        var connection = createDatabase();
        var files = Arrays.asList(Path.of(directoryPath).toFile().listFiles());
        files.stream()
                .filter(f -> f.getName().contains("."))
                .filter(f -> f.getName().substring(f.getName().lastIndexOf(".") + 1).equalsIgnoreCase("CSV"))
                .forEach(f -> loadDatabaseWithFile(connection, f));

        return connection;
    }

    private Connection createDatabase() throws SQLException {
        var path = Path.of(directoryPath, "database.db");
        var jdbcUrl = "jdbc:sqlite:" + path.toString();
        var driver = new JDBC();
        return driver.connect(jdbcUrl, new Properties());
    }

    private void loadDatabaseWithFile(Connection connection, File file) {
        try {
            var table = new CsvReader().readFile(file);
            createTable(file.getName(), table.getColumns(), connection);
            insertData(file.getName(), table.getColumns(), table.getRows(), connection);

        } catch (FileNotFoundException e) {
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

    private void insertData(String name, List<String> columns, List<List<String>> rows, Connection connection) {
        var values = rows.stream()
                .filter(row -> row.size() == columns.size())
                .map(this::rowToSql)
                .collect(Collectors.joining(","));

        var sql = "INSERT INTO \"" + name + "\" (" + columns.stream().collect(Collectors.joining(",")) + ") VALUES " + values + ";";
        try {
            connection.prepareStatement(sql).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String rowToSql(List<String> row) {
        return "(" + row.stream()
                .map(cell -> "'" + cell + "'")
                .collect(Collectors.joining(",")) + ")";
    }

}
