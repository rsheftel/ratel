package activemq;



public class HeartbeatConnectionRunnable extends AbstractHeartbeatMonitor {
    
    public HeartbeatConnectionRunnable(ActiveMQServerMonitor monitor) {
        super(monitor);
    }

    
    @Override
    protected String successLogMessage() {
        return "Sent heartbeat with new connection";
    }
    
    @Override
    protected String failedLogMessage() {
        return "Failed to send heartbeat with new connection";
    }
    
    @Override
    protected boolean sendHeartbeat() {
        return monitor.sendConnectionHeartbeat();
    }
    
    @Override
    protected void sendFailedEmail() {
        monitor.sendFailedConnectionEmail(monitor.getBrokerUrl());
    }

    @Override
    protected void sendReconnectEmail() {
        monitor.sendReconnectEmail();
    }
}