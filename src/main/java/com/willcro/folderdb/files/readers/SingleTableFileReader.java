package com.willcro.folderdb.files.readers;

import com.willcro.folderdb.config.FileConfiguration;
import com.willcro.folderdb.exception.FolderDbException;
import com.willcro.folderdb.exception.TableNotFoundException;
import com.willcro.folderdb.sql.TableV2;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public abstract class SingleTableFileReader extends BaseReader {

  @Override
  public String getId() {
    return null;
  }

  protected abstract TableV2 readSingleTableFromFile(File file, FileConfiguration config) throws FolderDbException;

  protected abstract Stream<List<String>> getData(File file, FileConfiguration configuration) throws FolderDbException;

  @Override
  public List<TableV2> readFile(File file, FileConfiguration config) throws FolderDbException {
    return Collections.singletonList(readSingleTableFromFile(file, config));
  }

  @Override
  public Stream<List<String>> getData(File file, TableV2 table, FileConfiguration configuration)
      throws FolderDbException {
    // since this type of reader only supports files with a single table, we will never have a
    // table with a subName
    if (table.getSubName() != null) {
      throw new TableNotFoundException(table.getSubName());
    }

    return getData(file, configuration);
  }
}
