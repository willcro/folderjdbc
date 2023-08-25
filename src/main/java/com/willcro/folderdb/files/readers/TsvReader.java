package com.willcro.folderdb.files.readers;

public class TsvReader extends CsvReader {

  public TsvReader() {
    super();
  }

  public TsvReader(Boolean hasHeaders) {
    super(hasHeaders);
  }

  @Override
  public String getId() {
    return "tsv";
  }

  @Override
  protected Character getDefaultDelimiter() {
    return '\t';
  }

}
