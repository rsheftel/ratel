package malbec.fer.jms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import malbec.util.EmailSender;
import malbec.util.EmailSettings;
import malbec.util.MessageUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JMS client represents one connection/session with a broker. The client may have multiple queues that it
 * subscribes/publishes to.
 * 
 */
public class JmsServerApp extends AbstractJmsApp {

    final private Logger log = LoggerFactory.getLogger(getClass());

    final EmailSettings emailSettings; 
    
    public JmsServerApp(String sessionName, EmailSettings emailSettings) {
        super(sessionName, new JmsSession(new ServerApplication(emailSettings)));
        this.emailSettings = emailSettings;

    }

    public JmsServerApp(String sessionName, Properties config) {
        this(sessionName, new EmailSettings(config));
        setConfiguration(config);
    }

    public void setConfiguration(Properties props) {
        jmsSession.setBrokerUrl(props.getProperty("jms.brokerurl"));
        ((JmsSession)jmsSession).setConsumerQueue(props.getProperty("jms.command.queue"));
        ((JmsSession)jmsSession).setProducerQueue(props.getProperty("jms.response.queue"));
    }

    public int getUnprocessedMessageCount() {
        ServerApplication jmsApp = (ServerApplication)((JmsSession)jmsSession).getJmsApp();
        return jmsApp.unprocessedMessages.size();
    }

    public Map<String, String> getNextMessage() {
        ServerApplication jmsApp = (ServerApplication)((JmsSession)jmsSession).getJmsApp();
        return jmsApp.unprocessedMessages.poll();
    }

    public boolean publishResponse(Map<String, String> message, List<String> errors) {
        try {
            ((JmsSession)jmsSession).publishResponse(message, errors);
            
            return true;
        } catch (JMSException e) {
            log.error("Unable to send JMS Response for " + message, e);
        }
        return false;
    }

    public boolean publishResponse(Map<String, String> message, String error) {
        List<String> errors = new ArrayList<String>();
        errors.add(error);
        return publishResponse(message, errors);
    }

    public boolean publishResponse(Map<String, String> message) {
        return publishResponse(message, Collections.<String> emptyList());
    }
    static final class ServerApplication implements IJmsApplication {

        Queue<Map<String, String>> unprocessedMessages = new LinkedBlockingQueue<Map<String, String>>();

        private Logger log = LoggerFactory.getLogger(getClass());

        private EmailSettings emailSettings;
        
        public ServerApplication(EmailSettings emailSettings) {
            this.emailSettings = emailSettings;
        }

        @Override
        public void inboundApp(Message message) {

            log.info("Received message");
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;

                // The client is populating the 'ReplyTo' string property
                try {
                    String messageBody = textMessage.getText();
                    log.info("Received message: " + messageBody);
                    Map<String, String> orderRecord = MessageUtil.extractRecord(messageBody);

                    // Extract the two pieces of information we need later
                    MessageUtil.setReplyTo(textMessage.getStringProperty("ReplyTo"), orderRecord);
                    MessageUtil.setOriginalMessageID(textMessage.getJMSMessageID(), orderRecord);

                    MessageUtil.setPublishTimestamp(textMessage.getJMSTimestamp(), orderRecord);
                    log.debug("Message with additional fields : " + orderRecord);

                    if (!unprocessedMessages.offer(orderRecord)) {
                        log.error("Unable to add order request to queue:" + orderRecord);
                    }
                } catch (JMSException e) {
                    log.error("Unable to extract text from JMS message", e);
                }
            }
        }

        @Override
        public void outboundApp(Message message) {
            // create JMS message as a response
            // jmsSession.publishResponse(message);
        }

        @Override
        public boolean filterConsumers() {
            return false;
        }

        @Override
        public boolean sendEmail(String subject, String messageBody) {
            EmailSender emailSender = new EmailSender(emailSettings.getAsProperties());
            return emailSender.sendMessage(subject, messageBody);
        }
    }
    @Override
    public int getReceivedMessageCount() {
        if (log != null) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return 0;
    }
}
