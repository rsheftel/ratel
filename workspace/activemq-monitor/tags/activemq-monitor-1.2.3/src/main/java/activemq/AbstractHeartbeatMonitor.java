package activemq;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHeartbeatMonitor implements Runnable {

    final Logger log = LoggerFactory.getLogger(getClass());
    protected final ActiveMQServerMonitor monitor;
    private boolean sentFailedEmail;

    public AbstractHeartbeatMonitor(ActiveMQServerMonitor monitor) {
        this.monitor = monitor;
    }

    abstract protected String successLogMessage();
    abstract protected String failedLogMessage();
    abstract protected boolean sendHeartbeat();
    abstract protected void sendFailedEmail();
    abstract protected void sendReconnectEmail();
    
    
    @Override
    public void run() {
        try {
            if (sendHeartbeat()) {
                log.info(successLogMessage());
                if (sentFailedEmail) {
                    sendReconnectEmail();
                    sentFailedEmail = false;
                }
            } else {
                log.error(failedLogMessage());
                if (!sentFailedEmail) {
                    sendFailedEmail();
                    sentFailedEmail = true;
                }
            }
            logHeartbeatRoundTrip(monitor);
        } catch (Exception e) {
            log.error("Caught exception on scheduled thread", e);
        }
    }

    private void logHeartbeatRoundTrip(final ActiveMQServerMonitor monitor) {
        // This will be null on the startup sequence, as we have not received
        // before we send.
        Date lastReceived = monitor.getHeartbeatReceivedTime();
        if (lastReceived != null) {
            Date now = new Date();
            if (now.getTime() - (monitor.getHeartbeatInterval() * 2 * 1000) > lastReceived.getTime()) {
                log.warn("Heartbeat is late");
            }
        }
    }


}
