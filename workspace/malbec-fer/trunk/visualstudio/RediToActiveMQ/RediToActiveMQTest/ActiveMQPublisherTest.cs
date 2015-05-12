using System;
using System.Collections.Generic;
using NUnit.Framework;
using RediToActiveMQ;

namespace RediToActiveMQTest
{
    [TestFixture]
    public class ActiveMQPublisherTest : AbstractTest
    {

         AppConfiguration appConfig;

        [SetUp]
        public void LoadConfiguration() 
        {
            appConfig = AppConfiguration.Load();
        }

        [Test]
        public void TestConnect() 
        {
            var publisher = new ActiveMQPublisher(appConfig.BrokerUrl);

            publisher.Connect();
            Assert.IsTrue(publisher.IsConnected, "Failed to connect to test broker");

            publisher.Disconnect();
            Assert.IsFalse(publisher.IsConnected, "Failed to disconnect");
        }

        [Test]
        public void TestPublish()
        {
            var publisher = new ActiveMQPublisher(appConfig.BrokerUrl);

            publisher.Connect();
            Assert.IsTrue(publisher.IsConnected, "Failed to connect to test broker");

            var message = new Dictionary<string, string> {{"SYMBOL", "ZVZZT"}};

            var messageId = publisher.Publish("Redi.Test.Position.ZVZZT", message);

            Assert.IsNotNull(messageId, "Failed to send message");

            publisher.Disconnect();
            Assert.IsFalse(publisher.IsConnected, "Failed to disconnect");
        }

        [Test]
        public void TestSendingHeartbeat() {
            var publisher = new ActiveMQPublisher(appConfig.BrokerUrl);

            publisher.Connect();
            Assert.IsTrue(publisher.IsConnected, "Failed to connect to test broker");

            var message = new Dictionary<string, string> { { "SYMBOL", "ZVZZT" } };

            var messageId = publisher.SendHeartbeat("TestUser", "1.2.3.4.5", true, false, DateTime.Now);

            Assert.IsNotNull(messageId, "Failed to send heartbeat");

            publisher.Disconnect();
            Assert.IsFalse(publisher.IsConnected, "Failed to disconnect");
            
        }

    }
}
