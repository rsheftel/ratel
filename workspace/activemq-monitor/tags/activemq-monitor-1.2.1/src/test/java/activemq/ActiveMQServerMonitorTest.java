package activemq;

import static activemq.ConnectionEventType.Disconnected;

import java.lang.management.MemoryUsage;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.jms.JMSException;

import org.testng.annotations.Test;

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

        assert monitor.isConnected() : "Monitor is not connected";

        assert monitor.sendHeartbeat() : "Did not sent heartbeat";

        waitForValue(new IWaitFor<Integer>() {
            @Override
            public Integer waitFor() {
                return monitor.getHeartbeatMessage() == null ? 0 : 1;
            }
        }, 1, 1000);

        String message = monitor.getHeartbeatMessage();

        assert "I am alive!".equals(message);
        Map<String, String> record = monitor.getHeartbeatRecord();

        assert record != null : "Heartbeat record not received";
        Date now = new Date();
        Date publishedTime = MessageUtil.getPublishTimestamp(record);

        assert publishedTime.before(now);
        Date receivedTime = monitor.getHeartbeatReceivedTime();

        assert receivedTime.after(publishedTime) : "Received message before it was published";

        long receiveMinus1 = receivedTime.getTime() - 1000;
        assert receiveMinus1 < publishedTime.getTime() : "Message took longer than 1 second to arrive";

        monitor.shutdown();
    }

    @Test(groups = { "unittest" })
    public void testConnectionDown() {
        ConnectionTask connection = new ConnectionTask(BROKER_URL);

        final ActiveMQServerMonitor monitor = new ActiveMQServerMonitor(connection);
        monitor.startupWaitingForConnection();

        assert monitor.isConnected() : "Monitor is not connected";

        connection.stop();
        waitForValue(new IWaitFor<Integer>() {
            @Override
            public Integer waitFor() {
                return monitor.getLastConnectionEvent() == null ? 0 : 1;
            }
        }, 1, 1000);

        assert ConnectionEventType.Stopped == monitor.getLastConnectionEvent() : "Did not receive Stopped event";

        monitor.setEmailSettings(getEmailProperties());
        boolean sentEmail = monitor.sendDisconnectEmail(new ConnectionEvent(Disconnected, new JMSException(
                "Test Disconnect"), "Test email sending"));
        assert sentEmail : "Email not sent";

        monitor.shutdown();
        assert !monitor.isConnected() : "Monitor is not disconnected";
    }

    @Test(groups = { "unittest" })
    public void testMemoryEmail() {
        ConnectionTask connection = new ConnectionTask(BROKER_URL);

        final ActiveMQServerMonitor monitor = new ActiveMQServerMonitor(connection);
        monitor.startupWaitingForConnection();

        assert monitor.isConnected() : "Monitor is not connected";

        monitor.setEmailSettings(getEmailProperties());
        monitor.sendLowMemoryEmail(new MemoryUsage(1000, 2000, 3000, 4000));
    }
    
    private Properties getEmailProperties() {
        Properties props = new Properties();
        props.setProperty("mail.host", "mail.fftw.com");
        props.setProperty("mail.from", "DevTest@fftw.com");
        props.setProperty("mail.to", "mfranz@fftw.com");

        return props;
    }
}
