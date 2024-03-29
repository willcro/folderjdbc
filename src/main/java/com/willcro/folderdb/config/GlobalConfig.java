package com.willcro.folderdb.config;

import java.util.Collections;
import java.util.Map;
import lombok.Data;

@Data
public class GlobalConfig {

  private Map<String, FileConfiguration> files = Collections.emptyMap();

  /**
   * Method for determining if a file has changed. (Default: hybrid)
   * hash: Uses the SHA256 hash of the file. Best for accuracy.
   * timestamp: Uses the file's update timestamp as reported by the OS. Better for performance.
   * hybrid: Uses timestamp for large files and hash for smaller
   */
  private String updateChecker = "hybrid";

  private FileConfiguration defaults = new FileConfiguration();

  public FileConfiguration getFileConfig(String fileName) {
    return files.getOrDefault(fileName, defaults);
  }

}
