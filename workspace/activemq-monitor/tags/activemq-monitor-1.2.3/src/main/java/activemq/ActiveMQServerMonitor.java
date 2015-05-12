package activemq;

import static activemq.ConnectionEventType.Disconnected;

import java.lang.management.MemoryUsage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import activemq.jmx.ThreadCountInfo;

import util.EmailSender;
import util.IObserver;
import util.MessageUtil;
import util.Monitoring;

public class ActiveMQServerMonitor extends AbstractJmsMonitor<IJmsObserver<Message>, Message> implements
        MessageListener, IObserver<ConnectionEvent> {

    private static final String HEARTBEAT_TOPIC = "ActiveMQ.Heartbeat";
    private static final String HEARTBEAT_FIELD_MESSAGE = "Message";
    private static final String HEARTBEAT_FIELD_TIMESTAMP = "Timestamp";

    static final String HEARTBEAT_TEXT = "I am alive!";

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public final Logger log = LoggerFactory.getLogger(getClass().getName());

    private Map<String, String> lastHeartbeat;
    private Date heartbeatReceivedTime;
    private Exception heartbeatException;
    private ConnectionEvent lastConnectionEvent;

    private Properties emailSettings;

    private String brokerUrl;
    
    private int heartbeatInterval = 30;

    public ActiveMQServerMonitor(String brokerUrl) {
        connectionTask = new ConnectionTask(brokerUrl);
        this.brokerUrl = brokerUrl;
    }

    public ActiveMQServerMonitor(ConnectionTask connection) {
        this.connectionTask = connection;
    }

    public ActiveMQServerMonitor(Properties props) {
        this(props.getProperty("brokerurl"));

        emailSettings = copyProperties(props, "mail");
        heartbeatInterval = intValue(props.getProperty("heartbeat.interval"), 30);
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public Exception getLastHeartbeatException() {
        return heartbeatException;
    }

    private Properties copyProperties(Properties props, String regex) {
        Properties copy = new Properties();

        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            if (entry.getKey().toString().startsWith(regex)) {
                copy.setProperty(entry.getKey().toString(), entry.getValue().toString());
            }
        }

        return copy;
    }

    public Properties getEmailSettings() {
        return emailSettings;
    }

    public void setEmailSettings(Properties emailSettings) {
        this.emailSettings = emailSettings;
    }

    public synchronized Map<String, String> getLastHeartbeat() {
        // Return a copy of the map
        if (lastHeartbeat == null) {
            return null;
        } else {
            return new HashMap<String, String>(lastHeartbeat);
        }
    }

    synchronized void setLastHeartbeat(Map<String, String> lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
        heartbeatException = null;
        heartbeatReceivedTime = new Date();

    }

    @Override
    public void startup() {
        connectionTask.addObserver(this);
        connectionTask.connectAsTask();
    }

    public void startupWaitingForConnection() {
        startup();

        while (!connectionTask.isConnected()) {
            Monitoring.sleep(100);
        }
    }

    @Override
    public void shutdown() {
        connectionTask.removeObserver(this);
        super.shutdown();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            try {
                // We assume that all text messages on this topic are heartbeats;
                TextMessage textMessage = (TextMessage) message;
                Map<String, String> hbMessage = MessageUtil.extractRecord(textMessage.getText());
                log.info("Received heartbeat");
                setLastHeartbeat(hbMessage);
            } catch (JMSException e) {
                log.error("Unable to parse heartbeat", e);
                heartbeatException = e;
            }
        } else {
            // If we get other types of messages, we will need to enhance this
            log.warn("Ignoring message type: " + message.getClass());
        }
    }

    public boolean sendHeartbeat() {
        return sendHeartbeat(false);
    }

    public boolean sendHeartbeat(boolean useNewSession) {
        Map<String, String> heartbeat = new HashMap<String, String>();
        heartbeat.put(HEARTBEAT_FIELD_MESSAGE, HEARTBEAT_TEXT);
        heartbeat.put(HEARTBEAT_FIELD_TIMESTAMP, sdf.format(new Date()));
        MessageUtil.setPublishTimestamp(new Date(), heartbeat);
        MessageUtil.setTopicName(HEARTBEAT_TOPIC, heartbeat);

        Topic topic = null;
        try {
            if (useNewSession) {
                topic = publishWithNewSession(HEARTBEAT_TOPIC, MessageUtil.createRecord(heartbeat));
            } else {
                topic = publish(HEARTBEAT_TOPIC, MessageUtil.createRecord(heartbeat));
            }
        } catch (JMSException e) {
            getLogger().error("Unable to publish heartbeat message", e);
        }

        return topic != null;
    }

    public boolean sendConnectionHeartbeat() {
        Map<String, String> heartbeat = new HashMap<String, String>();
        heartbeat.put(HEARTBEAT_FIELD_MESSAGE, HEARTBEAT_TEXT);
        heartbeat.put(HEARTBEAT_FIELD_TIMESTAMP, sdf.format(new Date()));
        MessageUtil.setPublishTimestamp(new Date(), heartbeat);
        MessageUtil.setTopicName(HEARTBEAT_TOPIC, heartbeat);

        Topic topic = null;
        try {
            topic = publishWithNewConnection(brokerUrl, HEARTBEAT_TOPIC, MessageUtil.createRecord(heartbeat));
        } catch (Exception e) {
            getLogger().error("Unable to publish heartbeat message", e);
        }

        return topic != null;
    }

    public String getHeartbeatMessage() {
        Map<String, String> heartbeat = getLastHeartbeat();
        if (heartbeat != null) {
            return heartbeat.get(HEARTBEAT_FIELD_MESSAGE);
        } else {
            return null;
        }
    }

    /**
     * Return the current heartbeat message record.
     * 
     * This might be redundant for the lastHeartbeat, but we can decide here if we want to return null or an
     * empty Map
     * 
     * @return
     */
    public Map<String, String> getHeartbeatRecord() {
        return getLastHeartbeat();
    }

    public synchronized Date getHeartbeatReceivedTime() {
        if (heartbeatReceivedTime != null) {
            return new Date(heartbeatReceivedTime.getTime());
        } else {
            return null;
        }
    }

    @Override
    public void onUpdate(ConnectionEvent connectionEvent) {
        final ConnectionEventType type = connectionEvent.getEventType();
        switch (type) {
            case Disconnected:
                lastConnectionEvent = connectionEvent;
                sendDisconnectEmail(connectionEvent);
                break;
            case Stopped:
                lastConnectionEvent = connectionEvent;
                break;
            case Connected:
                try {
                    Session session = connectionTask.getSession();
                    Destination heartbeatDest = session.createTopic(HEARTBEAT_TOPIC);

                    MessageConsumer heartbeatConsumer = session.createConsumer(heartbeatDest);
                    heartbeatConsumer.setMessageListener(this);

                } catch (JMSException e) {
                    log.error("Unable to startup monitor", e);
                    throw new JmsException("Unable to startup monitor", e);
                }
                if (lastConnectionEvent != null && lastConnectionEvent.getEventType() == Disconnected) {
                    sendReconnectEmail();
                }
                lastConnectionEvent = connectionEvent;
                break;
            default: // Do nothing
                break;
        }

    }

    boolean sendReconnectEmail() {
        EmailSender emailSender = new EmailSender(emailSettings);
        StringBuilder sb = new StringBuilder(128);

        sb.append("Monitor has reconnected to ActiveMQ.\n");

        return emailSender.sendMessage("ActiveMQ Monitor Reconnected", sb.toString());
    }

    boolean sendDisconnectEmail(ConnectionEvent connectionEvent) {
        EmailSender emailSender = new EmailSender(emailSettings);
        StringBuilder sb = new StringBuilder(128);

        sb.append("Monitor Disconnected from server.\n");
        sb.append(connectionEvent.getEventType()).append("\n");
        sb.append(connectionEvent.getMessage()).append("\n");
        if (connectionEvent.getException() != null) {
            sb.append(connectionEvent.getException().getMessage());
        }

        return emailSender.sendMessage("ActiveMQ Monitor Lost Connection", sb.toString());
    }

    boolean sendFailedConnectionEmail(String brokerUrl) {
        EmailSender emailSender = new EmailSender(emailSettings);
        StringBuilder sb = new StringBuilder(128);

        sb.append("Failed to create new connection to server.\n");
        sb.append("Tried to send heartbeat with new connection, unable to connect");

        return emailSender.sendMessage("ActiveMQ Monitor New Connection Failed", sb.toString());
    }

    
    boolean sendLowMemoryEmail(MemoryUsage memoryUsage, double thresholdPercent) {
        EmailSender emailSender = new EmailSender(emailSettings);
        StringBuilder sb = new StringBuilder(128);

        sb.append("ActiveMQ Memory is low.\n");
        sb.append(memoryUsage.toString());
        long percentUsed = memoryUsage.getUsed() * 100 / memoryUsage.getMax();
        sb.append("\nWe are at " ).append(percentUsed).append("% used - limit is ");
        sb.append(thresholdPercent).append("%.");
        
        return emailSender.sendMessage("ActiveMQ is low on memory", sb.toString());
        
    }
    
    boolean sendShutdownEmail() {
        EmailSender emailSender = new EmailSender(emailSettings);
        StringBuilder sb = new StringBuilder(128);

        sb.append("Monitor is shutting down.\n");

        return emailSender.sendMessage("ActiveMQ Monitor Is Shutting Down", sb.toString());
    }

    public ConnectionEventType getLastConnectionEvent() {
        if (lastConnectionEvent != null) {
            return lastConnectionEvent.getEventType();
        } else {
            return null;
        }
    }

    private int intValue(String strInt, int defaultValue) {
        try {
            return Integer.parseInt(strInt);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public long getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public boolean sendHighThreadEmail(ThreadCountInfo tci, long threadThreshold) {
        EmailSender emailSender = new EmailSender(emailSettings);
        StringBuilder sb = new StringBuilder(128);

        sb.append("ActiveMQ Thread Count is High.\n");
        sb.append(tci.toString());
        
        sb.append("\nThreshold set to ").append(threadThreshold).append(".");
        
        return emailSender.sendMessage("ActiveMQ has high thread usage.", sb.toString());
        
    }

}
