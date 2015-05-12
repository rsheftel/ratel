package activemq.jms;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import activemq.IJmsObserver;

public class QueueMonitor extends AbstractJmsMonitor<IJmsObserver<Message>, Message> {

    public final Logger log = LoggerFactory.getLogger(getClass());
    
    public QueueMonitor(String brokerUrl) {
       super(brokerUrl);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @SuppressWarnings("unchecked")
    public List<Message> browserMessages(String queueName) throws JMSException {
        Session jmsSession = connectionTask.getSession();
        
        QueueBrowser browser = jmsSession.createBrowser(jmsSession.createQueue(queueName));
        
        Enumeration<Message> messages = browser.getEnumeration();
        
        List<Message> messageList = new ArrayList<Message>();
        
        while (messages.hasMoreElements()) {
            messageList.add(messages.nextElement());
        }

        browser.close();
        
        return messageList;
    }

}
