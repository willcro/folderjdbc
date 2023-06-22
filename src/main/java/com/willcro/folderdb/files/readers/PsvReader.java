package com.willcro.folderdb.files.readers;

public class PsvReader extends DelimitedReader {

  @Override
  public String getId() {
    return "psv";
  }

  @Override
  protected String getDefaultDelimiter() {
    return "\\|";
  }
}
