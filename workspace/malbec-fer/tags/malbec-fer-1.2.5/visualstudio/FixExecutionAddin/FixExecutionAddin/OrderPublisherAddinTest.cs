using System;
using System.Text;
using FixExecutionAddin.Nms;
using NUnit.Framework;

namespace FixExecutionAddin
{
    [TestFixture]
    public class OrderPublisherAddinTest : AbstractNmsSetupTest
    {
        const string ORDER_STRING = "Side=BUY|OrderType=LIMIT|LimitPrice=89.09|Quantity=11|Symbol=ZZVTV|SecurityType=Equity|platform=TESTSERVER";

        /// <summary>
        /// Validate the combination of Redi ClientOrderId requirements along with Bloomberg's
        /// EMSX ClientOrderId requirements.
        /// 
        /// 
        /// </summary>
        [Test]
        public void TestValidateUserOrderId() {
            // disable the loading of the configuration - unit tests get random assembly names
            Configuration.loaded = true;
            var addin = new OrderPublisherAddin();
            var response = (string) addin.Pub("UserOrderId=UT12345");
            Assert.IsTrue(response.StartsWith("#Error"), "UserOrderId is too long, but failed validation");

            response = (string)addin.Pub("UserOrderId=124 56");
            Assert.IsTrue(response.StartsWith("#Error"), "UserOrderId contains space, but failed validation");
            response = (string)addin.Pub("UserOrderId=126%89");
            Assert.IsTrue(response.StartsWith("#Error"), "UserOrderId contains %, but failed validation");

            response = (string)addin.Pub("UserOrderId=123456|orderDate=" + DateTime.Now.ToShortDateString());
            Assert.IsFalse(response.StartsWith("#Error"), "UserOrderId is valid but flagged as error: "+ response);
            addin.nmsClient.Stop();
            WaitFor(Disconnected, addin.nmsClient, 3000);
            Assert.IsFalse(addin.nmsClient.Connected(), "Failed to disconnect from broker");
        }

        [Test]
        public void testPublishNewOrder()
        {
            // disable the loading of the configuration - unit tests get random assembly names
            Configuration.loaded = true;
            var addin = new OrderPublisherAddin();
            Console.WriteLine(DateTime.Now + " started waiting");
            WaitFor(Connected, addin.nmsClient, 10000);
            Console.WriteLine(DateTime.Now + " finished waiting");

            Assert.IsTrue(addin.nmsClient.Connected(), "Failed to connect to broker");
            var sb = CreateOrderAsString();

            var response = (string)addin.Pub(sb);
            Assert.IsNotNull(response, "Failed to send order");
            Assert.IsFalse(isFailedPost(response), "Order not accepted: " + response);

            addin.nmsClient.Stop();
            WaitFor(Disconnected, addin.nmsClient, 3000);
        }

        [Test]
        public void testPublishCancelOrder()
        {
            Console.WriteLine("Starting 'testPublishCancelOrder'");
            // disable the loading of the configuration - unit tests get random assembly names
            Configuration.loaded = true;
            var addin = new OrderPublisherAddin();

            WaitFor(Connected, addin.nmsClient, 10000);
            Assert.IsTrue(addin.nmsClient.Connected(), "Failed to connect to broker");

            var sb = CreateOrderAsString();

            var response = (string)addin.PubCancel(sb);
            Assert.IsNotNull(response, "Failed to send order");
            Assert.IsFalse(isFailedPost(response), "Order not accepted: " + response);

            addin.nmsClient.Stop();
            WaitFor(Disconnected, addin.nmsClient, 3000);
            Console.WriteLine("Finished 'testPublishCancelOrder'");
        }

        [Test]
        public void testPublishReplaceOrder()
        {
            // disable the loading of the configuration - unit tests get random assembly names
            Configuration.loaded = true;
            var addin = new OrderPublisherAddin();
            WaitFor(Connected, addin.nmsClient, 10000);
            Assert.IsTrue(addin.nmsClient.Connected(), "Failed to connect to broker");

            var sb = CreateOrderAsString();

            var response = (string)addin.PubCancel(sb);
            Assert.IsNotNull(response, "Failed to send order");
            Assert.IsFalse(isFailedPost(response), "Order not accepted: " + response);

            addin.nmsClient.Stop();
            WaitFor(Disconnected, addin.nmsClient, 3000);
        }

        static string CreateOrderAsString() {
            var sb = new StringBuilder(1024);
            sb.Append("UserOrderId=O").Append(string.Format("{0:00000}", DateTime.Now.Millisecond));
            sb.Append("|OrderDate=" + DateTime.Now.ToShortDateString());
            sb.Append("|").Append(ORDER_STRING);

            return sb.ToString();
        }

        static bool isFailedPost(string response)
        {
            return (response.StartsWith("#Error") || response.StartsWith("#Warn"));
        }

    }
}
