using System.Collections.Generic;
using ActiveMQLibrary;
using NUnit.Framework;

namespace ActiveMQLibraryTest {
    [TestFixture] 
    public class BrokerTest : AbstractNmsBaseTest {
        const string TestTopic = "Test.Library.Topic";

        [Test] 
        public void TestPublisher() {
            var broker = BrokerFactory.Broker(BrokerFactoryTest.Nyws802);
            var topicPublisher = broker.TopicPublisher(TestTopic);

            Assert.IsNotNull(topicPublisher);
            Assert.AreEqual(1, broker.PublisherCount);

            var topicPublisherSame = broker.TopicPublisher(TestTopic);
            Assert.IsNotNull(topicPublisher);
            Assert.AreEqual(1, broker.PublisherCount);
            Assert.AreSame(topicPublisher, topicPublisherSame);

            var topicPublisherDifferent = broker.TopicPublisher("Test.Library.Topic2");
            Assert.IsNotNull(topicPublisherDifferent);
            Assert.AreEqual(2, broker.PublisherCount);
            Assert.AreNotEqual(topicPublisher, topicPublisherDifferent);

            // Start up a test listener
            var testMessageConsumer = StartTestListener(broker, TestTopic);

            Assert.IsNotNull(topicPublisher.Publish("UnitTest.NMS.CSharp.Simple", "MyFieldName", "MyFieldValue"));
            WaitFor(HasMessageArrived, this, 10 * 1000);

            Assert.IsNotNull(_receivedMessage);
            Assert.AreEqual(true, _receivedMessage.Contains("MyFieldName"));
            _receivedMessage = null;

            IDictionary<string, string> testMessage = new Dictionary<string, string> {{"MyFieldName", "MyFieldValue"}};

            Assert.IsNotNull(topicPublisher.Publish(TestTopic, testMessage));

            WaitFor(HasMessageArrived, this, 10 * 1000);
            Assert.IsNotNull(_receivedMessage);
            Assert.AreEqual(true, _receivedMessage.Contains("MyFieldName"));

            StopTestListener(testMessageConsumer);

            BrokerFactory.Remove(BrokerFactoryTest.Nyws802);
        }

        [Test] 
        public void Testubscriber() {
            var broker = BrokerFactory.Broker(BrokerFactoryTest.Nyws802);

            _receivedMessage = null;

            var topicSubscriber = broker.TopicSubscriber(TestTopic, OnMessageTestHandler);
            Assert.IsNotNull(topicSubscriber);
            Assert.AreEqual(1, broker.SubscriberCount);

            var topicSubscriberSame = broker.TopicSubscriber(TestTopic, OnMessageTestHandler);
            Assert.IsNotNull(topicSubscriberSame);
            Assert.AreEqual(1, broker.SubscriberCount);

            var topicSubscriber2 = broker.TopicSubscriber("Test.Library.Topic2", OnMessageTestHandler);
            Assert.IsNotNull(topicSubscriber2);
            Assert.AreEqual(2, broker.SubscriberCount);

            Assert.IsNotNull(PublishTestMessage(broker, TestTopic, "FieldName", "FieldValue"));

            WaitFor(HasMessageArrived, this, 10 * 1000);
            Assert.IsNotNull(_receivedMessage);
            Assert.IsTrue(_receivedMessage.Contains("FieldValue"));
            broker.RemoveSubscriber("Test.Library.Topic2");
            Assert.AreEqual(1, broker.SubscriberCount);

        }
    }
}