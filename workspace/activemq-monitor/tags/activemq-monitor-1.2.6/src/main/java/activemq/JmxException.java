package activemq;

import util.ActiveMQMonitorException;

/**
 * 
 */
@SuppressWarnings("serial")
public class JmxException extends ActiveMQMonitorException {

    public JmxException() {
    }

    public JmxException(String message) {
        super(message);
    }

    public JmxException(Throwable cause) {
        super(cause);
    }

    public JmxException(String message, Throwable cause) {
        super(message, cause);
    }

}
