package com.willcro.folderdb.sql;

import java.util.List;
import lombok.Data;

@Data
public class TableV2 {

  private final String name;
  private final List<Column> columns;
  private final RowSupplier rowSupplier;

}
