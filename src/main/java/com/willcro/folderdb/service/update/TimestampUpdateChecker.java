package com.willcro.folderdb.service.update;

import com.willcro.folderdb.tester.UpdateState;

import java.io.File;

/**
 * UpdateChecker that checks for updates based on the OS-reported update timestamp of the file. A file is considered
 * to be changed if the update timestamp is different the recorded state
 */
public class TimestampUpdateChecker implements UpdateChecker {

    private static final String TYPE = "TIMESTAMP";

    @Override
    public UpdateState getState(File file) {
        return new UpdateState(TYPE, String.valueOf(file.lastModified()));
    }
}
