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

public class LineReader extends LineByLineReader {

    @Override
    public String getId() {
        return "line";
    }

    @Override
    protected List<String> readLine(String line, File file, FileConfiguration config) {
        return Collections.singletonList(line);
    }

    @Override
    protected List<String> getColumns(File file, FileConfiguration config) {
        return Collections.singletonList("line");
    }
}
