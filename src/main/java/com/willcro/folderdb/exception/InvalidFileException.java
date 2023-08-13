package com.willcro.folderdb.exception;

public class InvalidFileException extends FolderDbException {

  public InvalidFileException(String fileName, String issue) {
    super(String.format("File %s was invalid: %s", fileName, issue));
  }

  public InvalidFileException(String fileName, Throwable cause) {
    super(String.format("File %s was invalid: %s", fileName, cause.getMessage()), cause);
  }
}
