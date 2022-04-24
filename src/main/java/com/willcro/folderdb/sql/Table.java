package com.willcro.folderdb.sql;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Class representing the contents of a table
 */
@Data
@Builder
public class Table {

    private final String name;
    private final List<String> columns;
    private final List<List<String>> rows;

}
