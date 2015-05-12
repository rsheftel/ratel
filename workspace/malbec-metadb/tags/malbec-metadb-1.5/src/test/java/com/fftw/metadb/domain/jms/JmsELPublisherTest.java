package com.fftw.metadb.domain.jms;

import org.testng.annotations.Test;

public class JmsELPublisherTest extends ActiveMQTest {

    @Test(groups = { "unittest" })
    public void testPublish() {
        try {
            int count=0;
            MyMessageListener listener = new MyMessageListener();
            
            attachListener("TestTopic", listener);
            JmsELPublisher.publish("TestTopic", "testField", "TestValue");
            
            while (listener.message == null  && count < 10) {
                localSleep(10);
                count++;
            }

            assert listener.message != null : "Failed to receive message";
            
        } catch (Exception e) {
            assert false : "Failed to publish message";
        }
    }
}
