package com.willcro.folderdb.files.readers;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import com.willcro.folderdb.config.FileConfiguration;
import com.willcro.folderdb.exception.ConfigurationException;
import com.willcro.folderdb.sql.Table;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;

/**
 * Reader for .csv files
 */
@Slf4j
public class CsvReader extends BaseReader {

  @Override
  public String getId() {
    return "csv";
  }

  @Override
  public List<Table> readFile(File file, FileConfiguration config) throws ConfigurationException {
    try {
      var delimiter =
          config.getCsvDelimiter() == null ? getDefaultDelimiter() : config.getCsvDelimiter();
      InputStream is = Files.newInputStream(file.toPath());
      Reader reader = new BufferedReader(new InputStreamReader(is, guessEncoding(file)));
      CSVParser csvParser = new CSVParserBuilder().withSeparator(delimiter).build();
      CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(csvParser).build();
      var columns = Arrays.asList(csvReader.readNextSilently());

      Stream<List<String>> rows = StreamSupport.stream(csvReader.spliterator(), false)
          .map(Arrays::asList);

      var table = Table.builder()
          .columns(columns)
          .name(file.getName())
          .rows(rows)
          .build();

      return Collections.singletonList(table);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected Character getDefaultDelimiter() {
    return ',';
  }
}
