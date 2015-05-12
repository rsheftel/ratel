package malbec.fer.jms;

import static org.testng.Assert.*;

import javax.jms.JMSException;

import malbec.util.EmailSettings;
import malbec.util.IWaitFor;

import org.testng.annotations.Test;

/**
 * Test the Server JMS Application.
 * 
 */
public class JmsServerTest extends AbstractJmsTest {
    
    @Test(groups = { "unittest" })
    public void testServerStartup() throws JMSException {
        final AbstractJmsApp jc = new JmsServerApp("Test Session", new EmailSettings());

        jc.setConfiguration(createJmsTestProperties());
        jc.start();

        waitForConnected(jc);
      
        assertTrue(jc.isConnected(), "Did not connect to broker");

        jc.stop();
        assertFalse(jc.isRunning(), "Server did not stop");
    }
    
    @Test(groups = { "unittest" })
    public void testReceiveOrder() throws JMSException {
        final AbstractJmsApp jsa = new JmsServerApp("Test Session", new EmailSettings());

        jsa.setConfiguration(createJmsTestProperties());
        jsa.start();

        waitForConnected(jsa);

        assertTrue(jsa.isConnected(), "Did not connect to broker");
        
        // send an order to be processed
        sendValidLimitOrder();

        waitForValue(new IWaitFor<Boolean>() {
            @Override
            public Boolean waitFor() {
                return jsa.getUnprocessedMessageCount() > 0;
            }
        }, true, 2000);

        assertTrue(jsa.getUnprocessedMessageCount() > 0, "Did not receive test order");

        jsa.stop();
    }
}
