package activemq;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import activemq.jmx.JmxMonitor;
import activemq.jmx.ThreadCountInfo;

public class ThreadMonitor implements Runnable {
    final Logger log = LoggerFactory.getLogger(getClass());

    private final JmxMonitor jmxMonitor;
    private final ActiveMQServerMonitor monitor;

    private DateTime sentEmailAt;
    private int emailInterval = 30; // in minutes
    private long threadThreshold;

    public ThreadMonitor(ActiveMQServerMonitor monitor, JmxMonitor jmxMonitor, String thresholdStr, String interval) {
        this.monitor = monitor;
        this.jmxMonitor = jmxMonitor;
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
        if (!jmxMonitor.isConnected()) {
            jmxMonitor.shutdown();
            jmxMonitor.startup();
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

    }

}
