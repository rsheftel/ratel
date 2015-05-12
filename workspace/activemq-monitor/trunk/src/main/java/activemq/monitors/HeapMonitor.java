package activemq.monitors;

import java.lang.management.MemoryUsage;

import org.joda.time.DateTime;

import activemq.ActiveMQServerMonitor;
import activemq.jmx.JmxMonitor;

public class HeapMonitor extends AbstractJmxMonitor {

    public HeapMonitor(ActiveMQServerMonitor monitor, JmxMonitor jmxMonitor, String thresholdPercentStr, String interval) {
        super(monitor, jmxMonitor);
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
            if (!isConnected()) {
                reconnect();
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
            if (sentEmailAt == null || new DateTime().minusMinutes(emailInterval).isAfter(sentEmailAt)) {
                monitor.sendExceptionEmail("HeapMonitor Exception", e);
                sentEmailAt = new DateTime();
            }

            reconnect();
        }
    }
}
