package com.willcro.folderdb.config;

import lombok.Data;

import java.util.Collections;
import java.util.Map;

@Data
public class GlobalConfig {

    private Map<String, FileConfiguration> files = Collections.emptyMap();

    /**
     * Method for determining if a file has changed. (Default: hash)
     * hash: Uses the SHA256 hash of the file. Best for accuracy.
     * timestamp: Uses the file's update timestamp as reported by the OS. Better for performance.
     * hybrid: Uses timestamp for large files and hash for smaller (TODO)
     */
    private String updateChecker = "hash";

    private FileConfiguration defaults = new FileConfiguration();

}
