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

        scheduler.scheduleAtFixedRate(new Runnable() {
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
        }, 1, monitor.getHeartbeatInterval(), TimeUnit.SECONDS);

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
