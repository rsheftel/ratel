using System;
using System.Collections.Generic;
using System.Threading;
using FixExecutionAddin.Nms;
using NUnit.Framework;

namespace FixExecutionAddinTest.Nms {
    [TestFixture]
    [Category("integration")]
    public class NmsClientIntegrationTest : AbstractNmsTest
    {

        [Test]
        public void TestSendOrderIntegration()
        {
            var nmsClient = new NmsClientApp("TestClient");
            nmsClient.Configure(BrokerUrl, FerCommandQueue);
            nmsClient.Start();

            WaitFor(Connected, nmsClient, 10000);
            Assert.IsTrue(nmsClient.Connected(), "Failed to connect");

            var order = CreateLimitOrder();
            // Since this is an integration test, change platform to 'TEST'
            order.platform = "TEST";
            order.strategy = "TEST-EQUITY-STRATEGY";

            Console.WriteLine("Created order "+ order.CacheKey);
            var messageId = nmsClient.SendOrder(order);
            Assert.IsNotNull(messageId, "Failed to send order");
            Console.WriteLine("Sent message:" + messageId);

            WaitFor(Response, nmsClient, 3000);
            Assert.GreaterOrEqual(nmsClient.OrderCacheCount, 1, "Failed to receive response");
            Thread.Sleep(1000);
            var response = nmsClient.GetResponseFor(messageId);
            Assert.IsNotNull(response, "Did not receive response for order " + messageId);

            Assert.IsFalse(response.ContainsKey("ERROR_1"), "Error with order:" + GetValueForKey("ERROR_1", response));
            nmsClient.Stop();
            Assert.IsFalse(nmsClient.Connected(), "Failed to disconnect");
        }

        private static string GetValueForKey(string key, IDictionary<string, string> container) {
            return container.ContainsKey(key) ? container[key] : null;
        }
    }
}