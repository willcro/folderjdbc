package com.willcro.folderdb.exception;

public class MissingConfigurationException extends ConfigurationException {

  public MissingConfigurationException(String missingField) {
    super("Missing required field " + missingField);
  }

}
