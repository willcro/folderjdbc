package com.willcro.folderdb.sql;

import java.util.List;

/**
 * Class representing the contents of a table
 */
public class Table {

    public List<String> getColumns() {
        return columns;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    private final List<String> columns;
    private final List<List<String>> rows;

    public Table(List<String> columns, List<List<String>> rows) {
        this.columns = columns;
        this.rows = rows;
    }
}
