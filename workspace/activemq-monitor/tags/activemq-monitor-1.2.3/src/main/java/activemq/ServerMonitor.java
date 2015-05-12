package activemq;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.ActiveMQMonitorException;
import util.Monitoring;
import activemq.jmx.JmxMonitor;

public class ServerMonitor {

    static final Logger staticLog = LoggerFactory.getLogger(ServerMonitor.class);

    static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

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

        final JmxMonitor jmxMonitor = new JmxMonitor(props.getProperty("jmxurl"));
        jmxMonitor.startup();

        setShutdownHook(monitor);

        scheduler.scheduleAtFixedRate(new HeartbeatRunnable(monitor), 1, monitor.getHeartbeatInterval(),
                TimeUnit.SECONDS);

        scheduler.scheduleAtFixedRate(new HeartbeatSessionRunnable(monitor), 5, 5, TimeUnit.MINUTES);

        scheduler.scheduleAtFixedRate(new HeartbeatConnectionRunnable(monitor), 5, 5, TimeUnit.MINUTES);

        scheduler.scheduleAtFixedRate(new HeapMonitor(monitor, jmxMonitor, props
                .getProperty("memory.threshold.percent"), props
                .getProperty("memory.email.interval")), 1, 1, TimeUnit.MINUTES);

        scheduler.scheduleAtFixedRate(new ThreadMonitor(monitor, jmxMonitor, props
                .getProperty("thread.threshold"), props
                .getProperty("thread.email.interval")), 1, 1, TimeUnit.MINUTES);

        runApplication();
    }

    private static void setShutdownHook(final ActiveMQServerMonitor monitor) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                jvmIsStopping(monitor);
            }
        });
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
