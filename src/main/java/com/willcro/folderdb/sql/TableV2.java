package com.willcro.folderdb.sql;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TableV2 {

  /**
   * Gets the name of this table within the file. If a file only consists of one table, this will
   * be null. The actual name of the table will be in the format {fileName}/{subName}
   */
  private String subName;

  /**
   * Gets the name of the columns in the table
   */
  private List<String> columns;

  /**
   * Additional data stored for this table
   */
  @Builder.Default
  private Map<String, Object> metadata = Collections.emptyMap();

}
