package com.willcro.folderdb.sql;

import lombok.Data;

import java.util.List;
import java.util.stream.Stream;

@Data
public class TableV2 {

    private final String name;
    private final List<Column> columns;
    private final RowSupplier rowSupplier;

}
