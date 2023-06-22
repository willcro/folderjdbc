package com.willcro.folderdb.service.update;

import java.io.File;

public interface UpdateChecker {

  UpdateState getState(File file);

  default boolean isUpdated(UpdateState oldState, UpdateState newState) {
    return !oldState.equals(newState);
  }

}
