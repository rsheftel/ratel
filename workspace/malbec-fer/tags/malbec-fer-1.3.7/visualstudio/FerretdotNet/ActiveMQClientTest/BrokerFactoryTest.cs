using System.Collections.Generic;
using ActiveMQClient;
using NUnit.Framework;

namespace ActiveMQClientTest
{
    [TestFixture]
    public class BrokerFactoryTest : AbstractNmsBaseTest
    {
        [Test]
        public void TestGetMultipleBrokers() {
            var brokerFactory1A = BrokerFactory.Broker("failover:tcp://nyws802:60606");
            Assert.IsNotNull(brokerFactory1A);

            var brokerFactory1B = BrokerFactory.Broker("failover:tcp://nyws802:60606");
            Assert.IsNotNull(brokerFactory1B);

            Assert.AreSame(brokerFactory1A, brokerFactory1B);


            // change case to simulate different brokers
            var brokerFactory2 = BrokerFactory.Broker("failover:tcp://NYWS802:60606");
            Assert.IsNotNull(brokerFactory2);

            Assert.AreNotSame(brokerFactory1A, brokerFactory2);
        }

        [Test]
        public void TestCreatePublisher() {
            var brokerFactory = BrokerFactory.Broker("failover:tcp://nyws802:60606");
            Assert.IsNotNull(brokerFactory);
            Assert.IsTrue(brokerFactory.IsStarted);

            var topicMessage = new Dictionary<string, string>();
            topicMessage["Source"] = "UnitTest";
            topicMessage["SharesSold"] = "9";
            topicMessage["SharesBought"] = "10";
            topicMessage["Position"] = "1";

            var publishStatus = brokerFactory.Publish("Test.Topic.One", topicMessage);

            Assert.IsNotNull(publishStatus);
            Assert.IsFalse(publishStatus.StartsWith("Not sent"));

            brokerFactory.Shutdown();
            Assert.IsFalse(brokerFactory.IsStarted);

            var brokerFactoryDup = BrokerFactory.Broker("failover:tcp://nyws802:60606");
            Assert.IsNotNull(brokerFactoryDup);
            Assert.AreNotSame(brokerFactory, brokerFactoryDup);
        }

        [SetUp]
        public void Setup()
        {
            StartActiveMQBroker();
        }

        [TearDown]
        public void TearDown()
        {
            StopActiveMQBroker();
        }
    }
}
