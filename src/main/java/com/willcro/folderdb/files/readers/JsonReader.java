package com.willcro.folderdb.files.readers;

import com.willcro.folderdb.config.FileConfiguration;
import com.willcro.folderdb.exception.ConfigurationException;
import com.willcro.folderdb.sql.Table;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class JsonReader extends BaseReader {

    @Override
    public String getId() {
        return "json";
    }

    @Override
    public List<Table> readFile(File file, FileConfiguration config) throws ConfigurationException {
        // todo
        return Collections.emptyList();
    }

}
