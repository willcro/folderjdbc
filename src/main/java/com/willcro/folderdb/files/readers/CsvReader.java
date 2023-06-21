package com.willcro.folderdb.files.readers;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.willcro.folderdb.config.FileConfiguration;
import com.willcro.folderdb.exception.ConfigurationException;
import com.willcro.folderdb.sql.Table;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Reader for .csv files
 */
public class CsvReader extends BaseReader {

  @Override
  public String getId() {
    return "csv";
  }

  @Override
  public List<Table> readFile(File file, FileConfiguration config) throws ConfigurationException {
    CsvMapper mapper = new CsvMapper();
    var delimiter =
        config.getCsvDelimiter() == null ? getDefaultDelimiter() : config.getCsvDelimiter();
    mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
    var schema = CsvSchema.emptySchema().withAllowComments(true)
        .withColumnSeparator(delimiter);

    try {
      MappingIterator<List<String>> it = mapper.readerForListOf(String.class).with(schema)
          .readValues(file);
      var columns = it.next();
      Stream<List<String>> rows = StreamSupport.stream(
          Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false);
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
