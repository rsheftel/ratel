/**
 * 
 */
package malbec.hawkeye;

import java.util.Map;

import javax.jms.TextMessage;

import malbec.jms.DefaultTextMessageProcessor;
import malbec.util.DateTimeUtil;
import malbec.util.MessageUtil;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FerretStatusMessageProcessor extends DefaultTextMessageProcessor {
    private Logger log = LoggerFactory.getLogger(getClass());

    private DateTime ignoreBefore;
    
    public FerretStatusMessageProcessor(DateTime ignoreBeforeThis) {
        ignoreBefore = ignoreBeforeThis;
    }

    @Override
    protected void onTextMessage(TextMessage textMessage, Map<String, String> mapMessage) {
        DateTime publishedDateTime = publishedDateTime(mapMessage);
        
        if (ignoreBefore.isAfter(publishedDateTime)) {
            log.info("Message published before startup, ignoring: "+ mapMessage);
            return;
        }
        
        boolean canProcessMessage = true;
        // ensure that the topic and the date are correct
        LocalDate topicDate = dateFromTopic(mapMessage);
        LocalDate orderDate = orderDateFromMessage(mapMessage);
        LocalDate publishedDate = publishedDateFromJmsMessage(mapMessage);
        String userOrderId = MessageUtil.getUserOrderId(mapMessage);

        if (userOrderId == null) {
            canProcessMessage = false;
            log.error("Received order status without a UserOrderId - cannot verify");
        }

        if (topicDate == null) {
            log.error("Bad topic: " + mapMessage.get("JMSTOPIC"));
        }

        if (orderDate == null) {
            // might be a bad message, or a message without an order date
            log.warn("Message without OrderDate, " + mapMessage);
        }

        if (!publishedDate.equals(topicDate)) {
            log.error("JmsMessage publish date does not match topic date.  " + publishedDate + " -- "
                + topicDate);
            canProcessMessage = false;
        }

        if (canProcessMessage && publishedDate.equals(new LocalDate())) {
            // validate that we should have received this message
            validateStatusMessage(mapMessage);
        } else {
            // We are probably starting up and getting old topics
            log.info("Ignoring message for wrong date.  "+ mapMessage);
        }
    }
    
    protected void validateStatusMessage(Map<String, String> mapMessage) {
        log.debug("Received valid message to check.  " + mapMessage);
    }

    private DateTime publishedDateTime(Map<String, String> statusMessage) {
        DateTime jmsTimestamp = MessageUtil.getPublishTimestamp(statusMessage);

        return jmsTimestamp;
    }
    
    private LocalDate publishedDateFromJmsMessage(Map<String, String> statusMessage) {
        DateTime jmsTimestamp = publishedDateTime(statusMessage);

        if (jmsTimestamp != null) {
            return jmsTimestamp.toLocalDate();
        }

        return null;
    }

    private LocalDate orderDateFromMessage(Map<String, String> statusMessage) {
        return DateTimeUtil.guessDate(MessageUtil.getOrderDate(statusMessage));
    }

    // FER.Order.Response.20090403.T00000
    private LocalDate dateFromTopic(Map<String, String> statusMessage) {
        String jmsTopic = statusMessage.get("JMSTOPIC");
        if (jmsTopic != null) {
            String[] parts = jmsTopic.split("\\.");
            if (parts.length > 3) {
                return DateTimeUtil.guessDate(parts[3]);                
            }
        }
        return null;
    }


}