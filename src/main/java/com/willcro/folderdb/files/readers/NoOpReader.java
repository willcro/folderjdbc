package com.willcro.folderdb.files.readers;

import com.willcro.folderdb.config.FileConfiguration;
import com.willcro.folderdb.exception.ConfigurationException;
import com.willcro.folderdb.sql.Table;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class NoOpReader implements FileTableReader {

    @Override
    public String getId() {
        return "noop";
    }

    @Override
    public List<Table> readFile(File file, FileConfiguration config) throws ConfigurationException {
        return Collections.emptyList();
    }

}
