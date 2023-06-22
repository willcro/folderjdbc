package com.willcro.folderdb.config;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.nio.file.Path;

public class ConfigReader {

  public static GlobalConfig readConfigFile(Path projectDir) {
    var path = projectDir.resolve("Folderdbfile");
    if (!path.toFile().exists()) {
      return new GlobalConfig();
    }

    var om = new YAMLMapper();

    try {
      return om.reader().readValue(path.toFile(), GlobalConfig.class);
    } catch (IOException e) {
      // TODO
      e.printStackTrace();
      return new GlobalConfig();
    }
  }

}
