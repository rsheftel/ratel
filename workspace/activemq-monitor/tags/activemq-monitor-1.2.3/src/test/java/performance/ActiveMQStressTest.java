package performance;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import util.LocalProcess;
import util.Monitoring;

/**
 * These tests require that an external ActiveMQ server be started an configured for proper testing.
 * 
 * We assume that the instance will run on the local box using port 64656.
 * 
 */
public class ActiveMQStressTest implements ExceptionListener {

    private static final int MAX_MESSAGE_PER_DAY = 3600000;
    private static final int MAX_MESSAGE_PER_HALF_DAY = MAX_MESSAGE_PER_DAY / 2;

    private static final long NANO_TO_MILLI = 1000000;

    private static final int MIN_TOPICS = 10000;

    private static final int MIN_PRODUCER_CONSUMER = 100;
    private static final long LONG_BROKER_STARTUP_DELAY = 30;
    private static final long SHORT_BROKER_STARTUP_DELAY = 10;
    private static final String TEST_MESSAGE_BASE = "Test Message that is large so that we take up a lot of space on the broker.\n"
            + "Two lines is more than one, but three would be even bigger.  Not sure how many lines it will take\n"
            + "to break the broker so we can actually test in a reasonable time. ";

    //private static final String HOST_NAME = "localhost";
     private static final String HOST_NAME = "nysrv61";
    // private static final String HOST_NAME = "nyux51";
    private static final int HOST_PORT = 63636;
    // private static final int HOST_PORT = 64616;

    private static final String BROKER_URL = "tcp://" + HOST_NAME + ":" + HOST_PORT
            + "?wireFormat.maxInactivityDuration=0";
    // + "?transport.requesttimeout=10000";

    private LocalProcess localProcess;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private Exception connectionException;

    // Test that really hit the broker hard should set this to a large value so the next
    // test waits long enough for the broker to start
    private long startupDelay = LONG_BROKER_STARTUP_DELAY / 2; // seconds

    @BeforeMethod(groups = { "stresstest", "st-" })
    public void startJvm() throws Exception {

        String javaHome = System.getProperty("java.home");
        String activemqHome = System.getProperty("activemq.home");

        // *** If you want to use SSL, the keystore needs to be configured
        // "C:\Developer\Java\jdk1.6.0_06\bin\java.exe" -Dcom.sun.management.jmxremote -Xmx1536M
        // -Dorg.apache.activemq.UseDedicatedTaskRunner=false
        // -Dderby.system.home="C:\Developer\apache-activemq-5.1.0\bin\..\data"
        // -Dderby.storage.fileSyncTransactionLog=true -Djavax.net.ssl.keyStorePassword=password
        // -Djavax.net.ssl.trustStorePassword=password
        // -Djavax.net.ssl.keyStore="C:\Developer\apache-activemq-5.1.0\bin\../conf/broker.ks"
        // -Djavax.net.ssl.trustStore="C:\Developer\apache-activemq-5.1.0\bin\../conf/broker.ts"
        // -Dactivemq.classpath="C:\Developer\apache-activemq-5.1.0\bin\../conf;"
        // -Dactivemq.home="C:\Developer\apache-activemq-5.1.0\bin\.."
        // -Dactivemq.base="C:\Developer\apache-activemq-5.1.0\bin\.." -jar
        // "C:\Developer\apache-activemq-5.1.0\bin\../bin/run.jar" "start"
        String[] command = { javaHome + "/bin/java", "-Dcom.sun.management.jmxremote", "-Xmx1536M",
                "-Dorg.apache.activemq.UseDedicatedTaskRunner=false", "-jar", activemqHome + "/bin/run.jar",
                "start" };
        // "broker:(tcp://localhost:64616)?persistent=false&useJmx=true" };

        localProcess = new LocalProcess(command);
        localProcess.execute(5); // read at least 5 lines before returning
        System.out.println("Waiting " + startupDelay + " seconds for JVM...");
        Monitoring.sleep(startupDelay * 1000 * 4); // delay in seconds
    }
    
    @BeforeMethod(groups = { "stresstest-remote" })
    public void startJvmAsService() throws Exception {
        // TODO Add the service start stop code here -- need access on remote machine
    }
    
    @AfterMethod(groups = { "stresstest", "stresstest-" })
    public void stopJvm() {
        localProcess.kill();
        connectionException = null;
    }

    @Test(groups = { "stresstest" })
    public void testMaxConsumerTopics() throws Exception {
        // Create as many consumers we can until we get and error. If the number
        // of topics is less than 10K, we have a problem.

        startupDelay = LONG_BROKER_STARTUP_DELAY;
        Session session = createConnection();

        System.out.println("\nConnected to " + BROKER_URL);
        System.out.println("Testing ConsumerTopics");

        int topicCount = createAndCloseManyTopics(session, "UnitTest.MAX_TOPIC.", MIN_TOPICS);
        System.out.println("Min test, created: " + topicCount);
        assert topicCount == MIN_TOPICS : "Failed to create the minimum required topics";
        // If we made it this far, see how many we can create before we break
        int additionalTopics = 0;
        boolean brokerFailed = false;
        try {
            for (int i = 0;; i++) {
                additionalTopics = i;
                String topicName = "UnitTest.ADDITIONAL_TOPIC." + i;
                createAndCloseConsumerTopic(session, topicName);
            }
        } catch (Exception e) {
            brokerFailed = true;
        }

        System.out.println("Total consumer topics created=" + (MIN_TOPICS + additionalTopics));
        assert brokerFailed : "Broker did not fail - how did we get here?";

    }

    private int createAndCloseManyTopics(Session session, String topicName, int maxIteration) {
        int topicCount = 0;
        try {
            for (int i = 0; i < maxIteration; i++) {
                topicCount = i + 1;
                createAndCloseConsumerTopic(session, topicName + i);
            }
        } catch (Exception e) {}
        return topicCount;
    }

    @Test(groups = { "stresstest" })
    public void testMaxProducerTopics() throws Exception {
        // Create as many producers we can until we get and error. If the number
        // of topics is less than 10K, we have a problem.

        startupDelay = LONG_BROKER_STARTUP_DELAY;

        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
        Connection connection = factory.createConnection();
        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        connection.setExceptionListener(this);
        connection.start();

        System.out.println("\nConnected to " + BROKER_URL);
        System.out.println("Testing ProducerTopics");

        boolean createdAll = false;
        int topicCount = 0;
        try {
            for (int i = 1; i <= MIN_TOPICS; i++) {
                topicCount = i;
                createAndCloseProducerTopic(session, "UnitTest.MAX_TOPIC." + i);
            }

            createdAll = true;
        } catch (Exception e) {
            System.out.println("Total Number of Topics Created=" + topicCount);
        }

        assert createdAll : "Failed to create the minimum required topics";
        // If we made it this far, see how many we can create before we break
        int additionalTopics = 0;
        boolean brokerFailed = false;
        try {
            for (int i = 1;; i++) {
                additionalTopics = i;
                createAndCloseProducerTopic(session, "UnitTest.ADDITIONAL_TOPIC." + i);
            }
        } catch (Exception e) {
            brokerFailed = true;
        }

        System.out.println("Total producer topics created=" + (MIN_TOPICS + additionalTopics));
        assert brokerFailed : "Broker did not fail - how did we get here?";

    }

    @Test(groups = { "stresstest" })
    public void testProducerConsumerTopics() throws Exception {
        // Create as many producers with 100 consumers that we can until we get and error. If the number
        // of topics is less than 10K, we have a problem.
        startupDelay = LONG_BROKER_STARTUP_DELAY;
        Session session = createConnection();

        System.out.println("\nConnected to " + BROKER_URL);
        System.out.println("Testing Producer/Consumer");

        boolean createdAll = false;
        int topicCount = 0;
        try {
            for (int i = 0; i < MIN_TOPICS; i++) {
                topicCount = i;
                String topicName = "UnitTest.ProducerConsumer." + i;

                createAndCloseProducerTopic(session, topicName);

                for (int j = 0; j < MIN_PRODUCER_CONSUMER; j++) {
                    createAndCloseConsumerTopic(session, topicName);
                }
            }
            createdAll = true;
        } catch (JMSException e) {
            System.out.println("Total Number of Topics Created=" + topicCount);
            // e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Total Number of Topics Created=" + topicCount);
            // e.printStackTrace();
        }

        System.out.println("Min test, created: " + topicCount);
        assert createdAll : "Failed to create the minimum required topics";
        // If we made it this far, see how many we can create before we break
        int additionalTopics = 0;
        boolean brokerFailed = false;
        try {
            for (int i = 0;; i++) {
                additionalTopics = i;
                String topicName = "UnitTest.ProducerConsumer.Additional" + i;

                createAndCloseProducerTopic(session, topicName);

                for (int j = 0; j < MIN_PRODUCER_CONSUMER; j++) {
                    createAndCloseConsumerTopic(session, topicName);
                }
            }
        } catch (Exception e) {
            brokerFailed = true;
        }
        System.out.println("Total Producer/Consumer topics created=" + (MIN_TOPICS + additionalTopics));
        assert brokerFailed : "Broker did not fail - how did we get here?";

    }

    @Test(groups = { "stresstest", "stresstest-remote" })
    public void testConnectionProducerConsumerTopics() throws Exception {
        // Create as many connections with consumer/producer pair as we can until we get and error. If the
        // number of topics is less than 10K, we have a problem.

        System.out.println("\nConnected to " + BROKER_URL);
        System.out.println("Testing Connection/Producer/Consumer");
        startupDelay = LONG_BROKER_STARTUP_DELAY;

        boolean createdAll = false;
        int topicCount = 0;
        try {
            for (int i = 0; i < MIN_TOPICS; i++) {
                ConnectionSession connectionSession = createClosableConnection();
                topicCount = i;
                String topicName = "UnitTest.ConnectionProducerConsumer." + i;
                createAndCloseProducerTopic(connectionSession.jmsSession, topicName);
                createAndCloseConsumerTopic(connectionSession.jmsSession, topicName);
                connectionSession.jmsSession.close();
                connectionSession.jmsConnection.close();

                if (connectionException != null) {
                    System.out.println("CONN-Total Number of Topics Created=" + topicCount);
                    throw connectionException;
                }
            }
            createdAll = true;
        } catch (JMSException e) {
            System.out.println("Total Number of Topics Created=" + topicCount);
            e.printStackTrace();
            // e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Total Number of Topics Created=" + topicCount);
            // e.printStackTrace();
        } catch (Error e) {
            System.out.println("Total Number of Topics Created=" + topicCount);
        }
        System.out.println("Total Number of Topics Created=" + topicCount);

        assert createdAll : "Failed to create the minimum required topics";
        // If we made it this far, see how many we can create before we break
        int additionalTopics = 0;
        boolean brokerFailed = false;
        try {
            for (int i = 0;; i++) {
                additionalTopics = i;
                Session session = createConnection();
                String topicName = "UnitTest.Connection.ProducerConsumer.Additional" + i;
                createAndCloseProducerTopic(session, topicName);
                createAndCloseConsumerTopic(session, topicName);
            }
        } catch (Exception e) {
            brokerFailed = true;
        }

        System.out.println("Total topics created=" + (MIN_TOPICS + additionalTopics));
        assert brokerFailed : "Broker did not fail - how did we get here?";

    }

    @Test(groups = { "stresstest" })
    public void testSlowConsumer() throws Exception {
        // Create a slow/blocked consumer and a producer, produce messages until we crash
        startupDelay = LONG_BROKER_STARTUP_DELAY;
        System.out.println("\nConnected to " + BROKER_URL);
        System.out.println("Testing Slow Consumer");

        String topicName = "UnitTest.SlowConsumer.Topic";
        Session session = createConnection();
        SlowConsumer slowConsumer = createSlowConsumer(topicName, session);
        MessageProducer producer = session.createProducer(session.createTopic(topicName));

        int messageCount = 0;
        boolean brokerBroke = true;
        try {
            for (int i = 0; i < MAX_MESSAGE_PER_HALF_DAY; i++) {
                sendMessage(producer, session, TEST_MESSAGE_BASE + i);
                messageCount++;
            }
            brokerBroke = false;
        } catch (Exception e) {
            // we finished the test
        } catch (Error e) {
            // client ran out of memory
        }

        int failedTopicSize = messageCount - slowConsumer.processedMessageCount;
        System.out.println("Number of messages on topic=" + failedTopicSize);
        System.out.println("Message size=" + TEST_MESSAGE_BASE.length());
        assert failedTopicSize > MAX_MESSAGE_PER_HALF_DAY / 2 : "Topic failed too soon";
        assert !brokerBroke : "We broke the broker";

    }

    @Test(groups = { "stresstest" })
    public void testSlowConsumerFastConsumer() throws Exception {
        // Create a slow/blocked consumer and a producer, produce messages until we crash
        startupDelay = SHORT_BROKER_STARTUP_DELAY;
        System.out.println("\nConnected to " + BROKER_URL);
        System.out.println("Testing Slow Consumer/Fast Consumer");

        String topicName = "UnitTest.SlowFastConsumer.Topic";
        Session slowSession = createConnection();
        SlowConsumer slowConsumer = createSlowConsumer(topicName, slowSession);

        Session fastSession = createConnection();
        FastConsumer fastConsumer = createFastConsumer(topicName, fastSession);

        MessageProducer producer = fastSession.createProducer(fastSession.createTopic(topicName));

        int messageCount = 0;
        boolean brokerFailed = true;
        try {
            // estimated messages/day
            for (int i = 0; i < MAX_MESSAGE_PER_HALF_DAY; i++) {
                if (sendMessage(producer, fastSession, TEST_MESSAGE_BASE + i)) {
                    messageCount++;
                }
            }
            brokerFailed = false;
        } catch (Exception e) {
            // we finished the test
        }

        int failedTopicSize = messageCount - slowConsumer.processedMessageCount;
        System.out.println("Slow consumer consumed: " + slowConsumer.processedMessageCount);
        System.out.println("Fast consumer consumed: " + fastConsumer.processedMessageCount);
        System.out.println("Messages published: " + messageCount);
        System.out.println("Message size=" + TEST_MESSAGE_BASE.length());
        System.out.println("Number of messages on topic=" + failedTopicSize);
        assert brokerFailed : "We did not break the broker.";
        assert failedTopicSize > MAX_MESSAGE_PER_HALF_DAY / 2 : "Topic failed too soon";

    }

    @Test(groups = { "stresstest" })
    public void testMessageSpeed() throws Exception {
        // Create a slow/blocked consumer and a producer, produce messages until we crash

        System.out.println("\nConnected to " + BROKER_URL);
        System.out.println("Testing Message Speed");

        String topicName = "UnitTest.MessageSpeed.Topic";
        Session consumerSession = createConnection();
        Session producerSession = createConnection();
        TimingConsumer consumer = createTimingConsumer(topicName, consumerSession);

        MessageProducer producer = producerSession.createProducer(producerSession.createTopic(topicName));

        int messageCount = 0;
        try {
            for (int i = 0; i < 10000; i++) {
                sendMessage(producer, producerSession, String.valueOf(System.nanoTime()));
                messageCount++;
            }
        } catch (Exception e) {
            // we finished the test
        }
        Monitoring.sleep(5000); // try to get all the messages
        System.out.println("Messages processed=" + consumer.processedMessageCount);
        long maxDelayMilli = consumer.maxDelay / NANO_TO_MILLI;
        long minDelayMilli = consumer.minDelay / NANO_TO_MILLI;
        System.out.println("Longest delay=" + consumer.maxDelay + " (" + maxDelayMilli + ")");
        System.out.println("Shortest delay=" + consumer.minDelay + " (" + minDelayMilli + ")");

        assert maxDelayMilli < 1000 : "Message delivered too slow";
    }

    @Test(groups = { "stresstest" })
    public void testMessageSpeedManyEmptyTopics() throws Exception {

        startupDelay = LONG_BROKER_STARTUP_DELAY;

        System.out.println("\nConnected to " + BROKER_URL);
        System.out.println("Testing Message Speed Many Empty Topics");

        String topicName = "UnitTest.MessageSpeedManyTopics.Topic";
        Session consumerSession = createConnection();
        Session producerSession = createConnection();
        TimingConsumer consumer = createTimingConsumer(topicName, consumerSession);

        MessageProducer producer = producerSession.createProducer(producerSession.createTopic(topicName));

        // Load up the broker with many topics then test the speed
        Session session = createConnection();
        createAndCloseManyTopics(session, "UnitTest.MessageSpeedManyTopics.Dummy", 3000);

        int messageCount = 0;
        try {
            for (int i = 0; i < 10000; i++) {
                sendMessage(producer, producerSession, String.valueOf(System.nanoTime()));
                messageCount++;
            }
        } catch (Exception e) {
            // we finished the test
        }
        Monitoring.sleep(5000); // try to get all the messages
        System.out.println("Messages processed=" + consumer.processedMessageCount);
        long maxDelayMilli = consumer.maxDelay / NANO_TO_MILLI;
        long minDelayMilli = consumer.minDelay / NANO_TO_MILLI;
        System.out.println("Longest delay=" + consumer.maxDelay + " (" + maxDelayMilli + ")");
        System.out.println("Shortest delay=" + consumer.minDelay + " (" + minDelayMilli + ")");

        assert consumer.processedMessageCount > 0 : "Didn't receive any messages";
        assert consumer.processedMessageCount == 10000 : "Didn't receive all messages";
        assert maxDelayMilli < 1000 : "Message delivered too slow";

    }

    @Test(groups = { "stresstest" })
    public void testMessageSize() throws Exception {
        // Publish an ever increasing message until we crash

        System.out.println("\nConnected to " + BROKER_URL);
        System.out.println("Testing Message Size");

        String topicName = "UnitTest.MessageSize.Topic";
        Session consumerSession = createConnection();
        Session producerSession = createConnection();
        TimingConsumer consumer = createTimingConsumer(topicName, consumerSession);

        MessageProducer producer = producerSession.createProducer(producerSession.createTopic(topicName));

        int messageCount = 0;
        int messageSize = 0;
        try {
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                String message = createBiggerMessage(i);
                messageSize = message.length();
                sendMessage(producer, producerSession, String.valueOf(System.nanoTime() + "\n" + message));
                messageCount++;
            }
        } catch (Exception e) {
            // we finished the test
        } catch (Error e) {
            // We probably ran out of memory on the client
            System.out.println("client failure");
        }
        Monitoring.sleep(5000); // try to get all the messages
        System.out.println("Max message size=" + messageSize);
        System.out.println("Messages processed=" + consumer.processedMessageCount);
        long maxDelayMilli = consumer.maxDelay / NANO_TO_MILLI;
        long minDelayMilli = consumer.minDelay / NANO_TO_MILLI;
        System.out.println("Longest delay=" + consumer.maxDelay + " (" + maxDelayMilli + ")");
        System.out.println("Shortest delay=" + consumer.minDelay + " (" + minDelayMilli + ")");

        assert maxDelayMilli < 1000 : "Message delivered too slow";
    }

    private String createBiggerMessage(int size) {

        StringBuilder sb = new StringBuilder(1000 * size * TEST_MESSAGE_BASE.length());

        for (int i = 0; i < 1000 * size; i++) {
            // sb.append((char)(i % 26 + 65));
            sb.append(TEST_MESSAGE_BASE);
        }

        return sb.toString();
    }

    private SlowConsumer createSlowConsumer(String topicName, Session session) throws JMSException {
        MessageConsumer consumer = session.createConsumer(session.createTopic(topicName));
        SlowConsumer slowConsumer = new SlowConsumer();
        consumer.setMessageListener(slowConsumer);
        return slowConsumer;
    }

    private FastConsumer createFastConsumer(String topicName, Session session) throws JMSException {
        MessageConsumer consumer = session.createConsumer(session.createTopic(topicName));
        FastConsumer fastConsumer = new FastConsumer();
        consumer.setMessageListener(fastConsumer);
        return fastConsumer;
    }

    private TimingConsumer createTimingConsumer(String topicName, Session session) throws JMSException {
        MessageConsumer consumer = session.createConsumer(session.createTopic(topicName));
        TimingConsumer fastConsumer = new TimingConsumer();
        consumer.setMessageListener(fastConsumer);
        return fastConsumer;
    }

    private Session createConnection() throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
        Connection connection = factory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        connection.setExceptionListener(this);
        connection.start();

        return session;
    }

    private ConnectionSession createClosableConnection() throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
        Connection connection = factory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        connection.setExceptionListener(this);
        connection.start();
        return new ConnectionSession(connection, session);
    }

    private void createAndCloseConsumerTopic(final Session session, final String topicName) throws Exception {
        Future<Boolean> future = executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                MessageConsumer consumer = session.createConsumer(session.createTopic(topicName));
                consumer.close();
                return true;
            }
        });

        future.get(10, TimeUnit.SECONDS);
    }

    private void createAndCloseProducerTopic(final Session session, final String topicName) throws Exception {
        // Spin this off as a task, as the close will hang and we will never finish
        Future<Boolean> future = executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                MessageProducer producer = session.createProducer(session.createTopic(topicName));
                producer.send(session.createTextMessage(topicName));
                producer.close();
                return true;
            }
        });
        future.get(10, TimeUnit.SECONDS);
    }

    private boolean sendMessage(final MessageProducer producer, final Session session,
            final String messageBody) throws Exception {
        Future<Boolean> future = executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                producer.send(session.createTextMessage(messageBody));
                return true;
            }
        });

        return future.get(10, TimeUnit.SECONDS);
    }

    @Override
    public void onException(JMSException exception) {
        connectionException = exception;
    }

    private static final class SlowConsumer implements MessageListener {
        private int processedMessageCount;

        @Override
        public void onMessage(Message message) {
            processedMessageCount++;
            // Just sleep so that the topic backs-up
            Monitoring.sleep(100000);
        }
    }

    private static final class FastConsumer implements MessageListener {
        private int processedMessageCount;

        @Override
        public void onMessage(Message message) {
            processedMessageCount++;
        }
    }

    private static final class TimingConsumer implements MessageListener {
        private long maxDelay = Long.MIN_VALUE;

        private long minDelay = Long.MAX_VALUE;

        private int processedMessageCount;

        @Override
        public void onMessage(Message message) {
            try {
                long receivedTime = System.nanoTime();
                processedMessageCount++;

                TextMessage textMessage = (TextMessage) message;
                String messageBody = textMessage.getText();
                int nlPos = messageBody.indexOf('\n');
                if (nlPos > -1) {
                    messageBody = messageBody.substring(0, nlPos);
                }

                long sentTime = Long.parseLong(messageBody);
                long delay = receivedTime - sentTime;

                if (maxDelay < delay) {
                    maxDelay = delay;
                }

                if (minDelay > delay) {
                    minDelay = delay;
                }
            } catch (NumberFormatException e) {} catch (JMSException e) {} catch (Error e) {}
        }
    }

    private static class ConnectionSession {
        public ConnectionSession(Connection connection, Session session) {
            jmsConnection = connection;
            jmsSession = session;
        }

        Session jmsSession;
        Connection jmsConnection;
    }
}
