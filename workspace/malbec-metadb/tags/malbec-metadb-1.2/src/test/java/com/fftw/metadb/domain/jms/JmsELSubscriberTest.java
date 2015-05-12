package com.fftw.metadb.domain.jms;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.fftw.metadb.util.MessageUtil;

public class JmsELSubscriberTest extends ActiveMQTest {

    private static final Map<String, String> testMessage = new HashMap<String, String>();

    @Test(groups = { "unittest" })
    public void testGet() {
        
        try {
            String emptyValue = JmsELSubscriber.getString("TestTopic", "testField", false);
            
            assert emptyValue == null : "Received message when no message expected";
            testMessage.put("testField", "TestValue");
            
            publish("TestTopic", MessageUtil.createRecord(testMessage));
            
            String value = JmsELSubscriber.getString("TestTopic", "testField");
            assert value != null : "Failed to receive message";
            assert "TestValue".equals(value) : "Received wrong value";
            
        } catch (Exception e) {
            assert false : "Exception while testing get";
        }
        
        
    }
}
