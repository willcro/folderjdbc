package com.willcro.folderdb.files.readers;

import com.willcro.folderdb.config.FileConfiguration;
import java.io.File;
import java.util.Collections;
import java.util.List;

public class LineReader extends LineByLineReader {

  @Override
  public String getId() {
    return "line";
  }

  @Override
  protected List<String> readLine(String line, File file, FileConfiguration config) {
    return Collections.singletonList(line);
  }

  @Override
  protected List<String> getColumns(File file, FileConfiguration config) {
    return Collections.singletonList("line");
  }

  @Override
  protected int skipLines() {
    // don't skip the first line
    return 0;
  }
}
