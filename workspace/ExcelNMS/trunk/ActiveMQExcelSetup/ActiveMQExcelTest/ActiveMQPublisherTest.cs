using ActiveMQExcel;
using ActiveMQLibrary;
using ActiveMQLibraryTest;
using NUnit.Framework;

namespace ActiveMQExcelTest {
    [TestFixture]
    public class ActiveMQPublisherTest : AbstractNmsBaseTest
    {
        [Test]
        public void TestServerPublish()
        {
            _receivedMessage = null;

            var config = new AppConfiguration { MarketDataBrokerUrl = BrokerFactoryTest.Nyws802 };
            var temp = AppConfiguration.AssemblyPath;

            var publisher = new ActiveMQPublisher(config);

            var testMessageConsumer = StartTestListener(BrokerFactory.Broker(config.MarketDataBrokerUrl), "Test.Topic.Publish");
            var publisherResult = publisher.AMQPub("Test.Topic.Publish", "FieldName", "FieldValue");
            Assert.IsNotNull(publisherResult);
            Assert.IsTrue(publisherResult.ToString().StartsWith("Submitted"));

            WaitFor(HasMessageArrived, this, 10 * 1000);
            Assert.IsNotNull(_receivedMessage);
            Assert.AreEqual(true, _receivedMessage.Contains("FieldName"));
            _receivedMessage = null;

            var fieldNameRange = new MockRange("FieldName", 1, 1);
            var fieldValueRange = new MockRange("FieldValueUpdate", 2, 1);

            publisherResult = publisher.AMQPub("Test.Topic.Publish", fieldNameRange, fieldValueRange);
            Assert.IsNotNull(publisherResult);
            Assert.IsTrue(publisherResult.ToString().StartsWith("Submitted"));

            WaitFor(HasMessageArrived, this, 10 * 1000);
            Assert.IsNotNull(_receivedMessage);
            Assert.AreEqual(true, _receivedMessage.Contains("FieldValueUpdate"));


            var fieldNameRange2 = new MockRange(new[] { "FieldName1", "FieldName2" }, 1, 1, true);

            var fieldValueRange2 = new MockRange(new[] { "FieldValueUpdate1", "FieldValueUpdate2" }, 1, 1, true);

            publisherResult = publisher.AMQPub("Test.Topic.Publish", fieldNameRange2, fieldValueRange2);
            Assert.IsNotNull(publisherResult);
            Assert.IsTrue(publisherResult.ToString().StartsWith("Submitted"));

            WaitFor(HasMessageArrived, this, 10 * 1000);
            Assert.IsNotNull(_receivedMessage);
            Assert.AreEqual(true, _receivedMessage.Contains("FieldValueUpdate"));


            StopTestListener(testMessageConsumer);

        }
    }
}