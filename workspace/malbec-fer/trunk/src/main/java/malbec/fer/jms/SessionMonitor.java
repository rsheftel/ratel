/**
 * 
 */
package malbec.fer.jms;

import javax.jms.JMSException;

class SessionMonitor implements Runnable {
    private final JmsSession jmsSession;
    private boolean started;

    SessionMonitor(JmsSession jmsSession) {
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