package activemq;

public class HeartbeatRunnable extends AbstractHeartbeatMonitor {

    public HeartbeatRunnable(ActiveMQServerMonitor monitor) {
        super(monitor);
    }

    @Override
    protected String failedLogMessage() {
        return "Failed to send heartbeat";
    }

    @Override
    protected void sendFailedEmail() {
        // Not sending email
    }

    @Override
    protected boolean sendHeartbeat() {
        return monitor.sendHeartbeat();
    }

    @Override
    protected String successLogMessage() {
        return "Sent heartbeat";
    }

    @Override
    protected void sendReconnectEmail() {
        // not sending email
    }

}
