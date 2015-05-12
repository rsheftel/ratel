package activemq;

import static activemq.ConnectionEventType.Disconnected;
import static org.testng.Assert.*;

import java.lang.management.MemoryUsage;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.jms.JMSException;

import org.testng.annotations.Test;

import activemq.jms.ConnectionTask;

import util.MessageUtil;

public class ActiveMQServerMonitorTest extends ActiveMQTest {

    @Test(groups = { "unittest" })
    public void testHeartbeat() {

        final ActiveMQServerMonitor monitor = new ActiveMQServerMonitor(BROKER_URL);
        monitor.startup();

        waitForValue(new IWaitFor<Boolean>() {
            @Override
            public Boolean waitFor() {
                return monitor.isConnected();
            }
        }, true, 1000);

        assertTrue(monitor.isConnected(), "Monitor is not connected");

        assertTrue(monitor.sendHeartbeat(), "Did not sent heartbeat");

        waitForValue(new IWaitFor<Integer>() {
            @Override
            public Integer waitFor() {
                return monitor.getHeartbeatMessage() == null ? 0 : 1;
            }
        }, 1, 1000);

        String message = monitor.getHeartbeatMessage();

        assertEquals(message, "I am alive!");
        Map<String, String> record = monitor.getHeartbeatRecord();

        assertNotNull(record, "Heartbeat record not received");
        Date now = new Date();
        Date publishedTime = MessageUtil.getPublishTimestamp(record);

        assertTrue(publishedTime.before(now));
        Date receivedTime = monitor.getHeartbeatReceivedTime();

        assertTrue(receivedTime.after(publishedTime), "Received message before it was published");

        long receiveMinus1 = receivedTime.getTime() - 1000;
        assertTrue(receiveMinus1 < publishedTime.getTime(), "Message took longer than 1 second to arrive");

        monitor.shutdown();
    }

    @Test(groups = { "unittest" })
    public void testConnectionDown() {
        ConnectionTask connection = new ConnectionTask(BROKER_URL);

        final ActiveMQServerMonitor monitor = new ActiveMQServerMonitor(connection);
        monitor.startupWaitingForConnection();

        assertTrue(monitor.isConnected(), "Monitor is not connected");

        connection.stop();
        waitForValue(new IWaitFor<Integer>() {
            @Override
            public Integer waitFor() {
                return monitor.getLastConnectionEvent() == null ? 0 : 1;
            }
        }, 1, 1000);

        assertEquals(ConnectionEventType.Stopped, monitor.getLastConnectionEvent(),
                "Did not receive Stopped event");

        monitor.setEmailSettings(getEmailProperties());
        boolean sentEmail = monitor.sendDisconnectEmail(new ConnectionEvent(Disconnected, new JMSException(
                "Test Disconnect"), "Test email sending"));
        assertTrue(sentEmail, "Email not sent");

        monitor.shutdown();
        assertFalse(monitor.isConnected(), "Monitor is not disconnected");
    }

    @Test(groups = { "unittest" })
    public void testMemoryEmail() {
        ConnectionTask connection = new ConnectionTask(BROKER_URL);

        final ActiveMQServerMonitor monitor = new ActiveMQServerMonitor(connection);
        monitor.startupWaitingForConnection();

        assertTrue(monitor.isConnected(), "Monitor is not connected");

        monitor.setEmailSettings(getEmailProperties());
        monitor.sendLowMemoryEmail(new MemoryUsage(1000, 2000, 3000, 4000), .8d);
        
        connection.stop();
    }

    private Properties getEmailProperties() {
        Properties props = new Properties();
        props.setProperty("mail.host", "mail.fftw.com");
        props.setProperty("mail.from", "DevTest@fftw.com");
        props.setProperty("mail.to", "mfranz@fftw.com");

        return props;
    }
}
