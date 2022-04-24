package com.willcro.folderdb.files.readers;

import com.willcro.folderdb.config.FileConfiguration;

public class CsvReader extends DelimitedReader {

    @Override
    public String getId() {
        return "csv";
    }

    @Override
    protected String getDefaultDelimiter() {
        return ",";
    }
}
