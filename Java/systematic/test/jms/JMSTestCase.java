package jms;

import db.*;

public abstract class JMSTestCase extends DbTestCase {

    public static final String TEST_BROKER = "vm://testBroker1?marshal=false&broker.persistent=false";
    public static final String TEST_BROKER2 = "vm://testBroker2?marshal=false&broker.persistent=false";
    protected QQueue TEST_QUEUE;

	@Override public void setUp() throws Exception {
		super.setUp();
		staticSetup();
        TEST_QUEUE = new QQueue("test.queue");
	}

    // staticSetup and staticTeardown are here for use from R
    public static void staticSetup() {
        useTestBroker();
    }

	public static void useTestBroker() {
		Channel.setDefaultBroker(TEST_BROKER);
	}

	@Override public void tearDown() throws Exception {
		TEST_QUEUE.shutdown();
		super.tearDown();
	}

}