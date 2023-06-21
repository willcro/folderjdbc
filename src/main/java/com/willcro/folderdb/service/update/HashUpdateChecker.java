package com.willcro.folderdb.service.update;

import com.willcro.folderdb.files.FileUtils;

import java.io.File;

/**
 * Update checker that uses a SHA1 hash of the file to determine if the file has changed
 */
public class HashUpdateChecker implements UpdateChecker {

    private static final String TYPE = "SHA1";

    @Override
    public UpdateState getState(File file) {
        var hash = FileUtils.createSha1(file);
        return new UpdateState(TYPE, hash);
    }
}
