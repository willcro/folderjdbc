package com.willcro.folderdb.sql;

import java.util.List;
import java.util.stream.Stream;

@FunctionalInterface
public interface RowSupplier {

  Stream<List<Column>> getRows();

}
