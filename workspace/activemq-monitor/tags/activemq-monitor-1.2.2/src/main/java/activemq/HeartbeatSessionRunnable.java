package activemq;

public class HeartbeatSessionRunnable extends AbstractHeartbeatMonitor {

    public HeartbeatSessionRunnable(ActiveMQServerMonitor monitor) {
        super(monitor);
    }

    @Override
    protected String failedLogMessage() {
        return "Failed to send heartbeat with new session";
    }

    @Override
    protected void sendFailedEmail() {
    // we are not sending emails
    }

    @Override
    protected boolean sendHeartbeat() {
        return monitor.sendHeartbeat(true);
    }

    @Override
    protected String successLogMessage() {
        return "Sent heartbeat with new session";
    }

    @Override
    protected void sendReconnectEmail() {
    // Not sending email
    }

}
