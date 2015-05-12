using System;
using System.Text;
using FixExecutionAddin;
using FixExecutionAddinTest.Nms;
using NUnit.Framework;

namespace FixExecutionAddinTest {
    [TestFixture]
    public class OrderPublisherAddinTest : AbstractNmsSetupTest
    {
        const string OrderString = "Side=BUY|OrderType=LIMIT|LimitPrice=89.09|Quantity=11|Symbol=ZZVTV|SecurityType=Equity|platform=TESTSERVER";

        /// <summary>
        /// Validate the combination of Redi ClientOrderId requirements along with Bloomberg's
        /// EMSX ClientOrderId requirements.
        /// 
        /// 
        /// </summary>
        [Test]
        public void TestValidateUserOrderId() {
            // disable the loading of the configuration - unit tests get random assembly names
            var config = new AppConfiguration { BrokerUrl = Nyws802 };
            var addin = new OrderPublisherAddin(config);
            var response = (string) addin.Pub("UserOrderId=UT12345");
            Assert.IsTrue(response.StartsWith("#Error"), "UserOrderId is too long, but failed validation");

            response = (string)addin.Pub("UserOrderId=124 56");
            Assert.IsTrue(response.StartsWith("#Error"), "UserOrderId contains space, but failed validation");
            response = (string)addin.Pub("UserOrderId=126%89");
            Assert.IsTrue(response.StartsWith("#Error"), "UserOrderId contains %, but failed validation");

            response = (string)addin.Pub("UserOrderId=123456|orderDate=" + DateTime.Now.ToShortDateString());
            Assert.IsFalse(response.StartsWith("#Error"), "UserOrderId is valid but flagged as error: "+ response);
            addin._nmsClient.Stop();
            WaitFor(Disconnected, addin._nmsClient, 3000);
            Assert.IsFalse(addin._nmsClient.Connected(), "Failed to disconnect from broker");
        }

        [Test]
        public void TestPublishNewOrder()
        {
            // disable the loading of the configuration - unit tests get random assembly names
            var config = new AppConfiguration { BrokerUrl = Nyws802 };
            var addin = new OrderPublisherAddin(config);

            Console.WriteLine(DateTime.Now + " started waiting");
            WaitFor(Connected, addin._nmsClient, 10000);
            Console.WriteLine(DateTime.Now + " finished waiting");

            Assert.IsTrue(addin._nmsClient.Connected(), "Failed to connect to broker");
            var sb = CreateOrderAsString();

            var response = (string)addin.Pub(sb);
            Assert.IsNotNull(response, "Failed to send order");
            Assert.IsFalse(IsFailedPost(response), "Order not accepted: " + response);

            addin._nmsClient.Stop();
            WaitFor(Disconnected, addin._nmsClient, 3000);
        }

        [Test]
        public void TestPublishCancelOrder()
        {
            Console.WriteLine("Starting 'TestPublishCancelOrder'");
            // disable the loading of the configuration - unit tests get random assembly names
            var config = new AppConfiguration { BrokerUrl = Nyws802 };
            var addin = new OrderPublisherAddin(config);

            WaitFor(Connected, addin._nmsClient, 10000);
            Assert.IsTrue(addin._nmsClient.Connected(), "Failed to connect to broker");

            var sb = CreateOrderAsString();

            var response = (string)addin.PubCancel(sb);
            Assert.IsNotNull(response, "Failed to send order");
            Assert.IsFalse(IsFailedPost(response), "Order not accepted: " + response);

            addin._nmsClient.Stop();
            WaitFor(Disconnected, addin._nmsClient, 3000);
            Console.WriteLine("Finished 'TestPublishCancelOrder'");
        }

        [Test]
        public void TestPublishReplaceOrder()
        {
            // disable the loading of the configuration - unit tests get random assembly names
            var config = new AppConfiguration { BrokerUrl = Nyws802 };
            var addin = new OrderPublisherAddin(config);

            WaitFor(Connected, addin._nmsClient, 10000);
            Assert.IsTrue(addin._nmsClient.Connected(), "Failed to connect to broker");

            var sb = CreateOrderAsString();

            var response = (string)addin.PubCancel(sb);
            Assert.IsNotNull(response, "Failed to send order");
            Assert.IsFalse(IsFailedPost(response), "Order not accepted: " + response);

            addin._nmsClient.Stop();
            WaitFor(Disconnected, addin._nmsClient, 3000);
        }

        static string CreateOrderAsString() {
            var sb = new StringBuilder(1024);
            sb.Append("UserOrderId=O").Append(string.Format("{0:00000}", DateTime.Now.Millisecond));
            sb.Append("|OrderDate=" + DateTime.Now.ToShortDateString());
            sb.Append("|").Append(OrderString);

            return sb.ToString();
        }

        static bool IsFailedPost(string response)
        {
            return (response.StartsWith("#Error") || response.StartsWith("#Warn"));
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
}