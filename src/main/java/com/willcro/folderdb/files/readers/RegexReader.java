package com.willcro.folderdb.files.readers;

import com.willcro.folderdb.config.FileConfiguration;
import com.willcro.folderdb.exception.ConfigurationException;
import com.willcro.folderdb.exception.FolderDbException;
import com.willcro.folderdb.exception.InvalidConfigurationException;
import com.willcro.folderdb.exception.MissingConfigurationException;
import com.willcro.folderdb.sql.Table;
import com.willcro.folderdb.sql.TableV2;
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
import java.util.stream.Stream;

public class RegexReader extends LineByLineReader {

  @Override
  public String getId() {
    return "regex";
  }

  private Pattern getPattern(FileConfiguration config) throws ConfigurationException {
    if (config.getPattern() == null) {
      throw new MissingConfigurationException("pattern");
    }

    try {
      return Pattern.compile(config.getPattern());
    } catch (PatternSyntaxException ex) {
      throw new InvalidConfigurationException("pattern", ex);
    }
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

  @Override
  protected List<String> readLine(String line, File file, FileConfiguration config)
      throws ConfigurationException {
    var pattern = getPattern(config);
    Matcher matcher = pattern.matcher(line);
    if (matcher.matches()) {
      return matchResultToRow(matcher);
    }
    return null;
  }

  @Override
  protected List<String> getColumns(File file, FileConfiguration config)
      throws ConfigurationException {
    var pattern = getPattern(config);

    // this is the only way to get the number of groups without actually having any data
    // this works whether the pattern matches or not
    var groupCount = pattern.matcher("").groupCount();

    return generateColumnNames(groupCount);
  }

  @Override
  protected int skipLines() {
    return 0;
  }
}
