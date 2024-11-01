package com.willcro.folderdb.files.readers;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.willcro.folderdb.config.FileConfiguration;
import com.willcro.folderdb.exception.FolderDbException;
import com.willcro.folderdb.exception.InvalidFileException;
import com.willcro.folderdb.sql.TableV2;
import com.willcro.folderdb.utils.JsonLayer;
import com.willcro.folderdb.utils.JsonLayer.LayerType;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reader that flattens a JSON file into two columns: path and value
 */
public class JsonFlatReader extends BaseReader {

  @Override
  public String getId() {
    return "jsonflat";
  }

  @Override
  public List<TableV2> readFile(File file, FileConfiguration config) throws FolderDbException {
    // Table is always exactly the same
    return Collections.singletonList(TableV2.builder().columns(List.of("path", "value")).build());
  }

  @Override
  public Stream<List<String>> getData(File file, TableV2 table, FileConfiguration configuration)
      throws FolderDbException {
    try {
      JsonFactory jsonFactory = getFactory();
      var json2 = jsonFactory.createParser(file);
      return getKeyValues(json2);
    } catch (Exception e) {
      throw new InvalidFileException(file.getName(), e);
    }
  }

  private static Stream<List<String>> getKeyValues(JsonParser parser) throws IOException {
//    var currentToken = parser.nextToken();

    Stack<JsonLayer> stack = new Stack<>();
    stack.push(new JsonLayer(LayerType.BASE));

    return Stream.iterate(parser.nextToken(), Objects::nonNull, (currentToken) -> {
      try {
        return parser.nextToken();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }).map((currentToken) -> {
      try {
        if (currentToken.equals(JsonToken.START_OBJECT)) {
          stack.add(new JsonLayer(LayerType.OBJECT));
        } else if (currentToken.equals(JsonToken.START_ARRAY)) {
          stack.add(new JsonLayer(LayerType.ARRAY));
        } else if (currentToken.equals(JsonToken.END_OBJECT)) {
          stack.pop();
          stack.peek().increment();
        } else if (currentToken.equals(JsonToken.END_ARRAY)) {
          stack.pop();
          stack.peek().increment();
        } else if (currentToken.equals(JsonToken.FIELD_NAME)) {
          stack.peek().setKey(parser.currentName());
        } else if (currentToken.equals(JsonToken.VALUE_NULL)) {
          // TODO: handle NULL properly
          stack.peek().increment();
        } else if (currentToken.isScalarValue()) {
          var out = List.of(stack.stream().map(JsonLayer::toString).collect(Collectors.joining()),
              parser.getValueAsString());
          stack.peek().increment();
          return out;
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return null;
    }).filter(Objects::nonNull).onClose(() -> {
      try {
        parser.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  protected JsonFactory getFactory() {
    return new JsonFactory();
  }
}
