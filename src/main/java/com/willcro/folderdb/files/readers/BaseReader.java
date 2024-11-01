package com.willcro.folderdb.files.readers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.mozilla.universalchardet.UniversalDetector;

@Slf4j
public abstract class BaseReader implements FileTableReader {

  private static final Map<String, ByteOrderMark> CHARSET_BYTE_ORDER_MARK_MAP = Map.of(
      "UTF-8", ByteOrderMark.UTF_8,
      "UTF-16LE", ByteOrderMark.UTF_16LE,
      "UTF-16BE", ByteOrderMark.UTF_16BE,
      "UTF-32LE", ByteOrderMark.UTF_32LE,
      "UTF-32BE", ByteOrderMark.UTF_32BE
  );

  protected Charset guessEncoding(File file) {
    try {
      var encoding = UniversalDetector.detectCharset(file);
      log.info("Guessed charset {}", encoding);
      if (encoding == null) {
        log.warn("Encoding detected as null, changing to UTF-8");
        encoding = "UTF-8";
      }
      return Charset.forName(encoding);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected InputStream getInputStream(File file, Charset charset) throws IOException {
    InputStream is = Files.newInputStream(file.toPath());
    if (CHARSET_BYTE_ORDER_MARK_MAP.containsKey(charset.name())) {
      return new BOMInputStream(is, CHARSET_BYTE_ORDER_MARK_MAP.get(charset.name()));
    }
    return is;
  }

}
