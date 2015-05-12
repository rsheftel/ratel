package activemq;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.ActiveMQMonitorException;
import util.Monitoring;

public class ServerMonitor {

    private static final class HeartbeatConnectionRunnable implements Runnable {
        private final ActiveMQServerMonitor monitor;
        private boolean sentFailedEmail;

        private HeartbeatConnectionRunnable(ActiveMQServerMonitor monitor) {
            this.monitor = monitor;
        }

        @Override
        public void run() {
            try {
                if (monitor.sendConnectionHeartbeat()) {
                    staticLog.info("Sent heartbeat with new connection");
                    if (sentFailedEmail) {
                        monitor.sendReconnectEmail();
                        sentFailedEmail = false;
                    }
                } else {
                    staticLog.error("Failed to send heartbeat with new connection");
                    if (!sentFailedEmail) {
                        monitor.sendFailedConnectionEmail(monitor.getBrokerUrl());
                        sentFailedEmail = true;
                    }
                }
                logHeartbeatRoundTrip(monitor);
            } catch (Exception e) {
                staticLog.error("Caught exception on scheduled thread", e);
            }
        }

        private void logHeartbeatRoundTrip(final ActiveMQServerMonitor monitor) {
            // This will be null on the startup sequence, as we have not received
            // before we send.
            Date lastReceived = monitor.getHeartbeatReceivedTime();
            if (lastReceived != null) {
                Date now = new Date();
                if (now.getTime() - (monitor.getHeartbeatInterval() * 2 * 1000) > lastReceived.getTime()) {
                    staticLog.warn("Heartbeat is late");
                }
            }
        }
    }

    private static final class HeartbeatSessionRunnable implements Runnable {
        private final ActiveMQServerMonitor monitor;

        private HeartbeatSessionRunnable(ActiveMQServerMonitor monitor) {
            this.monitor = monitor;
        }

        @Override
        public void run() {
            try {
                if (monitor.sendHeartbeat(true)) {
                    staticLog.info("Sent heartbeat with new session");
                } else {
                    staticLog.error("Failed to send heartbeat with new session");
                }
                logHeartbeatRoundTrip(monitor);
            } catch (Exception e) {
                staticLog.error("Caught exception on scheduled thread", e);
            }
        }

        private void logHeartbeatRoundTrip(final ActiveMQServerMonitor monitor) {
            // This will be null on the startup sequence, as we have not received
            // before we send.
            Date lastReceived = monitor.getHeartbeatReceivedTime();
            if (lastReceived != null) {
                Date now = new Date();
                if (now.getTime() - (monitor.getHeartbeatInterval() * 2 * 1000) > lastReceived.getTime()) {
                    staticLog.warn("Heartbeat is late");
                }
            }
        }
    }

    private static final class HeartbeatRunnable implements Runnable {
        private final ActiveMQServerMonitor monitor;

        private HeartbeatRunnable(ActiveMQServerMonitor monitor) {
            this.monitor = monitor;
        }

        @Override
        public void run() {
            try {
                if (monitor.sendHeartbeat()) {
                    staticLog.info("Sent heartbeat");
                } else {
                    staticLog.error("Failed to send heartbeat");
                }
                logHeartbeatRoundTrip(monitor);
            } catch (Exception e) {
                staticLog.error("Caught exception on scheduled thread", e);
            }
        }

        private void logHeartbeatRoundTrip(final ActiveMQServerMonitor monitor) {
            // This will be null on the startup sequence, as we have not received
            // before we send.
            Date lastReceived = monitor.getHeartbeatReceivedTime();
            if (lastReceived != null) {
                Date now = new Date();
                if (now.getTime() - (monitor.getHeartbeatInterval() * 2 * 1000) > lastReceived.getTime()) {
                    staticLog.warn("Heartbeat is late");
                }
            }
        }
    }

    static final Logger staticLog = LoggerFactory.getLogger(ServerMonitor.class);

    static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static boolean running;

    /**
     * @param args
     */
    public static void main(String[] args) {

        Properties props = loadProperties("server_monitor");

        staticLog.info("Starting ServerMonitor - " + props.getProperty("brokerurl"));
        running = true;

        final ActiveMQServerMonitor monitor = new ActiveMQServerMonitor(props);
        monitor.startupWaitingForConnection();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                jvmIsStopping(monitor);
            }
        });

        scheduler.scheduleAtFixedRate(new HeartbeatRunnable(monitor), 1, monitor.getHeartbeatInterval(),
                TimeUnit.SECONDS);

        scheduler.scheduleAtFixedRate(new HeartbeatSessionRunnable(monitor), 5, 5, TimeUnit.MINUTES);

        scheduler.scheduleAtFixedRate(new HeartbeatConnectionRunnable(monitor), 5, 5, TimeUnit.MINUTES);

        runApplication();
    }

    private static void runApplication() {
        while (running) {
            Monitoring.sleep(1000);
        }
    }

    protected static void jvmIsStopping(ActiveMQServerMonitor monitor) {
        running = false;
        monitor.sendShutdownEmail();
    }

    private static Properties loadProperties(String basePropertFileName) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Properties props = new Properties();

        try {
            props.load(cl.getResourceAsStream(basePropertFileName + ".properties"));
        } catch (IOException e) {
            staticLog.error("Unable to load properties for " + basePropertFileName, e);
            throw new ActiveMQMonitorException("Unable to load properties for " + basePropertFileName, e);
        }

        return props;
    }

}
