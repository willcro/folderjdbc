package com.willcro.folderdb.files.readers;

import com.willcro.folderdb.config.FileConfiguration;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class DelimitedReader extends LineByLineReader {

  @Override
  protected List<String> readLine(String line, File file, FileConfiguration config) {
    var delimiter =
        config.getCsvDelimiter() == null ? getDefaultDelimiter() : config.getCsvDelimiter();
    String[] values = line.split(String.valueOf(delimiter));
    return Arrays.asList(values);
  }

  @Override
  protected List<String> getColumns(File file, FileConfiguration config) {
    // todo: handle bad file
    try (var stream = Files.lines(file.toPath())) {
      return stream.map(line -> readLine(line, file, config)).findFirst().get();
    } catch (IOException e) {
      e.printStackTrace();
      return Collections.emptyList();
    }
  }

  protected abstract String getDefaultDelimiter();

}
