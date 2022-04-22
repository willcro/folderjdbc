package com.willcro.folderdb.files;

import com.willcro.folderdb.sql.Table;

import java.io.File;
import java.io.FileNotFoundException;

public interface FileTableReader {

    String getId();

    Table readFile(File file) throws FileNotFoundException;

}
