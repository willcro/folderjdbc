package com.willcro.folderdb.config;

import lombok.Data;

import java.util.Collections;
import java.util.Map;

@Data
public class GlobalConfig {

    private Map<String, FileConfiguration> files = Collections.emptyMap();

    private FileConfiguration defaults = new FileConfiguration();

}
