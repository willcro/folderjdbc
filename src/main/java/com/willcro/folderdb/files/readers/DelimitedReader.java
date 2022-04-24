package com.willcro.folderdb.files.readers;

import com.willcro.folderdb.config.FileConfiguration;
import com.willcro.folderdb.exception.ConfigurationException;
import com.willcro.folderdb.sql.Table;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class DelimitedReader extends BaseReader {

    @Override
    public List<Table> readFile(File file, FileConfiguration configuration) throws ConfigurationException {
        var delimiter = configuration.getCsvDelimiter() == null ? getDefaultDelimiter() : configuration.getCsvDelimiter();

        List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(delimiter);
                records.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            //todo
            e.printStackTrace();
        }

        // todo: handle less than one row
        var columns = records.get(0);
        var rows = records.subList(1, records.size());

        var table = Table.builder()
                .name(file.getName())
                .columns(columns)
                .rows(rows.stream())
                .build();

        return Collections.singletonList(table);
    }

    protected abstract String getDefaultDelimiter();

}
