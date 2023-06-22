package com.willcro.folderdb.service.update;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UpdateState {

  /**
   * Type of update check (e.g. SHA1 or TIMESTAMP)
   */
  private String type;

  /**
   * Current state value (sha1 hash or timestamp)
   */
  private String value;

}
