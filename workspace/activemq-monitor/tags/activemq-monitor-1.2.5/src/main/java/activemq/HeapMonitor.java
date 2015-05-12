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
    private double thresholdPercent;

    public HeapMonitor(ActiveMQServerMonitor monitor, JmxMonitor jmxMonitor, String thresholdPercentStr, String interval) {
        this.monitor = monitor;
        this.jmxMonitor = jmxMonitor;
        // determine the threshold

        long thresholdPercent = 80;
        if (thresholdPercentStr != null && thresholdPercentStr.trim().length() > 0) {
            try {
                thresholdPercent = Long.parseLong(thresholdPercentStr);
            } catch (NumberFormatException e) {
                log.warn("Unable to parse memory threshold level", e);
            }
        }

        this.thresholdPercent = thresholdPercent / 100d;
        
        try {
            this.emailInterval = Integer.parseInt(interval);
        } catch (NumberFormatException e) {
            log.warn("unable to set email interval, using default " + emailInterval, e);
        }
    }

    @Override
    public void run() {
        try {
            if (!jmxMonitor.isConnected()) {
                jmxMonitor.shutdown();
                jmxMonitor.startup();
            }
            MemoryUsage memoryUsage = jmxMonitor.getHeapMemoryUsage();
            log.info(memoryUsage.toString());
            if (thresholdPercent * memoryUsage.getMax() < memoryUsage.getUsed()) {
                log.warn("ActiveMQ memory usage has exceeded limit -- " + (thresholdPercent* 100d)+"%");
                if (sentEmailAt == null || new DateTime().minusMinutes(emailInterval).isAfter(sentEmailAt)) {
                    monitor.sendLowMemoryEmail(memoryUsage, thresholdPercent * 100d);
                    sentEmailAt = new DateTime();
                }
            }
        } catch (Exception e) {
            log.error("Tried to check ActiveMQ heap", e);
            monitor.sendExceptionEmail("HeapMonitor Exception", e);
        }
    }
}
