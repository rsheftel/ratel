using System;
using System.Collections.Generic;
using ActiveMQExcel;
using ActiveMQLibrary;
using ActiveMQLibraryTest;
using Microsoft.Office.Interop.Excel;
using NUnit.Framework;

namespace ActiveMQExcelTest {
    [TestFixture]
    public class NmsRtdClientTest : AbstractNmsBaseTest
    {

        [Test]
        public void TestServer() {
            var config = new AppConfiguration { PositionsBrokerUrl = BrokerFactoryTest.Nyws802 };
            var server = new NmsRtdClient(config);
            IRTDUpdateEvent updateEventHandler = new TestEventHandler();
            var startResult = server.ServerStart(updateEventHandler);
            Assert.AreEqual(1, startResult);
            Assert.AreEqual(1, server.Heartbeat());
            var client = server.Client;

            var newValues = false;
            Array parameters = new[] { "account=Test|securityId=123|level1TagName=UnitTest", "FieldName1" };
            Assert.AreEqual("N/A", server.ConnectData(1, ref parameters, ref newValues));
            Assert.AreEqual(1, server.TopicCount);
            Assert.IsTrue(client.IsConnected());
            Assert.AreEqual("N/A", server.ConnectData(2, ref parameters, ref newValues));
            Assert.AreEqual(2, server.TopicCount);
            server.DisconnectData(2);
            Assert.AreEqual(1, server.TopicCount);

            var position = new Dictionary<string, string> {
                {"account", "Test"},
                {"securityId", "123"},
                {"level1TagName", "UnitTest"},
                {"FieldName1", "FirstTestValue"},
                {"messageType", "BatchPosition"}
            };

            var broker = BrokerFactory.Broker(config.PositionsBrokerUrl);

            Assert.IsNotNull(PublishTestMessage(broker, "position.Test", position));

            WaitFor(PositionMessage, client, 10000);
            var key = client.BuildPositionLookupKey("account=Test|securityId=123|level1TagName=UnitTest", "FieldName1");

            Assert.AreEqual("FirstTestValue", client.LookupValue(key, "FieldName1"));

            server.DisconnectData(1);
            Assert.AreEqual(0, server.TopicCount);

            server.ServerTerminate();
        }

        static bool PositionMessage(object source) {
            var client = (NmsClient)source;
            var key = client. BuildPositionLookupKey("account=Test|securityId=123|level1TagName=UnitTest", "FieldName1");

            return "N/A" != client.LookupValue(key, "FieldName1");
        }

    }

}