package malbec.util;

import malbec.fer.FerException;

@SuppressWarnings("serial")
public class InvalidConfigurationException extends FerException {

    public InvalidConfigurationException() {
        super();
    }

    public InvalidConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidConfigurationException(String message) {
        super(message);
    }

    public InvalidConfigurationException(Throwable cause) {
        super(cause);
    }

}
