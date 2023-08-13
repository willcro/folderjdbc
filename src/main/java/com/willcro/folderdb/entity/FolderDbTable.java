package com.willcro.folderdb.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.SneakyThrows;

@Data
public class FolderDbTable {

  private String filename;
  private String tableName;
  private String subName;
  private String columns;
  private String metadata;
  private Boolean loadedData;
  private Boolean dirty;

  @SneakyThrows
  public List<String> getColumnsAsList() {
    return new ObjectMapper().readValue(this.columns, new TypeReference<>() {
    });
  }

  @SneakyThrows
  public Map<String,Object> getMetadataAsMap() {
    return new ObjectMapper().readValue(this.metadata, new TypeReference<>() {
    });
  }
}
