package com.willcro.folderdb.tester;

import com.willcro.folderdb.jdbc.FolderDbDriver;
import org.sqlite.jdbc4.JDBC4ResultSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Main class used for testing
 */
public class SqlTester {

    public static void main(String[] args) throws SQLException, IOException {
        var connection = new FolderDbDriver().connect("jdbc:folderdb:C:\\Users\\Will\\Documents\\folderdbtest", new Properties());

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
                ex.printStackTrace();
            }
        }
    }

}