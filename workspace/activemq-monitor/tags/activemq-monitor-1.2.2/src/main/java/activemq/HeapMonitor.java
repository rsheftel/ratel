package activemq;

import java.lang.management.MemoryUsage;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import activemq.jmx.JmxMonitor;

public class HeapMonitor implements Runnable {

    final Logger log = LoggerFactory.getLogger(getClass());

    private final JmxMonitor jmxMonitor;
    private final ActiveMQServerMonitor monitor;

    private DateTime sentEmailAt;
    private int emailInterval = 30; // in minutes
    private long threshold;

    public HeapMonitor(ActiveMQServerMonitor monitor, JmxMonitor jmxMonitor, String thresholdStr, String interval) {
        this.monitor = monitor;
        this.jmxMonitor = jmxMonitor;
        // determine the threshold

        long threshold = 64 * 1024 * 1024;
        if (thresholdStr != null && thresholdStr.trim().length() > 0) {
            thresholdStr = thresholdStr.toLowerCase();
            int sizeStr = thresholdStr.indexOf("m");
            if (sizeStr > -1) {
                thresholdStr = thresholdStr.substring(0, sizeStr);
            }
            try {
                threshold = Long.parseLong(thresholdStr);
            } catch (NumberFormatException e) {
                log.warn("Unable to parse memory threshold level", e);
            }
            if (sizeStr > 0) {
                threshold = threshold * 1024 * 1024;
            }
        }

        this.threshold = threshold;
        
        try {
            this.emailInterval = Integer.parseInt(interval);
        } catch (NumberFormatException e) {
            log.warn("unable to set email interval, using default " + emailInterval, e);
        }
    }

    @Override
    public void run() {
        if (!jmxMonitor.isConnected()) {
            jmxMonitor.shutdown();
            jmxMonitor.startup();
        }
        MemoryUsage memoryUsage = jmxMonitor.getHeapMemoryUsage();
        log.info(memoryUsage.toString());
        if (threshold < memoryUsage.getUsed()) {
            log.warn("ActiveMQ memory usage has exceeded limit.");
            if (sentEmailAt == null || new DateTime().minusMinutes(emailInterval).isAfter(sentEmailAt)) {
                monitor.sendLowMemoryEmail(memoryUsage);
                sentEmailAt = new DateTime();
            }
        }

    }

}
