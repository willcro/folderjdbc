package com.willcro.folderdb.exception;

public class TableNotFoundException extends FolderDbException {

  public TableNotFoundException(String table) {
    super("Table " + table + " doesn't exist");
  }
}
