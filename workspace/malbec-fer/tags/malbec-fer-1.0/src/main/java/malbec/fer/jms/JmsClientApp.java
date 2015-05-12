package malbec.fer.jms;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import malbec.fer.Order;
import malbec.util.MessageUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JMS client represents one connection/session with a broker. The client may have multiple queues that it
 * subscribes/publishes to.
 * 
 * This is not currently used in the application, but is used for testing the server applications and should
 * be a basis for the Excel clients.
 */
public class JmsClientApp extends AbstractJmsApp {

    final private Logger log = LoggerFactory.getLogger(getClass());

//    final private ClientApplication jmsApp;
    
    public JmsClientApp(String sessionName) {
        super(sessionName, new JmsSession(new ClientApplication()));
    }

    public void setConfiguration(Properties props) {
        jmsSession.setBrokerUrl(props.getProperty("jms.brokerurl"));
        jmsSession.setConsumerQueue(props.getProperty("jms.response.queue"));
        jmsSession.setProducerQueue(props.getProperty("jms.command.queue"));
    }

    public String sendOrder(Order order) {
        try {
            Map<String, String> orderMap = order.toMap();
            MessageUtil.setNewOrder(orderMap);
            
            TextMessage textMessage = jmsSession.sendSpecifyResponse(orderMap);

            return textMessage.getJMSMessageID();
        } catch (JMSException e) {
            log.error("Unable to send order", e);
        }

        return null;
    }

    public int getUnprocessedMessageCount() {
        ClientApplication jmsApp = (ClientApplication)jmsSession.getJmsApp();
        return jmsApp.unprocessedMessages.size();
    }
    
    /**
     * find the response that matches the specified message ID
     * 
     * @param jmsMessageID
     * @return
     */
    public Map<String, String> getResponseFor(String jmsMessageID) {
        ClientApplication jmsApp = (ClientApplication)jmsSession.getJmsApp();
        
        return jmsApp.getResponseFor(jmsMessageID);
    }

    public Map<String, String> removeResponseFor(String jmsMessageID) {
        ClientApplication jmsApp = (ClientApplication)jmsSession.getJmsApp();
        return jmsApp.removeResponseFor(jmsMessageID);
    }

    public void dumpUnprocessedMessages() {
        ClientApplication jmsApp = (ClientApplication)jmsSession.getJmsApp();
        jmsApp.dumpUnprocessedMessages();
    }
    
    private static final class ClientApplication implements IJmsApplication {
        final private Logger log = LoggerFactory.getLogger(getClass());
        private Queue<Map<String, String>> unprocessedMessages = new LinkedBlockingQueue<Map<String, String>>();

        @Override
        public void inboundApp(Message message) {
            
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;

                try {
                    // The server is sending back our JMSMessageID as the original message ID.
                    Map<String, String> appMessage = MessageUtil.extractRecord(textMessage.getText());

                    // extract the two string properties and add to the message so we can use them
                    MessageUtil.setOriginalMessageID(textMessage.getStringProperty("JmsOriginalMessageID"),
                            appMessage);
                    MessageUtil.setJmsMessageID(textMessage.getJMSMessageID(), appMessage);
                    
                    log.info("Received message: "+ appMessage);
                    
                    synchronized (unprocessedMessages) {
                        unprocessedMessages.add(appMessage);
                    }

                } catch (JMSException e) {
                    // log.error("Unable to extract text from JMS message", e);
                    e.printStackTrace();
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
            return true;
        }

        public Map<String, String> getResponseFor(String jmsMessageID) {
            return getResponseFor(jmsMessageID, false);

        }

        Map<String, String> getResponseFor(String jmsMessageID, boolean remove) {
            synchronized (unprocessedMessages) {
                Iterator<Map<String, String>> it = unprocessedMessages.iterator();
                while (it.hasNext()) {
                    Map<String, String> record = it.next();
                    String originalMessageID = MessageUtil.getOriginalMessageID(record);
                    if (originalMessageID != null && jmsMessageID.equals(originalMessageID)) {
                        if (remove) {
                            it.remove();
                        }
                        log.info("Found response match: " + record);
                        return record;
                    }
                }
            }
            return null;
        }

        
        public Map<String, String> removeResponseFor(String jmsMessageID) {
            return getResponseFor(jmsMessageID, true);
        }

        public void dumpUnprocessedMessages() {
            synchronized (unprocessedMessages) {
                Iterator<Map<String, String>> it = unprocessedMessages.iterator();
                while (it.hasNext()) {
                    Map<String, String> record = it.next();
                    System.out.println(record);
                }
            }
        }

        @Override
        public boolean sendEmail(String subject, String messageBody) {
            System.err.println(subject);
            System.err.println(messageBody);
            return true;
        }
    }

    public String queryOrder(String clientOrderID) {
        try {
            Map<String, String> queryMap = new HashMap<String, String>();
            MessageUtil.setOrderQuery(queryMap);
            queryMap.put("ClientOrderID", clientOrderID);
            
            TextMessage textMessage = jmsSession.sendSpecifyResponse(queryMap);

            return textMessage.getJMSMessageID();
        } catch (JMSException e) {
            log.error("Unable to send query", e);
        }

        return null;
    }
}
