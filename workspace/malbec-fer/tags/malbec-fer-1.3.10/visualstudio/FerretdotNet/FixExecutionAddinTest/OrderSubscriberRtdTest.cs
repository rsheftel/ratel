using System;
using System.Text;
using System.Threading;
using FixExecutionAddin;
using FixExecutionAddin.Nms;
using FixExecutionAddin.Util;
using FixExecutionAddinTest.Nms;
using Microsoft.Office.Interop.Excel;
using NUnit.Framework;

namespace FixExecutionAddinTest
{
    [TestFixture]
    public class OrderSubscriberRtdTest : AbstractNmsSetupTest
    {

        [Test]
        public void TestRtdServer()
        {
            var config = new AppConfiguration { BrokerUrl = Nyws802 };
            var server = new OrderSubscriberRtd(config);
            IRTDUpdateEvent updateEventHandler = new TestEventHandler();
            Assert.AreEqual(1, server.ServerStart(updateEventHandler));
            WaitFor(NmsConnected, server, 10000);
            Assert.IsTrue(server.Connected);
            Assert.AreEqual(1, server.Heartbeat());

            var newValues = false;
            Array parameters = new[] { "UserOrderId=123456|OrderDate=07/08/2009" };
            Assert.AreEqual("Unknown order - sent query", server.ConnectData(1, ref parameters, ref newValues));
            Assert.AreEqual(1, server.TopicCount);

            parameters = new[] { "UserOrderId=123457|OrderDate=07/08/2009" };
            Assert.AreEqual("Unknown order - sent query", server.ConnectData(2, ref parameters, ref newValues));
            Assert.AreEqual(2, server.TopicCount);

            var updatedCount = 0;
            var updatedOrders = server.RefreshData(ref updatedCount);
            Assert.AreEqual(0, updatedCount);
            Assert.IsNotNull(updatedOrders);

            // Send a fake order
            var messageId = PublishOrder("123456", "07/08/2009");
            Assert.IsNotNull(messageId);
            Thread.Sleep(1000);

            updatedCount = 0;
            updatedOrders = server.RefreshData(ref updatedCount);
            Assert.AreEqual(1, updatedCount);
            Assert.IsNotNull(updatedOrders);
            Assert.AreEqual(1, updatedOrders.GetValue(0,0));
            Assert.AreEqual("NEW", updatedOrders.GetValue(1, 0));

            server.DisconnectData(1);
            Assert.AreEqual(1, server.TopicCount);
            server.DisconnectData(2);
            Assert.AreEqual(0, server.TopicCount);
            server.ServerTerminate();
            Assert.IsFalse(server.Connected);

        }

        string PublishOrder(string userOrderId, string orderDateString) {
            Connect();

            var order = new Order {UserOrderId = userOrderId, FixMessageType = "D"};
            var orderDate = DateTime.Parse(orderDateString);

            var sb = new StringBuilder(128);
            sb.Append(NmsSession.topicPrefix).Append(".");
            sb.Append(string.Format(@"{0:yyyyMMdd}", orderDate));
            sb.Append(".").Append(userOrderId);

            var producer = CreateTopicProducer(NmsSession.topicPrefix + ".20090708." + userOrderId);
            var messageBody = MessageUtil.CreateMessage(order.ToDictionary());
            var textMessage = producer.CreateTextMessage(messageBody);

            producer.Send(textMessage);

            Disconnect();

            return textMessage.NMSMessageId;
        }



        static bool NmsConnected(object source) {
            var ourSource = (OrderSubscriberRtd) source;

            return ourSource.Connected;
        }

        [SetUp]
        public void Setup() {
            StartActiveMQBroker();
        }

        [TearDown]
        public void TearDown() {
            StopActiveMQBroker();
        }
    }

    internal class TestEventHandler : IRTDUpdateEvent
    {
        #region IRTDUpdateEvent Members
        public void UpdateNotify()
        {
            Console.WriteLine("UpdateNotify called");
        }

        public void Disconnect()
        {
            Console.WriteLine("Disconnect called");
        }

        public int HeartbeatInterval
        {
            get { return 10; }
            set { }
        }
        #endregion
    }
}
