package com.willcro.folderdb.files.readers;

public class PsvReader extends CsvReader {

  @Override
  public String getId() {
    return "psv";
  }

  @Override
  protected Character getDefaultDelimiter() {
    return '|';
  }
}
