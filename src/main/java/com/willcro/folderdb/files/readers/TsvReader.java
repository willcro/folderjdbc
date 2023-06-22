package com.willcro.folderdb.files.readers;

public class TsvReader extends DelimitedReader {

  @Override
  public String getId() {
    return "tsv";
  }

  @Override
  protected String getDefaultDelimiter() {
    return "\t";
  }

}
