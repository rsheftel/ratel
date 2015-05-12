/**
 * 
 */
package malbec.jms;

import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import malbec.util.MessageUtil;

public class DefaultTextMessageProcessor implements ITextMessageProcessor {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    public DefaultTextMessageProcessor() {}

    @Override
    public void onTextMessage(TextMessage textMessage) {
        try {
            Map<String, String> mapMessage = MessageUtil.extractRecord(textMessage.getText());
            Destination dest = textMessage.getJMSDestination();
            if (dest instanceof Topic) {
                Topic topic = (Topic) dest;
                mapMessage.put("JMSTOPIC", topic.getTopicName());
            } else {
                mapMessage.put("JMSTOPIC", dest.toString());
            }
            MessageUtil.setPublishTimestamp(textMessage.getJMSTimestamp(), mapMessage);
            onTextMessage(textMessage, mapMessage);
        } catch (JMSException e) {
            log.error("Unable to process JMS message", e);
        }
    }

    protected void onTextMessage(TextMessage textMessage, Map<String, String> mapMessage) {
        log.debug("Default implmentation: "+ mapMessage);
    }
}