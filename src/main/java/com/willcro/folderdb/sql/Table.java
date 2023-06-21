package com.willcro.folderdb.sql;

import java.util.function.BiFunction;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Stream;

/**
 * Class representing the contents of a table
 */
@Data
@Builder
public class Table {

    private final String name;
    private final List<String> columns;
    private final Stream<List<String>> rows;

}
