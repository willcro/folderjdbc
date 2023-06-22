package com.willcro.folderdb.exception;

public class InvalidConfigurationException extends ConfigurationException {

  public InvalidConfigurationException(String configuration, Throwable cause) {
    super(configuration + " was invalid", cause);
  }

}
