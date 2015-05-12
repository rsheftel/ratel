using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Apache.NMS;
using FixExecutionAddin.util;
using NUnit.Framework;

namespace FixExecutionAddin.nms
{
    [TestFixture]
    public class NmsClientTest : AbstractNmsSetupTest
    {

        [Test]
        public void testStartStop() {
            var nmsClient = new NmsClientApp("TestClient");
            nmsClient.Configure(BROKER_URL, FER_RESPONSE, FER_COMMAND);
            nmsClient.Start();

            WaitFor(Connected, nmsClient, 3000);
            Assert.IsTrue(nmsClient.Connected(), "Failed to connect");

            nmsClient.Stop();
            Assert.IsFalse(nmsClient.Connected(), "Failed to disconnect");
        }

        [Test]
        public void testSendOrder() {
            var nmsClient = new NmsClientApp("TestClient");
            nmsClient.Configure(BROKER_URL, FER_RESPONSE, FER_COMMAND);
            nmsClient.Start();

            WaitFor(Connected, nmsClient, 1000);
            Assert.IsTrue(nmsClient.Connected(), "Failed to connect");

            var order = CreateLimitOrder();
            var messageID = nmsClient.SendOrder(order);
            Assert.IsNotNull(messageID, "Failed to send order");
            Console.WriteLine("Sent message:"+ messageID);

            // simulate the server processing
            ProcessTestOrder();

            WaitFor(Response, nmsClient, 3000);
            Assert.GreaterOrEqual(nmsClient.OrderCacheCount, 1, "Failed to receive response");
            var response = nmsClient.GetResponseFor(messageID);
            Assert.IsNotNull(response, "Did not receive response for order "+ messageID);
            var sentOrder = nmsClient.GetOrderByClientOrderID(order.ClientOrderID);

            Assert.IsNotNull(sentOrder, "Failed to lookup order by clientOrderID");
            Assert.AreNotSame("NEW", sentOrder.Status, "Status not updated to non NEW");

            nmsClient.Stop();
            Assert.IsFalse(nmsClient.Connected(), "Failed to disconnect");
        }

      
        #region simulated server
        void ProcessTestOrder() {
            Connect();
            var consumer = CreateQueueConsumer(FER_COMMAND);
            var producer = CreateQueueProducer(FER_RESPONSE);

            var textMessage = consumer.Receive(TimeSpan.FromMilliseconds(1000)) as ITextMessage;
            while (textMessage != null) {
                var record = MessageUtil.ExtractRecord(textMessage.Text);

                PrintLine(record);
                var replyTo = textMessage.Properties["ReplyTo"] as string;
                if (String.IsNullOrEmpty(replyTo)) {
                    Console.WriteLine("Received message without a 'ReplyTo' property");
                } else {
                    Console.WriteLine("ReplyTo="+ replyTo);
                }
                var originalNmsMessageID = textMessage.NMSMessageId;

                var response = record;

                response["STATUS"] = "SENT";

                var textResponse = CreateTextMessage(MessageUtil.CreateMessage(response));
                textResponse.NMSCorrelationID = replyTo;

                textResponse.Properties.SetString("JmsOriginalMessageID", originalNmsMessageID);
                producer.Send(textResponse);
                //Console.WriteLine("Sent response:" + textResponse);

                textMessage = consumer.Receive(TimeSpan.FromMilliseconds(1000)) as ITextMessage;
            }
            consumer.Dispose();
            producer.Dispose();
            Disconnect();


        }
        #endregion
  


    }

}
