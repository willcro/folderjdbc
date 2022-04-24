package com.willcro.folderdb.files.readers;

import com.google.gson.Gson;
import com.google.gson.stream.JsonToken;
import com.willcro.folderdb.config.FileConfiguration;
import com.willcro.folderdb.exception.ConfigurationException;
import com.willcro.folderdb.sql.Table;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
 * - Object where all values are arrays
 *
 * All other situations are disallowed
 */
public class JsonReader extends BaseReader {

    public static void main(String[] args) throws ConfigurationException {
        var path = "C:\\Users\\Will\\Documents\\folderdbtest\\test.json";
        var file = Path.of(path).toFile();
        var table = new JsonReader().readFile(file);
        var test = 1;
    }

    @Override
    public String getId() {
        return "json";
    }

    @Override
    public List<Table> readFile(File file, FileConfiguration config) throws ConfigurationException {

        try {
            var json = new Gson().newJsonReader(new FileReader(file));
            if (!json.peek().equals(JsonToken.BEGIN_ARRAY)) {
                throw new RuntimeException("Top level of JSON file has to be an array");
            }
            json.beginArray();
            var allKeys = new HashSet<String>();

            while(json.hasNext() && json.peek().equals(JsonToken.BEGIN_OBJECT)) {
                allKeys.addAll(getKeysInObject(json));
            }

            var columns = new ArrayList<>(allKeys);

            // calculate map for speed improvement
            var columnToIndex = new HashMap<String, Integer>();
            for (int i = 0; i < columns.size(); i++) {
                columnToIndex.put(columns.get(i), i);
            }

            // start the reader over
            var json2 = new Gson().newJsonReader(new FileReader(file));
            json2.beginArray();

            Stream<List<String>> rows = Stream.iterate(null, it -> {
                try {
                    return json2.hasNext() && json2.peek().equals(JsonToken.BEGIN_OBJECT);
                } catch (IOException e) {
                    // todo
                    e.printStackTrace();
                    return false;
                }
            }, it -> {
                try {
                    return getColumns(json2, columnToIndex);
                } catch (IOException e) {
                    // todo
                    e.printStackTrace();
                    return null;
                }
            });

            return Collections.singletonList(Table.builder()
                    .name(file.getName())
                    .columns(columns)
                    .rows(rows)
                    .build());

        } catch (IOException e) {
            e.printStackTrace();
        }

        // todo
        return Collections.emptyList();
    }

    private Set<String> getKeysInObject(com.google.gson.stream.JsonReader reader) throws IOException {
        var ret = new HashSet<String>();
        if (!reader.peek().equals(JsonToken.BEGIN_OBJECT)) {
            throw new RuntimeException("Expected an object, but found something else");
        }
        reader.beginObject();
        while (reader.hasNext()) {
            ret.add(reader.nextName());
            reader.skipValue();
        }

        reader.endObject();
        return ret;
    }

    private List<String> getColumns(com.google.gson.stream.JsonReader reader, Map<String, Integer> columns) throws IOException {
        var ret = new ArrayList<String>(IntStream.range(0, columns.size()).mapToObj(it -> (String) null).collect(Collectors.toList()));
        // fill with nulls
        if (!reader.peek().equals(JsonToken.BEGIN_OBJECT)) {
            throw new RuntimeException("Expected an object, but found something else");
        }
        reader.beginObject();
        while (reader.hasNext()) {
            var key = reader.nextName();
            if (reader.peek().equals(JsonToken.STRING)) {
                ret.set(columns.get(key), reader.nextString());
            } else {
                reader.skipValue();
            }
        }

        reader.endObject();
        return ret;
    }

}
