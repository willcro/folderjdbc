package com.willcro.folderdb.exception;

import java.util.regex.PatternSyntaxException;

public class InvalidConfigurationException extends ConfigurationException {

    public InvalidConfigurationException(String configuration, Throwable cause) {
        super(configuration + " was invalid", cause);
    }

}
