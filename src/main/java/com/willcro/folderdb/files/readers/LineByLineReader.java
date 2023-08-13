package com.willcro.folderdb.files.readers;

import com.willcro.folderdb.config.FileConfiguration;
import com.willcro.folderdb.exception.ConfigurationException;
import com.willcro.folderdb.exception.FileProcessingException;
import com.willcro.folderdb.exception.FolderDbException;
import com.willcro.folderdb.sql.Table;
import com.willcro.folderdb.sql.TableV2;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Abstract reader where each line in a file corresponds to one row in the resulting table
 */
abstract public class LineByLineReader extends SingleTableFileReader {

  protected TableV2 readSingleTableFromFile(File file, FileConfiguration config) throws FolderDbException {
    return TableV2.builder()
        .columns(getColumns(file, config))
        .build();
  }

  protected Stream<List<String>> getData(File file, FileConfiguration configuration) throws FolderDbException {
    try {
      return Files.lines(file.toPath(), guessEncoding(file))
          .skip(skipLines())
          .map(line -> {
            try {
              return readLine(line, file, configuration);
            } catch (FolderDbException e) {
              // TODO
              return null;
            }
          }).filter(Objects::nonNull);
    } catch (IOException e) {
      throw new FileProcessingException(file.getName(), e);
    }
  }

  protected abstract List<String> readLine(String line, File file, FileConfiguration config) throws FolderDbException;

  protected abstract List<String> getColumns(File file, FileConfiguration config) throws FolderDbException;

  /**
   * Get how many lines to skip at the top of the file. Defaults to 1 (for the header line of a
   * CSV)
   *
   * @return number of lines to skip
   */
  protected int skipLines() {
    return 1;
  }

}
