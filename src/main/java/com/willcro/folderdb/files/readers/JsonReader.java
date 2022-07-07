package com.willcro.folderdb.files.readers;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.willcro.folderdb.config.FileConfiguration;
import com.willcro.folderdb.exception.ConfigurationException;
import com.willcro.folderdb.sql.Table;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Current support is limited the following situations:
 * - Array of objects
 *
 * TODO
 * - Object containing arrays
 *
 * All other situations are disallowed
 */
@Slf4j
public class JsonReader extends BaseReader {

    @Override
    public String getId() {
        return "json";
    }

    @Override
    public List<Table> readFile(File file, FileConfiguration config) throws ConfigurationException {
        try {
            JsonFactory jsonFactory = new JsonFactory();
            var json = jsonFactory.createParser(file);
            json.configure(JsonParser.Feature.ALLOW_COMMENTS, true);

            json.nextToken();
            var columns = getKeysInArray(json);

            json.close();

            var json2 = jsonFactory.createParser(file);
            json2.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
            json2.setCodec(new ObjectMapper());
            return Collections.singletonList(getTableFromArray(json2, columns, file.getName()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Table getTableFromArray(JsonParser json, List<String> columns, String tableName) throws IOException {
        var columnToIndex = new HashMap<String, Integer>();
        for (int i = 0; i < columns.size(); i++) {
            columnToIndex.put(columns.get(i), i);
        }

        var next = json.nextToken();
        next = json.nextToken();
        if (next == null || next.equals(JsonToken.END_ARRAY)) {
            return Table.builder()
                    .name(tableName)
                    .columns(columns)
                    .rows(Stream.empty())
                    .build();
        }

        var first = getColumns(json, columnToIndex);

        Stream<List<String>> rows = Stream.iterate(first, Objects::nonNull, it -> {
            try {
                var next2 = json.nextToken();
                if (next2 == null || next2.equals(JsonToken.END_ARRAY)) {
                    return null;
                }
                return getColumns(json, columnToIndex);
            } catch (IOException e) {
                // todo
                e.printStackTrace();
                return null;
            }
        });

        return Table.builder()
                .name(tableName)
                .columns(columns)
                .rows(rows)
                .build();
    }

    private List<String> getKeysInArray(JsonParser json) throws IOException {
        if (!JsonToken.START_ARRAY.equals(json.currentToken())) {
            log.error("JSON parse error at {}:{}", json.currentLocation().getLineNr(), json.currentLocation().getColumnNr());
            throw new RuntimeException("Expected an array, but found something else");
        }

        var level = new Stack<String>();
        var columns = new HashSet<String>();

        var next = json.nextToken();
        // loop until we reach the end of the original array
        while(!JsonToken.END_ARRAY.equals(next)) {
            if (next == null) {
                throw new RuntimeException("Unexpected end of file");
            }
            if (next.isScalarValue()) {
                level.push(json.currentName());
                columns.add(String.join(".", level));
                level.pop();
            } else if (next.equals(JsonToken.START_OBJECT)) {
                if (json.currentName() != null) {
                    level.push(json.currentName());
                }
            } else if (next.equals(JsonToken.END_OBJECT)) {
                if (!level.isEmpty()) {
                    level.pop();
                }
            } else if (next.equals(JsonToken.START_ARRAY)) {
                level.push(json.currentName());
                columns.add(String.join(".", level));
                level.pop();
                json.skipChildren();
            }

            next = json.nextToken();
        }

        return columns.stream().sorted().collect(Collectors.toList());

    }

    /**
     * Get the column data for a single row
     * @param reader parser currently at the start of an object
     * @param columns map of column name to order
     * @return array of row data in the correct order
     * @throws IOException
     */
    private List<String> getColumns(JsonParser reader, Map<String, Integer> columns) throws IOException {
        // create list of correct size full of nulls
        var ret = IntStream.range(0, columns.size()).mapToObj(it -> (String) null).collect(Collectors.toCollection(ArrayList::new));

        if (!JsonToken.START_OBJECT.equals(reader.currentToken())) {
            log.error("JSON parse error at {}:{}", reader.currentLocation().getLineNr(), reader.currentLocation().getColumnNr());
            throw new RuntimeException("Expected an object, but found something else");
        }

        var level = new Stack<String>();
        var next = reader.nextToken();

        while (level.size() > 0 || !next.equals(JsonToken.END_OBJECT)) {
            if (next.equals(JsonToken.FIELD_NAME)) {
                level.push(reader.currentName());
                var fieldPath = Strings.join(level, '.');
                level.pop();
                var value = reader.nextValue();
                if (JsonToken.VALUE_STRING.equals(value)) {
                    ret.set(columns.get(fieldPath), reader.getValueAsString());
                } else if (JsonToken.START_ARRAY.equals(value)) {
                    ret.set(columns.get(fieldPath), readObjectAsString(reader));
                } else if (JsonToken.START_OBJECT.equals(value)) {
                    level.push(reader.currentName());
                }
            } else if (JsonToken.END_OBJECT.equals(next)) {
                level.pop();
            }
            next = reader.nextToken();
        }

        return ret;
    }

    /**
     * Converts JSON objects and arrays into strings
     * @param json parser currently at an array or object
     * @return value as string
     * @throws IOException
     */
    private String readObjectAsString(JsonParser json) throws IOException {
        // this feels pretty hacky, but I couldn't find an easier way to do it
        return new ObjectMapper().writeValueAsString(json.readValueAs(Object.class));
    }

    private static List<String> getAllArraysJackson(File file) throws IOException {
        JsonFactory jsonFactory = new JsonFactory();
        var json = jsonFactory.createParser(file);

        Stack<String> currentLevel = new Stack<>();

        var lastName = "";
        var out = new ArrayList<String>();

        var nextToken = json.nextToken();
        while (nextToken != null) {
            if (nextToken.equals(JsonToken.START_OBJECT)) {
                currentLevel.push(lastName);
            } else if (nextToken.equals(JsonToken.END_OBJECT)) {
                currentLevel.pop();
            } else if (nextToken.equals(JsonToken.FIELD_NAME)) {
                lastName = json.currentName();
            } else if (nextToken.equals(JsonToken.START_ARRAY)) {
                currentLevel.push(lastName);
                out.add(String.join(".", currentLevel));
                currentLevel.pop();
            }
            nextToken = json.nextToken();
        }
        return out;
    }

    public static void main(String[] args) throws IOException {
        var file = Path.of("C:\\Users\\Will\\Documents\\folderdbtest\\testfiles\\arraytest.json").toFile();
        System.out.println(getAllArraysJackson(file));
    }

}
