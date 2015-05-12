package malbec.fer.jms;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import javax.jms.JMSException;
import javax.jms.Message;

import org.testng.annotations.Test;

public class JmsSessionTest extends AbstractJmsTest {

    private final static class TestApplication implements IJmsApplication {
        @Override
        public boolean filterConsumers() {
          return true;
        }

        @Override
        public void inboundApp(Message message) {
            if (true) {
               throw new UnsupportedOperationException("Implement me!");
            }
            
        }

        @Override
        public void outboundApp(Message message) {
            if (true) {
               throw new UnsupportedOperationException("Implement me!");
            }
            
        }

        @Override
        public boolean sendEmail(String subject, String messageBody) {
            if (true) {
               throw new UnsupportedOperationException("Implement me!");
            }
            return false;
        }
    }

    @Test(groups = { "unittest" })
    public void testStartup() throws JMSException {
        final JmsSession jmsSession = new JmsSession(BROKER_URL, new TestApplication());

        jmsSession.setConsumerQueue("FER.response");
        jmsSession.setProducerQueue("FER.command");

        jmsSession.start();
        waitForConnected(jmsSession);
        assertTrue(jmsSession.isConnected(), "Did not connect to broker");

        jmsSession.stop();
        waitForDisconnect(jmsSession);
        assertFalse(jmsSession.isConnected(), "Client did not stop");
    }
}
