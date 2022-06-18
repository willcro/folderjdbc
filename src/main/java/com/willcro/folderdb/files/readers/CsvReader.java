package com.willcro.folderdb.files.readers;

/**
 * Reader for .csv files
 */
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
