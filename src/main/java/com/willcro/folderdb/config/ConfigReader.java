package com.willcro.folderdb.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class ConfigReader {

    public static GlobalConfig readConfigFile(Path projectDir) {
        var path = projectDir.resolve(".folderdb/config.json");
        if (!path.toFile().exists()) {
            return new GlobalConfig();
        }

        var om = new ObjectMapper();

        try {
            return om.reader().readValue(path.toFile(), GlobalConfig.class);
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
            return new GlobalConfig();
        }
    }
// TODO
//    public static FileConfiguration getConfigForFile(File file) {
//
//    }

}
