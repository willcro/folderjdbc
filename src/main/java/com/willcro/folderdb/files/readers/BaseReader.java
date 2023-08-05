package com.willcro.folderdb.files.readers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.universalchardet.UniversalDetector;

@Slf4j
public abstract class BaseReader implements FileTableReader {

  protected Charset guessEncoding(File file) {
    try {
      var encoding = UniversalDetector.detectCharset(file);
      log.info("Guessed charset {}", encoding);
      return Charset.forName(encoding);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
