package com.willcro.folderdb.files.readers;

import com.willcro.folderdb.config.FileConfiguration;

public class PsvReader extends DelimitedReader {

    @Override
    public String getId() {
        return "psv";
    }

    @Override
    protected String getDefaultDelimiter() {
        return "\\|";
    }
}
