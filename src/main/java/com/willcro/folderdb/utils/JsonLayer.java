package com.willcro.folderdb.utils;

import lombok.Data;
import lombok.Setter;

@Data
public class JsonLayer {

  public enum LayerType {
    ARRAY, OBJECT, BASE
  }

  private final LayerType layerType;
  private Long index = 0L;
  @Setter
  private String key;

  public void increment() {
    this.index++;
  }

  public String toString() {
    if (layerType == LayerType.ARRAY) {
      return "[" + index + "]";
    } else if (layerType == LayerType.OBJECT) {
      return "." + key;
    } else {
      return "$";
    }
  }


}
