package com.willcro.folderdb.files.readers;

import com.willcro.folderdb.config.FileConfiguration;
import com.willcro.folderdb.exception.FolderDbException;
import com.willcro.folderdb.sql.TableV2;
import java.io.File;
import java.util.List;
import java.util.stream.Stream;

public interface FileTableReader {

  String getId();

  List<TableV2> readFile(File file, FileConfiguration config) throws FolderDbException;

  Stream<List<String>> getData(File file, TableV2 table, FileConfiguration configuration) throws FolderDbException;

}
