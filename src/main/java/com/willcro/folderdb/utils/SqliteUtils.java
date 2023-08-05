package com.willcro.folderdb.utils;

import java.sql.SQLSyntaxErrorException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.sqlite.parser.Parser;
import org.sqlite.parser.ast.Cmd;
import org.sqlite.parser.ast.QualifiedName;

@Slf4j
public class SqliteUtils {

  public static List<String> getTablesFromQuery(String query) throws SQLSyntaxErrorException {
    Cmd cmd = Parser.parse(query);
    return walk(cmd, new HashSet<>()).collect(Collectors.toList());
  }

  private static Stream<String> walk(Object o, Set<Integer> visited) {
    if (o == null) {
      return Stream.empty();
    }

    if (visited.contains(o.hashCode())) {
      return Stream.empty();
    }

    Set<Integer> newVisited = new HashSet<>(visited);
    newVisited.add(o.hashCode());

    if (o instanceof Collection) {
      return ((Collection<?>) o).stream()
          .flatMap(it -> walk(it, newVisited));
    }

    Stream<String> first = Arrays.stream(o.getClass().getFields()).map(f -> {
          try {
            return f.get(o);
          } catch (Exception e) {
            log.warn("Exception occurred while parsing query", e);
            return null;
          }
        })
        .filter(Objects::nonNull)
        .filter(it -> it instanceof QualifiedName)
        .map(it -> (QualifiedName) it)
        .map(it -> it.name);

    Stream<String> rest = Arrays.stream(o.getClass().getFields()).map(f -> {
          try {
            return f.get(o);
          } catch (Exception e) {
            log.warn("Exception occurred while parsing query", e);
            return null;
          }
        })
        .filter(Objects::nonNull)
        .flatMap(it -> walk(it, newVisited));

    return Stream.concat(first, rest);
  }

}
