package com.fftw.metadb.domain.jms;

import com.fftw.metadb.service.LiveListener;
import com.fftw.metadb.util.TextUtil;
import com.fftw.util.PropertyLoader;
import org.apache.activemq.broker.BrokerService;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.jms.Session;
import javax.jms.TextMessage;
import static java.util.Arrays.asList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

/**
 * JmsLiveSubscriber Tester.
 */
public class JmsLiveSubscriberTest {

    private static final List<String> VALID_FIELDS = asList("LastPrice", "LastVolume", "OpenPrice", "HighPrice", "LowPrice", "Timestamp");

    @BeforeTest(groups =
            {
                    "unittest"
                    })
    public void setUp() throws Exception {
        try {
            // we need to have ActiveMQ running within the JVM for testing
            PropertyLoader.setPropertyFile(getClass().getClassLoader().getResource("default.conf").getFile());
            String brokerUrl = PropertyLoader.getProperty("brokerUrl");
            BrokerService broker = new BrokerService();
            broker.setUseJmx(false);
            broker.setBrokerName("UnitTest");
            // configure the broker
            broker.addConnector(brokerUrl);
            broker.start();
        } catch (IOException e) {
            // assume that we are using an existing broker
        }
    }

    @AfterMethod(groups =
            {
                    "unittest"
                    })
    public void clearSubscribers() {
        System.out.println("ClearSubscribers");
        JmsLiveSubscriber subscriber = JmsLiveSubscriber.getInstance();
        LiveListener listener = new LiveListenerImpl();
        // Ensure that we don't have any subscribers - we may have some
        // depending on the order the tests fire
        subscriber.clearSubscribers();
    }

    @Test(groups =
            {
                    "unittest"
                    })
    public void testGetInstance() {
        JmsLiveSubscriber subscriber = JmsLiveSubscriber.getInstance();
        assert subscriber != null : "No instance created";
    }

    @Test(groups =
            {
                    "unittest"
                    })
    public void testSubscribe() throws Exception {
        try {
            JmsLiveSubscriber subscriber = JmsLiveSubscriber.getInstance();
            LiveListener listener = new LiveListenerImpl();
            subscriber.subscribe("US.1C", listener);

            //Thread.sleep(30000);
            assert subscriber.getSubscriptions().contains("MarketData.US.1C") : "Subscription failed";
        } catch (Exception e) {
            e.printStackTrace();
            assert false : "Unexpected exception " + e.getMessage();
        }
    }

    @Test(groups =
            {
                    "unittest"
                    })
    public void testUnsubscribe() {
        try {
            JmsLiveSubscriber subscriber = JmsLiveSubscriber.getInstance();
            LiveListener listener = new LiveListenerImpl();

            subscriber.subscribe("AD.1C", listener);

            assert subscriber.getSubscriptions().contains("MarketData.AD.1C") : "Subscription failed";
            subscriber.unsubscribe("AD.1C", listener);
            assert !subscriber.getSubscriptions().contains("MarketData.AD.1C") : "unsubscribe failed";
        } catch (Exception e) {
            e.printStackTrace();
            assert false : "Unexpected exception " + e.getMessage();
        }
    }

    @Test(groups =
            {
                    "unittest"
                    })
    public void testOnMessage() {
        try {
            JmsLiveSubscriber subscriber = JmsLiveSubscriber.getInstance();
            LiveListener listener = new LiveListenerImpl();

            subscriber.subscribe("AD.1C", listener);
            assert subscriber.getSubscriptions().contains("MarketData.AD.1C") : "Subscription failed";

            String testMessage = TextUtil.createRecord(buildTestRecord());

            Session session = subscriber.getSession();
            TextMessage textMessage = session.createTextMessage(testMessage);
            subscriber.onMessage(textMessage);
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
        dataMap.put("Timestamp", TextUtil.formatDate());

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
