using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using Apache.NMS.ActiveMQ.Commands;
using NUnit.Framework;

namespace FixExecutionAddin.nms
{
    [TestFixture]
    [Category("integration")]
    public class NmsClientIntegrationTest : AbstractNmsTest
    {

        [Test]
        public void testSendOrderIntegration()
        {
            var nmsClient = new NmsClientApp("TestClient");
            nmsClient.Configure(BROKER_URL, FER_RESPONSE, FER_COMMAND);
            nmsClient.Start();

            WaitFor(Connected, nmsClient, 1000);
            Assert.IsTrue(nmsClient.Connected(), "Failed to connect");

            var order = CreateLimitOrder();
            Console.WriteLine("Created order "+ order.ClientOrderID);
            var messageID = nmsClient.SendOrder(order);
            Assert.IsNotNull(messageID, "Failed to send order");
            Console.WriteLine("Sent message:" + messageID);

            WaitFor(Response, nmsClient, 3000);
            Assert.GreaterOrEqual(nmsClient.OrderCacheCount, 1, "Failed to receive response");
            var response = nmsClient.GetResponseFor(messageID);
            Thread.Sleep(1000);
            Assert.IsNotNull(response, "Did not receive response for order " + messageID);

            Assert.IsFalse(response.ContainsKey("ERROR_1"), "Error with order:" + GetValueForKey("ERROR_1", response));
            nmsClient.Stop();
            Assert.IsFalse(nmsClient.Connected(), "Failed to disconnect");
        }

        private static string GetValueForKey(string key, IDictionary<string, string> container) {
            return container.ContainsKey(key) ? container[key] : null;
        }

        [Test]
        public void testSessionReconnect() {
            var nmsClient = new NmsClientApp("TestClient");
            nmsClient.Configure(BROKER_URL, FER_RESPONSE, FER_COMMAND);
            nmsClient.Start();

            WaitFor(Connected, nmsClient, 1000);
            Assert.IsTrue(nmsClient.Connected(), "Failed to connect");

            Console.WriteLine("Kill the broker NOW!");
            // sleep for a minute so we have time to kill the broker
            Thread.Sleep(60000);
            Console.WriteLine("Going to reconnect, the broker better be up!");

            WaitFor(Connected, nmsClient, 1000);
            Assert.IsTrue(nmsClient.Connected(), "Failed to connect");

        }
    }
}
