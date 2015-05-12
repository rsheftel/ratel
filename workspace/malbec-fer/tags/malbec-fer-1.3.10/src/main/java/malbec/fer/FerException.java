package malbec.fer;

/**
 * Base class for all of the FIX Execution Router
 * 
 */
@SuppressWarnings("serial")
public class FerException extends Exception {


    public FerException() {
        super();
    }

    public FerException(String message, Throwable cause) {
        super(message, cause);
    }

    public FerException(String message) {
        super(message);
    }

    public FerException(Throwable cause) {
        super(cause);
    }

}
