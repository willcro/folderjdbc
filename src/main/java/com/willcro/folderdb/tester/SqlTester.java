package com.willcro.folderdb.tester;

import com.willcro.folderdb.jdbc.FolderDbDriver;
import lombok.extern.slf4j.Slf4j;
import org.sqlite.jdbc4.JDBC4ResultSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Main class used for testing
 */
@Slf4j
public class SqlTester {

    public static void main(String[] args) throws SQLException, IOException {
        deleteDirectory(Path.of("C:\\Users\\Will\\Documents\\folderdbtest\\testfiles\\.folderdb"));
        var connection = new FolderDbDriver().connect("jdbc:folderdb:C:\\Users\\Will\\Documents\\folderdbtest\\testfiles", new Properties());
        System.out.println("SQL query:");

        while (true) {
            // Enter data using BufferReader
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in));

            // Reading data using readLine
            String query = reader.readLine();

            // Printing the read line
            try {
                var results = (JDBC4ResultSet) connection.prepareStatement(query).executeQuery();
                System.out.println(Arrays.stream(results.cols).collect(Collectors.joining("|")));
                ResultSetMetaData metadata = results.getMetaData();
                int columnCount = metadata.getColumnCount();

                while (results.next()) {
                    StringJoiner row = new StringJoiner("|");
                    for (int i = 1; i <= columnCount; i++) {
                        row.add(results.getString(i));
                    }
                    System.out.println(row.toString());

                }
                var test = 1;
            } catch (Exception ex) {
               log.error("Exception occurred", ex);
            }
        }
    }

    static void deleteDirectory(Path pathToBeDeleted) {
        try {
            Files.walk(pathToBeDeleted)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException ex) {
            // ignore
        }
    }

}
