package com.willcro.folderdb.service.update;

import java.io.File;

/**
 * Update checker that uses a SHA1 hash if the file is less or equal to 10 MB and timestamp check
 * below that
 */
public class HybridUpdateChecker implements UpdateChecker {

  private static final long THRESHOLD = 10_000_000L; // 10 megabytes

  private static final TimestampUpdateChecker timestampUpdateChecker = new TimestampUpdateChecker();
  private static final HashUpdateChecker hashUpdateChecker = new HashUpdateChecker();

  @Override
  public UpdateState getState(File file) {
    if (file.length() > THRESHOLD) {
      return timestampUpdateChecker.getState(file);
    }

    return hashUpdateChecker.getState(file);
  }
}
