package com.fftw.metadb.domain.jms;

import static java.util.Arrays.asList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.fftw.metadb.service.LiveListener;
import com.fftw.metadb.util.MessageUtil;

/**
 * JmsLiveSubscriber Tester.
 */
public class JmsLiveSubscriberTest extends ActiveMQTest {

    private static final String SUBSCRIBER_INSTANCE = "UnitTestInstance";

    private static final List<String> VALID_FIELDS = asList("LastPrice", "LastVolume", "OpenPrice",
            "HighPrice", "LowPrice", "Timestamp");

    @AfterMethod(groups = { "unittest" })
    public void clearSubscribers() {
        System.out.println("ClearSubscribers");
        JmsLiveSubscriber subscriber = JmsLiveSubscriber.getInstance();
        // Ensure that we don't have any subscribers - we may have some
        // depending on the order the tests fire
        subscriber.clearSubscribers();
    }

    @Test(groups = { "unittest" })
    public void testGetInstance() {
        JmsLiveSubscriber subscriber = JmsLiveSubscriber.getInstance();
        assert subscriber != null : "No instance created";
        subscriber.shutdown();
    }

    @Test(groups = { "unittest" })
    public void testSubscribe() throws Exception {
        try {
            JmsLiveSubscriber subscriber = JmsLiveSubscriber.getInstance(SUBSCRIBER_INSTANCE, BROKER_URL);
            LiveListener listener = new LiveListenerImpl();
            subscriber.subscribe("US.1C", listener);

            // Thread.sleep(30000);
            assert subscriber.getSubscriptions().contains("MarketData.US.1C?consumer.retroactive=true") : "Subscription failed";
            subscriber.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
            assert false : "Unexpected exception " + e.getMessage();
        }
    }

    @Test(groups = { "unittest" })
    public void testUnsubscribe() {
        try {
            JmsLiveSubscriber subscriber = JmsLiveSubscriber.getInstance(SUBSCRIBER_INSTANCE, BROKER_URL);
            LiveListener listener = new LiveListenerImpl();

            subscriber.subscribe("AD.1C", listener);

            assert subscriber.getSubscriptions().contains("MarketData.AD.1C?consumer.retroactive=true") : "Subscription failed";
            subscriber.unsubscribe("AD.1C", listener);
            assert !subscriber.getSubscriptions().contains("MarketData.AD.1C") : "unsubscribe failed";
            subscriber.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
            assert false : "Unexpected exception " + e.getMessage();
        }
    }

    @Test(groups = { "unittest" })
    public void testOnMessage() {
        try {
            JmsLiveSubscriber subscriber = JmsLiveSubscriber.getInstance(SUBSCRIBER_INSTANCE, BROKER_URL);
            LiveListener listener = new LiveListenerImpl();

            subscriber.subscribe("TEST.AAPL.1C", listener);
            assert subscriber.getSubscriptions().contains("TestTopic.TEST.AAPL.1C?consumer.retroactive=true") : "Subscription failed";

            String testMessage = MessageUtil.createRecord(buildTestRecord());

            Session session = subscriber.getSession();
            Topic topic = session.createTopic("TestTopic.TEST.AAPL.1C");
           
            TextMessage textMessage = session.createTextMessage(testMessage);
            textMessage.setJMSDestination(topic);
            
            subscriber.onMessage(textMessage);
            subscriber.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
            assert false : "Unexpected exception " + e.getMessage();
        }
    }

    private Map<String, String> buildTestRecord() {
        Map<String, String> dataMap = new HashMap<String, String>();
        dataMap.put("LastPrice", "12.09");
        dataMap.put("LastVolume", "12000");
        dataMap.put("OpenPrice", "11.03");
        dataMap.put("HighPrice", "13.45");
        dataMap.put("LowPrice", "10.56");
        dataMap.put("Timestamp", MessageUtil.formatDate());

        return dataMap;
    }

    private static class LiveListenerImpl implements LiveListener {
        public void onData(Map<String, String> dataMap) {
            for (String fieldName : VALID_FIELDS) {
                if (!dataMap.containsKey(fieldName)) {
                    assert false : "Missing required fieldName '" + fieldName + "'";
                }
            }

        }
    }

}
