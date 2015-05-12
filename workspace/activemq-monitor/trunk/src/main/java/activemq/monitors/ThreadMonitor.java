package activemq.monitors;

import org.joda.time.DateTime;

import activemq.ActiveMQServerMonitor;
import activemq.jmx.JmxMonitor;
import activemq.jmx.ThreadCountInfo;

public class ThreadMonitor extends AbstractJmxMonitor {

    private long threadThreshold;

    public ThreadMonitor(ActiveMQServerMonitor monitor, JmxMonitor jmxMonitor, String thresholdStr, String interval) {
       super(monitor, jmxMonitor);
        // determine the threshold

        long threshold = 80;
        if (thresholdStr != null && thresholdStr.trim().length() > 0) {
            try {
                threshold = Long.parseLong(thresholdStr);
            } catch (NumberFormatException e) {
                log.warn("Unable to parse thread threshold level", e);
            }
        }

        this.threadThreshold = threshold;
        
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
            ThreadCountInfo tci = jmxMonitor.getThreadCountInfo();
            log.info(tci.toString());
            if (threadThreshold  < tci.threadCount) {
                log.warn("ActiveMQ thread usage has exceeded limit - "+ threadThreshold);
                if (sentEmailAt == null || new DateTime().minusMinutes(emailInterval).isAfter(sentEmailAt)) {
                    monitor.sendHighThreadEmail(tci, threadThreshold);
                    sentEmailAt = new DateTime();
                }
            }
        } catch (Exception e) {
            log.error("Tried to check ActiveMQ thread count", e);
            if (sentEmailAt == null || new DateTime().minusMinutes(emailInterval).isAfter(sentEmailAt)) {
                monitor.sendExceptionEmail("ThreadMonitor Exception", e);
                sentEmailAt = new DateTime();
            }
            reconnect();
        }
    }
}
