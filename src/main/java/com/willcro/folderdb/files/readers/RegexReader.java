package com.willcro.folderdb.files.readers;

import com.willcro.folderdb.config.FileConfiguration;
import com.willcro.folderdb.exception.ConfigurationException;
import com.willcro.folderdb.exception.InvalidConfigurationException;
import com.willcro.folderdb.sql.Table;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RegexReader extends BaseReader {

  @Override
  public String getId() {
    return "regex";
  }

  @Override
  public List<Table> readFile(File file, FileConfiguration config) throws ConfigurationException {
    if (config.getPattern() == null) {
      throw new ConfigurationException("pattern");
    }

    Pattern pattern;

    try {
      pattern = Pattern.compile(config.getPattern());
    } catch (PatternSyntaxException ex) {
      throw new InvalidConfigurationException("pattern", ex);
    }

    // todo: verify that pattern contains at least one capture group

    List<List<String>> records = new ArrayList<>();
    var invalidLines = 0;
    var groups = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = br.readLine()) != null) {
        Matcher matcher = pattern.matcher(line);
        groups = Math.max(groups, matcher.groupCount());
        if (matcher.matches()) {
          records.add(matchResultToRow(matcher));
        } else {
          invalidLines++;
        }
      }
    } catch (IOException e) {
      //todo
      e.printStackTrace();
    }

    var columns = generateColumnNames(groups);

    var table = Table.builder()
        .name(file.getName())
        .columns(columns)
        .rows(records.stream())
        .build();

    return Collections.singletonList(table);
  }

  private List<String> matchResultToRow(MatchResult mr) {
    var out = new ArrayList<String>(mr.groupCount());
    for (int i = 1; i <= mr.groupCount(); i++) {
      out.add(mr.group(i));
    }
    return out;
  }

  private List<String> generateColumnNames(Integer count) {
    return IntStream.range(1, count + 1)
        .mapToObj(it -> "group" + it)
        .collect(Collectors.toList());
  }
}
