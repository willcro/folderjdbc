package com.willcro.folderdb.files.readers;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.willcro.folderdb.config.FileConfiguration;
import com.willcro.folderdb.exception.FolderDbException;
import com.willcro.folderdb.sql.TableV2;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;

/**
 * Reader for .csv files
 */
@Slf4j
public class CsvReader extends SingleTableFileReader {

  private final Boolean hasHeaders;

  public CsvReader() {
    this.hasHeaders = true;
  }

  public CsvReader(Boolean hasHeaders) {
    this.hasHeaders = hasHeaders;
  }

  @Override
  public String getId() {
    return "csv";
  }

  @Override
  protected TableV2 readSingleTableFromFile(File file, FileConfiguration config)
      throws FolderDbException {
    var hasHeaders = config.getHasHeaders() != null ? config.getHasHeaders() : getHasHeaders();

    try (var csvReader = getReader(file, config)) {
      var columns = Arrays.asList(csvReader.readNextSilently());

      if (!hasHeaders) {
        columns = getGeneratedColumns(columns.size());
      }

      return TableV2.builder()
          .columns(columns)
          .build();

    } catch (IOException e) {
      var message = String.format("Error occurred while processing %s: %s", file.getName(), e.getMessage());
      throw new FolderDbException(message, e);
    }
  }

  private List<String> getGeneratedColumns(Integer count) {
    return IntStream.range(0, count).mapToObj(i -> "column" + i).collect(Collectors.toList());
  }

  @Override
  protected Stream<List<String>> getData(File file, FileConfiguration configuration)
      throws FolderDbException {
    var hasHeaders = configuration.getHasHeaders() != null ? configuration.getHasHeaders() : getHasHeaders();

    try {
      var csvReader = getReader(file, configuration);
      // skip headers
      if (hasHeaders) {
        csvReader.skip(1);
      }
      return StreamSupport.stream(csvReader.spliterator(), false)
          .map(Arrays::asList);
    } catch (IOException e) {
      var message = String.format("Error occurred while processing %s: %s", file.getName(), e.getMessage());
      throw new FolderDbException(message, e);
    }
  }

  protected Character getDefaultDelimiter() {
    return ',';
  }

  private CSVReader getReader(File file, FileConfiguration config) throws IOException {
    var delimiter =
        config.getCsvDelimiter() == null ? getDefaultDelimiter() : config.getCsvDelimiter();
    InputStream is = Files.newInputStream(file.toPath());
    Reader reader = new BufferedReader(new InputStreamReader(is, guessEncoding(file)));
    CSVParser csvParser = new CSVParserBuilder().withSeparator(delimiter).build();
    return new CSVReaderBuilder(reader).withCSVParser(csvParser).build();
  }

  protected Boolean getHasHeaders() {
    return hasHeaders;
  }
}
