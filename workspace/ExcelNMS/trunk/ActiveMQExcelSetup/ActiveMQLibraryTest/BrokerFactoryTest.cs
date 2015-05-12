using ActiveMQLibrary;
using NUnit.Framework;

namespace ActiveMQLibraryTest
{
    [TestFixture]
    public class BrokerFactoryTest : AbstractNmsBaseTest
    {
        public const string Nyws802 = "failover:tcp://nyws802:60606";
        [Test]
        public void TestGetMultipleBrokers()
        {
            
            var brokerFactory1A = BrokerFactory.Broker(Nyws802);
            Assert.IsNotNull(brokerFactory1A);
            Assert.AreEqual(1, BrokerFactory.Count);

            var brokerFactory1B = BrokerFactory.Broker(Nyws802);
            Assert.IsNotNull(brokerFactory1B);
            Assert.AreEqual(1, BrokerFactory.Count);
            Assert.AreSame(brokerFactory1A, brokerFactory1B);

            // change case to simulate different brokers
            var brokerFactory2 = BrokerFactory.Broker("failover:tcp://NYWS802:60606");
            Assert.IsNotNull(brokerFactory2);
            Assert.AreEqual(2, BrokerFactory.Count);
            Assert.AreNotSame(brokerFactory1A, brokerFactory2);

            BrokerFactory.Remove(Nyws802);
            Assert.AreEqual(1, BrokerFactory.Count);
            BrokerFactory.Remove("failover:tcp://NYWS802:60606");
            Assert.AreEqual(0, BrokerFactory.Count);
        }

    }
}
