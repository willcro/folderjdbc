package com.willcro.folderdb.files.readers;

public class TsvReader extends CsvReader {

  @Override
  public String getId() {
    return "tsv";
  }

  @Override
  protected Character getDefaultDelimiter() {
    return '\t';
  }

}
