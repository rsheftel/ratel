package jms;

import static util.Dates.*;
import static util.Errors.*;
import static util.Log.*;
import static util.Times.*;
import systemdb.data.*;
import util.*;

public class TestChannel extends JMSTestCase {

    private QTopic testTopic;
    @Override public void setUp() throws Exception {
        super.setUp();
        testTopic = new QTopic("test.topic", false);
    }
    
	static class Acknowledgement extends MessageOnlyReceiver {
		private boolean messageReceived;

		@Override public void onMessage(Envelope e) {
			if(e.text().equals("this is a test message"))
				messageReceived = true;
		}
	}
	
	static class SaySomething extends MessageOnlyReceiver {
	    @Override public void onMessage(Envelope e) {
	        info("received message " + e.text());
	    }
	}
	
	static class Bombs extends MessageOnlyReceiver {

	    boolean received;
	    
        @Override public void onMessage(Envelope envelope) {
            received = true;
            bomb("should bomb");
        }
	    
	}

	static class GotFields extends FieldsReceiver {
        private Fields received;
        @Override public void onMessage(Fields fields) {
            received = fields;
        }
	}
	
	private static final long TIMEOUT_MILLIS = 500;
	
	public void testQ() throws Exception {
		Acknowledgement a = new Acknowledgement();
		TEST_QUEUE.register(a);
		try {
			TEST_QUEUE.register(a);
			fail("can't reregister on q! disconnect first...;");
		} catch (Exception success) { 
			assertMatches("can't reregister", success);
		}
		long started = nowMillis();
		TEST_QUEUE.send("this is a test message");
		while (!a.messageReceived && reallyMillisSince(started) < TIMEOUT_MILLIS)
			Times.sleep(1);
		assertTrue("did not receive message in a timely manner.", a.messageReceived);
	}
	
	public void testReadonlyChannel() throws Exception {
	    QQueue q = new QQueue("foo");
	    q.setReadonly(true);
	    try {
            q.send("fail");
            fail();
        } catch (RuntimeException e) {
            assertMatches("readonly", e);
        }
        q.setReadonly(false);
        q.send("fail");
    }
	
	public void testSecondBroker() throws Exception {
        Acknowledgement firstBroker = new Acknowledgement();
        testTopic.register(firstBroker);
        Acknowledgement secondBroker = new Acknowledgement();
        QTopic otherBrokerTopic = new QTopic(testTopic.name(), TEST_BROKER2, false);
        otherBrokerTopic.register(secondBroker);
        long start = reallyNow().getTime();
        testTopic.send("this is a test message");
        waitForMessage(firstBroker, start);
        assertFalse(secondBroker.messageReceived);
        firstBroker.messageReceived = false;
        start = reallyNow().getTime();
        otherBrokerTopic.send("this is a test message");
        waitForMessage(secondBroker, start);
        assertFalse(firstBroker.messageReceived);
    }
	
	public void testMultiTopicSubscription() throws Exception {
	    QTopic all = new QTopic("j.*", false);
	    Acknowledgement listener = new Acknowledgement();
	    all.register(listener);
	    new QTopic("j.foo").send("this is a test message");
	    waitForMessage(listener, reallyNow().getTime());
	    new QTopic("j.bar").send("this is a test message");
        waitForMessage(listener, reallyNow().getTime());
    }
	
	void waitForMessage(Acknowledgement a, long start) {
	    while (!a.messageReceived && reallyMillisSince(start) < TIMEOUT_MILLIS)
	        Times.sleep(25);
	    bombUnless(a.messageReceived, "did not receive timely message");
	}
	
	public void testTopic() throws Exception {
		Acknowledgement a = new Acknowledgement();
		Acknowledgement b = new Acknowledgement();
		testTopic.register(a);
		testTopic.register(b);
		long started = nowMillis();
		testTopic.send("this is a test message");
		waitForMessage(a, started);
		waitForMessage(b, started);
	}
	
	public void testDisconnect() throws Exception {
	    Acknowledgement a = new Acknowledgement();
	    testTopic.register(a);
	    long started = nowMillis();
        testTopic.send("this is a test message");
        while (!a.messageReceived && reallyMillisSince(started) < TIMEOUT_MILLIS)
            Times.sleep(1);
        assertTrue(a.messageReceived);
        a.messageReceived = false;
        Channel.closeResources();
        Channel.closeResources();  // second call should not bomb
        testTopic.send("this is a test message");
        while (!a.messageReceived && reallyMillisSince(started) < TIMEOUT_MILLIS)
            Times.sleep(1);
        assertFalse(a.messageReceived);
        
    }
	
	public void testSendWithFields() throws Exception {
	    GotFields f = new GotFields();
	    testTopic.register(f);
	    Fields fields = new Fields();
	    fields.put("foo", 1);
	    fields.put("bar", "asdf");
	    testTopic.send(fields);
	    long started = nowMillis();
	    while(f.received == null && reallyMillisSince(started) < TIMEOUT_MILLIS)
	        Times.sleep(1);
	    assertContains("MSTimestamp", f.received.keySet());
	    assertContains("MSTopicName", f.received.keySet());
	    assertContains("foo", f.received.keySet());
    }

	
	public void testTopicBomb() throws Exception {
	    Bombs b = new Bombs();
	    testTopic.register(b);
        long started = nowMillis();
        testTopic.send("this is a test message");
        while (!b.received && reallyMillisSince(started) < TIMEOUT_MILLIS)
            Times.sleep(1);
	    assertTrue(b.received);
    }
	
	public void functestBomb() throws Exception {
	    Channel.setDefaultBroker("failover:(tcp://amqmktdata.fftw.com:63636)?initialReconnectDelay=100&maxReconnectAttempts=10");
       SaySomething a = new SaySomething();
        testTopic.register(a);
        int count = 0;
        while(true) { sleepSeconds(1); info("sending " + count); testTopic.send("message #" + count++);}
    }
	
	
	
	
	
	
	
	
	
	
	
	
	
}
