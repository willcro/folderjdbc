package com.willcro.folderdb.files.readers;

import com.willcro.folderdb.config.FileConfiguration;
import com.willcro.folderdb.exception.ConfigurationException;
import com.willcro.folderdb.sql.Table;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Reader that can handle fixed-width files
 *
 * WIP
 */
public class FixedWidthFileReader extends BaseReader {

    private static final Integer LINES_TO_READ = 100;
    private static final Double THRESHOLD = 0.33;
//    private static final Pattern BORDER_PATTERN = Pattern.compile("((?<=[^\\d])(?=\\d)|(?<=[\\d])(?=[^\\d])|(?<=[^ ])(?= )|(?<=[ ])(?=[^ ]))");
    private static final Pattern BORDER_PATTERN = Pattern.compile("(?<=[ ])(?=[^ ])");

    @Override
    public String getId() {
        return "fixedwidth";
    }

    @Override
    public List<Table> readFile(File file, FileConfiguration config) throws ConfigurationException {
        Stream<List<String>> rows = null;

        var columnIndexes = config.getFixedWidthColumns() == null
                ? getColumnIndexes(file)
                : getIndexesFromConfig(config.getFixedWidthColumns());

        try {
            var lines = Files.lines(file.toPath());
            rows = lines.map(line -> splitAll(line, columnIndexes));
        } catch (IOException e) {
            e.printStackTrace();
        }
        var columns = getColumns(columnIndexes.size());

        var table = Table.builder()
                .name(file.getName())
                .columns(columns)
                .rows(rows)
                .build();


        return Collections.singletonList(table);
    }

    private List<Integer> getIndexesFromConfig(List<Integer> config) {
        var ret = new ArrayList<Integer>();
        var pos = 0;
        ret.add(pos);

        // ignore the last configured width and assume it goes to the end of the file
        for (int i = 0; i < config.size() - 1; i++) {
            pos += config.get(i);
            ret.add(pos);
        }
        return ret;
    }

    private List<String> getColumns(int size) {
        return IntStream.range(0, size).boxed().map(num -> "column" + num).collect(Collectors.toList());
    }

    private static List<String> splitAll(String line, List<Integer> splitAt) {
        var out = new ArrayList<String>(splitAt.size());
        for (int i = 0; i < splitAt.size(); i++) {
            if (i == splitAt.size() - 1) {
                // this is the last column. consider the end of the string to be the end
                out.add(line.substring(splitAt.get(i)));
            } else {
                out.add(line.substring(splitAt.get(i), splitAt.get(i + 1)));
            }
        }

        return out;
    }

    /**
     * Return a list of the best guess for where the start of each fixed-width column is
     * @param file
     * @return
     */
    private static List<Integer> getColumnIndexes(File file) {
        try (var lines = Files.lines(file.toPath()).limit(LINES_TO_READ)) {
            AtomicInteger linesRead = new AtomicInteger();
            return lines.flatMap(line -> {
                        linesRead.getAndIncrement();
                        return findBreakPoints(line).stream();
                    })
                    .collect(Collectors.groupingBy(it -> it, Collectors.counting()))
                    .entrySet().stream()
                    .filter(entry -> (double) entry.getValue() / (double) linesRead.get() > THRESHOLD)
                    .map(Map.Entry::getKey)
                    .sorted()
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * List possible breakpoint candidates for a line. The follow boundaries are considered
     * - border between number and letter
     * - border between space and non-space
     * @param line
     * @return
     */
    private static Set<Integer> findBreakPoints(String line) {
        var substrings = BORDER_PATTERN.split(line);
        var out = new HashSet<Integer>();
        var index = 0;

        for (String substring : substrings) {
            out.add(index);
            index += substring.length();
        }

        return out;
    }

}
