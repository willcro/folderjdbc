package com.willcro.folderdb.service.update;

import com.willcro.folderdb.entity.FolderDbFile;
import com.willcro.folderdb.tester.UpdateState;

import java.io.File;

public interface UpdateChecker {

    UpdateState getState(File file);

    default boolean isUpdated(UpdateState oldState, UpdateState newState) {
        return !oldState.equals(newState);
    }

}
