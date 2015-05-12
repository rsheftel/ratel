using System;
using System.Threading;
using Apache.NMS;
using FixExecutionAddin.Nms;
using FixExecutionAddin.Util;
using NUnit.Framework;

namespace FixExecutionAddinTest.Nms {
    [TestFixture]
    public class NmsClientTest : AbstractNmsSetupTest
    {

        [Test]
        public void TestStartStop() {
            var nmsClient = new NmsClientApp("TestClient");
            nmsClient.Configure(BrokerUrl, FerCommandQueue);
            nmsClient.Start();

            WaitFor(Connected, nmsClient, 10000);
            Assert.IsTrue(nmsClient.Connected(), "Failed to connect");

            nmsClient.Stop();
            Assert.IsFalse(nmsClient.Connected(), "Failed to disconnect");
        }

        [Test]
        public void TestSendOrder() {
            var nmsClient = new NmsClientApp("TestClient");
            nmsClient.Configure(BrokerUrl, FerCommandQueue);
            nmsClient.Start();

            WaitFor(Connected, nmsClient, 10000);
            Assert.IsTrue(nmsClient.Connected(), "Failed to connect");

            var order = CreateLimitOrder();
            var messageId = nmsClient.SendOrder(order);
            Assert.IsNotNull(messageId, "Failed to send order");
            Console.WriteLine("Sent message:"+ messageId);

            // simulate the server processing
            ProcessTestOrder();

            WaitFor(Response, nmsClient, 3000);
            Assert.GreaterOrEqual(nmsClient.OrderCacheCount, 1, "Failed to receive response");
            var response = nmsClient.GetResponseFor(messageId);
            Assert.IsNotNull(response, "Did not receive response for order "+ messageId);
            var sentOrder = nmsClient.GetOrderByKey(order.CacheKey);

            Assert.IsNotNull(sentOrder, "Failed to lookup order by clientOrderID");
            Assert.AreNotSame("NEW", sentOrder.Status, "Status not updated to non NEW");

            nmsClient.Stop();
            Assert.IsFalse(nmsClient.Connected(), "Failed to disconnect");
        }
        [Test]
        public void TestSessionReconnect()
        {
            var nmsClient = new NmsClientApp("TestClient");
            nmsClient.Configure(BrokerUrl, FerCommandQueue);
            nmsClient.Start();

            WaitFor(Connected, nmsClient, 10000);
            Assert.IsTrue(nmsClient.Connected(), "Failed to connect");

            Console.WriteLine("Killing the broker NOW!");
            StopActiveMQBroker();
            StartActiveMQBroker();
            // sleep for a minute so we have time to kill the broker
            Thread.Sleep(1000);
            Console.WriteLine("Going to reconnect, the broker better be up!");

            WaitFor(Connected, nmsClient, 1000);
            Assert.IsTrue(nmsClient.Connected(), "Failed to connect");
        }

        #region simulated server
        void ProcessTestOrder() {
            Connect();
            var consumer = CreateQueueConsumer(FerCommandQueue);
            

            var textMessage = consumer.Receive(TimeSpan.FromMilliseconds(1000)) as ITextMessage;
            while (textMessage != null) {
                var record = MessageUtil.ExtractRecord(textMessage.Text);

                PrintLine("Processing test message: ", record);

                var replyTo = textMessage.NMSReplyTo;
                if (replyTo == null) {
                    Console.WriteLine("Received message without a 'ReplyTo' property");
                } else {
                    Console.WriteLine("ReplyTo="+ replyTo);
                }

                var producer = CreateQueueProducer(replyTo);
                var originalNmsMessageID = textMessage.NMSMessageId;

                var response = record;

                response["STATUS"] = "SENT";

                var textResponse = CreateTextMessage(MessageUtil.CreateMessage(response));
                //textResponse.NMSCorrelationID = replyTo;

                textResponse.Properties.SetString("JmsOriginalMessageID", originalNmsMessageID);
                producer.Send(textResponse);
                producer.Dispose();
                //Console.WriteLine("Sent response:" + textResponse);

                textMessage = consumer.Receive(TimeSpan.FromMilliseconds(1000)) as ITextMessage;
            }
            consumer.Dispose();
            
            Disconnect();
        }

        #endregion

        #region Broker Start/Stop
        [SetUp]
        public void Setup() {
            StartActiveMQBroker();
        }

        [TearDown]
        public void TearDown() {
            StopActiveMQBroker();
        }
        #endregion


    }
}