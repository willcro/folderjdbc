package com.willcro.folderdb.sql;

import lombok.Data;

@Data
public class Column {

  private ColumnTypeEnum type;
  private String name;

}
