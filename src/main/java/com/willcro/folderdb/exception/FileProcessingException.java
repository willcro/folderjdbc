package com.willcro.folderdb.exception;

public class FileProcessingException extends FolderDbException {

  public FileProcessingException(String filename, String problem) {
    super(String.format("Error occurred while processing %s: %s", filename, problem));
  }

  public FileProcessingException(String filename, Throwable cause) {
    super(String.format("Error occurred while processing %s: %s", filename, cause.getMessage()), cause);
  }
}
