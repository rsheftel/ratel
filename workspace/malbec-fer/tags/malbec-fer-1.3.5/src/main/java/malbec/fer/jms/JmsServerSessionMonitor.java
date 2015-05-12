/**
 * 
 */
package malbec.fer.jms;

import javax.jms.JMSException;

class JmsServerSessionMonitor implements Runnable {
    private final JmsServerSession jmsSession;
    private boolean started;

    JmsServerSessionMonitor(JmsServerSession jmsSession) {
        this.jmsSession = jmsSession;
    }

    @Override
    public synchronized void run() {
        if (!started) {
            synchronized (jmsSession) {
                try {
                    jmsSession.connect();
                    started = true;
                } catch (JMSException e) {
                    jmsSession.getLogger().error("Unable to start JmsSession", e);
                } catch (Exception e) {
                    jmsSession.getLogger().error("Unexpected error when starting JmsSession", e);
                }
            }
        }
    }
}