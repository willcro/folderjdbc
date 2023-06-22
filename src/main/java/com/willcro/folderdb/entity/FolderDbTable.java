package com.willcro.folderdb.entity;

import lombok.Data;

@Data
public class FolderDbTable {

  private String filename;
  private String tableName;
  private Boolean loadedData;
  private Boolean dirty;

}
