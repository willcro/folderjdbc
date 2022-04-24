package com.willcro.folderdb.files.readers;

import com.willcro.folderdb.config.FileConfiguration;
import com.willcro.folderdb.exception.ConfigurationException;
import com.willcro.folderdb.sql.Table;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LineReader extends BaseReader {

    @Override
    public String getId() {
        return "line";
    }

    @Override
    public List<Table> readFile(File file, FileConfiguration config) throws ConfigurationException {
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                records.add(Collections.singletonList(line));
            }
        } catch (IOException e) {
            //todo
            e.printStackTrace();
        }

        var columns = Collections.singletonList("line");

        var table = Table.builder()
                .name(file.getName())
                .columns(columns)
                .rows(records.stream())
                .build();

        return Collections.singletonList(table);
    }
}
