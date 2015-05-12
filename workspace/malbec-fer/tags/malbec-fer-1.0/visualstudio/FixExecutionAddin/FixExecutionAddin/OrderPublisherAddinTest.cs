using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using NUnit.Framework;

namespace FixExecutionAddin
{
    [TestFixture]
    public class OrderPublisherAddinTest
    {
        const string ORDER_STRING = "Side=BUY|OrderType=LIMIT|LimitPrice=89.09|Quantity=11|Symbol=ZZVTV|SecurityType=Equity|platform=TESTSERVER";

        [Test]
        public void testValidateClientOrderID() {
            var addin = new OrderPublisherAddin();
            var response = (string) addin.Pub("clientOrderID=12345678901234567");
            Assert.IsTrue(response.StartsWith("#Error"), "ClientOrderID is too long, but failed validation");
            response = (string)addin.Pub("clientOrderID=123456 890123456");
            Assert.IsTrue(response.StartsWith("#Error"), "ClientOrderID contains space, but failed validation");
            response = (string)addin.Pub("clientOrderID=123456%890123456");
            Assert.IsTrue(response.StartsWith("#Error"), "ClientOrderID contains %, but failed validation");

            response = (string)addin.Pub("clientOrderID=123456890123456");
            Assert.IsFalse(response.StartsWith("#Error"), "ClientOrderID is valid but flagged as error");
        }

        [Test]
        public void testPublish()
        {
            var addin = new OrderPublisherAddin();
            var sb = new StringBuilder(1024);
            sb.Append("ClientOrderID=CS-").Append(DateTime.Now.Millisecond);
            sb.Append("|").Append(ORDER_STRING);

            var response = (string)addin.Pub(sb.ToString());
            Assert.IsNotNull(response, "Failed to send order");
            Assert.IsFalse(response.StartsWith("#Error"), "Order not accepted: "+ response);
            
        }

    }
}
