package com.willcro.folderdb.files.readers;

public class PsvReader extends CsvReader {

  public PsvReader() {
    super();
  }

  public PsvReader(Boolean hasHeaders) {
    super(hasHeaders);
  }

  @Override
  public String getId() {
    return "psv";
  }

  @Override
  protected Character getDefaultDelimiter() {
    return '|';
  }
}
