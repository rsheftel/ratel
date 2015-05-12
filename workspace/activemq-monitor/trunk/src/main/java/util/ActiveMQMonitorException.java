package util;

/**
 * Base class for exceptions within this application.
 * 
 */
@SuppressWarnings("serial")
public class ActiveMQMonitorException extends RuntimeException {

    public ActiveMQMonitorException() {
    }

    public ActiveMQMonitorException(String message) {
        super(message);
    }

    public ActiveMQMonitorException(Throwable cause) {
        super(cause);
    }

    public ActiveMQMonitorException(String message, Throwable cause) {
        super(message, cause);
    }

}
