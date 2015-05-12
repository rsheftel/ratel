package activemq;

import util.ActiveMQMonitorException;

/**
 * 
 */
@SuppressWarnings("serial")
public class JmsException extends ActiveMQMonitorException {

    public JmsException() {
    }

    public JmsException(String message) {
        super(message);
    }

    public JmsException(Throwable cause) {
        super(cause);
    }

    public JmsException(String message, Throwable cause) {
        super(message, cause);
    }

}
