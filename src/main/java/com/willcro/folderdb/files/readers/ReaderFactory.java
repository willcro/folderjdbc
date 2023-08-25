package com.willcro.folderdb.files.readers;

import com.willcro.folderdb.config.FileConfiguration;
import com.willcro.folderdb.config.GlobalConfig;
import java.io.File;

public class ReaderFactory {

  private final GlobalConfig globalConfig;

  public ReaderFactory(GlobalConfig globalConfig) {
    this.globalConfig = globalConfig;
  }

  /**
   * Determine reader for this file
   *
   * @param file the file to be read
   * @return the appropriate reader
   */
  public FileTableReader createReader(File file) {
    // todo: handle file with no extension
    var extension = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
    var fileConfig = globalConfig.getFiles().getOrDefault(file.getName(), new FileConfiguration());

    if (fileConfig.getReader() != null) {
      return ReaderRegistry.getById(fileConfig.getReader());
    }

    switch (extension) {
      case "csv":
        return new CsvReader();
      case "psv":
        return new PsvReader();
      case "tsv":
        return new TsvReader();
      case "csvnh":
        return new CsvReader(false);
      case "psvnh":
        return new PsvReader(false);
      case "tsvnh":
        return new TsvReader(false);
      case "json":
      case "jsonc":
        return new JsonReader();
      case "xlsx":
        return new ExcelReader();
      case "txt":
        return new LineReader();
      case "yml":
      case "yaml":
        return new YamlReader();
      case "xml":
      case "html":
        return new XmlReader();
      default:
        return new NoOpReader(); // todo: is this how I want to do this?
    }
  }

}
