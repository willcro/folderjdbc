package com.willcro.folderdb.files.readers;

import com.willcro.folderdb.config.FileConfiguration;
import com.willcro.folderdb.exception.FolderDbException;
import com.willcro.folderdb.exception.TableNotFoundException;
import com.willcro.folderdb.sql.TableV2;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class NoOpReader implements FileTableReader {

  @Override
  public String getId() {
    return "noop";
  }

  @Override
  public List<TableV2> readFile(File file, FileConfiguration config) throws FolderDbException {
    throw new FolderDbException("No reader registered for this file");
  }

  @Override
  public Stream<List<String>> getData(File file, TableV2 table, FileConfiguration configuration) throws FolderDbException {
    throw new TableNotFoundException(table.getSubName());
  }

}
