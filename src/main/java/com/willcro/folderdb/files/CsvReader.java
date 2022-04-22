package com.willcro.folderdb.files;

import com.willcro.folderdb.sql.Table;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CsvReader implements FileTableReader {

    @Override
    public String getId() {
        return "csv";
    }

    @Override
    public Table readFile(File file) throws FileNotFoundException {
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                records.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            //todo
            e.printStackTrace();
        }

        // todo: handle less than one row
        var columns = records.get(0);
        var rows = records.subList(1, records.size());
        return new Table(columns, rows);
    }

}
