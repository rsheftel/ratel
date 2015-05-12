using System;
using System.Collections.Generic;
using System.Threading;
using ActiveMQExcel;
using ActiveMQLibrary;
using ActiveMQLibraryTest;
using Microsoft.Office.Interop.Excel;
using NUnit.Framework;

namespace ActiveMQExcelTest
{
    [TestFixture]
    public class ActiveMQRtdServerTest : AbstractNmsBaseTest
    {
        [Test]
        public void TestServerSubscribe() {
            var config = new AppConfiguration { MarketDataBrokerUrl = BrokerFactoryTest.Nyws802 };

            var server = new ActiveMQRtdServer(config);
            IRTDUpdateEvent updateEventHandler = new TestEventHandler();
            var startResult = server.ServerStart(updateEventHandler);
            Assert.AreEqual(1, startResult);
            Assert.AreEqual(1, server.Heartbeat());

            // Use the default broker
            var newValues = false;
            Array parameters = new[] { "Test.Topic", "FieldName1" };
            Assert.AreEqual("N/A", server.ConnectData(1, ref parameters, ref newValues));
            Assert.IsTrue(newValues);
            Assert.AreEqual(1, server.TopicCount);
            Assert.AreEqual(1, server.FieldCount);

            var testBroker = BrokerFactory.Broker(BrokerFactoryTest.Nyws802);
            //PublishTestMessage(testBroker, "Test.Topic", "FieldName", "FieldValue");
            var multiFieldRecord = new Dictionary<string, string> {
                {"FieldName1", "FieldValue1"},
                {"FieldName2", "FieldValue2"}
            };

            PublishTestMessage(testBroker, "Test.Topic", multiFieldRecord);

            Thread.Sleep(1000);
            Assert.AreEqual("FieldValue1", server.ConnectData(2, ref parameters, ref newValues));
            Assert.AreEqual(2, server.TopicCount);
            Assert.AreEqual(1, server.FieldCount);

            // subscribe to field already in the message/cache
            parameters = new[] { "Test.Topic", "FieldName2" };
            Assert.AreEqual("FieldValue2", server.ConnectData(3, ref parameters, ref newValues));
            Assert.AreEqual(3, server.TopicCount);
            Assert.AreEqual(2, server.FieldCount);
            server.DisconnectData(3);
            Assert.AreEqual(2, server.TopicCount);
            Assert.AreEqual(1, server.FieldCount);

            var topicCount = 0;
            var updatedTopics = server.RefreshData(ref topicCount);
            Assert.AreEqual(1, topicCount);
            Assert.IsNotNull(updatedTopics);

            // Publish again to ensure we get both updates
            Console.WriteLine("Publish second message, when we have two Excel topics");
            PublishTestMessage(testBroker, "Test.Topic", "FieldName1", "FieldValue1");
            Thread.Sleep(1000);

            Console.WriteLine("Calling Refresh");
            updatedTopics = server.RefreshData(ref topicCount);
            Assert.AreEqual(2, topicCount);
            Assert.IsNotNull(updatedTopics);

            server.DisconnectData(1);
            Assert.AreEqual(1, server.TopicCount);
            Assert.AreEqual(1, server.FieldCount);
            server.DisconnectData(2);
            Assert.AreEqual(0, server.TopicCount);
            Assert.AreEqual(0, server.FieldCount);

            // Check constant lookups
            var newValues2 = false;
            Array parameters2 = new[] { "Test.Topic", "brokerUrl" };
            Assert.AreEqual(BrokerFactoryTest.Nyws802, server.ConnectData(10, ref parameters2, ref newValues2));

            parameters2 = new[] { "Test.Topic", "productVersion" };
            var productVersion = server.ConnectData(11, ref parameters2, ref newValues2);
            Assert.IsNotNull(productVersion);
            Assert.AreNotEqual("#Error", productVersion);
            Console.WriteLine(productVersion);
        }

    }
}
