package activemq.monitors;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import activemq.ActiveMQServerMonitor;
import activemq.jmx.JmxMonitor;

public abstract class AbstractJmxMonitor implements Runnable {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    
    protected final JmxMonitor jmxMonitor;
    protected final ActiveMQServerMonitor monitor;
    protected DateTime sentEmailAt;
    protected int emailInterval = 30;
    protected double thresholdPercent;
    
    protected AbstractJmxMonitor(ActiveMQServerMonitor monitor, JmxMonitor jmxMonitor) {
        this.monitor = monitor;
        this.jmxMonitor = jmxMonitor;
    }

    protected void reconnect() {
        try {
            jmxMonitor.shutdown();
            jmxMonitor.startup();
        } catch (Exception e) {
            log.error("Unable to reconnect to JMX", e);   
        }
    }

    protected boolean isConnected() {
        return jmxMonitor.isConnected();
    }

}
