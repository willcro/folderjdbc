package com.willcro.folderdb.files.readers;

import com.willcro.folderdb.config.FileConfiguration;
import com.willcro.folderdb.exception.ConfigurationException;
import com.willcro.folderdb.sql.Table;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Abstract reader where each line in a file corresponds to one row in the resulting table
 */
abstract public class LineByLineReader extends BaseReader {

    @Override
    public List<Table> readFile(File file, FileConfiguration config) throws ConfigurationException {
        Stream<List<String>> rows = null;
        try {
            rows = Files.lines(file.toPath()).skip(skipLines()).map(line -> readLine(line, file, config));
        } catch (IOException e) {
            e.printStackTrace();
        }
        var columns = getColumns(file, config);

        var table = Table.builder()
                .name(file.getName())
                .columns(columns)
                .rows(rows)
                .build();


        return Collections.singletonList(table);
    }

    protected abstract List<String> readLine(String line, File file, FileConfiguration config);

    protected abstract List<String> getColumns(File file, FileConfiguration config);

    /**
     * Get how many lines to skip at the top of the file. Defaults to 1 (for the header line of a CSV)
     * @return number of lines to skip
     */
    protected int skipLines() {
        return 1;
    }

}
