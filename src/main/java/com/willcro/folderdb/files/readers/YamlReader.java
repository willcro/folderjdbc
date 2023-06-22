package com.willcro.folderdb.files.readers;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * Current support is limited the following situations: - Array of objects
 *
 * TODO
 * - Object containing arrays
 *
 * All other situations are disallowed
 */
@Slf4j
public class YamlReader extends JsonReader {

  @Override
  public String getId() {
    return "yaml";
  }

  @Override
  protected JsonFactory getFactory() {
    return new YAMLFactory();
  }
}
