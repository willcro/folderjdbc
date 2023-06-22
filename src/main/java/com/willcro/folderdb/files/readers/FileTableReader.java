package com.willcro.folderdb.files.readers;

import com.willcro.folderdb.config.FileConfiguration;
import com.willcro.folderdb.exception.ConfigurationException;
import com.willcro.folderdb.sql.Table;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public interface FileTableReader {

  String getId();

  /**
   * Extract tables from a file
   *
   * @param file the input file
   * @return table(s) that are produced by this file
   * @throws FileNotFoundException if the file doesn't exist
   */
  default List<Table> readFile(File file) throws ConfigurationException {
    return readFile(file, new FileConfiguration());
  }

  List<Table> readFile(File file, FileConfiguration config) throws ConfigurationException;

}
