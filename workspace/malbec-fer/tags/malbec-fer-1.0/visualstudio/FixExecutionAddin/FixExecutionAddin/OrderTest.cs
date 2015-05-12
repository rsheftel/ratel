using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using NUnit.Framework;

namespace FixExecutionAddin
{
    [TestFixture]
    public class OrderTest
    {
        const string LIMIT_ORDER_QUANTITY_STR = "14.59";
        const string LIMIT_PRICE_STR = "12.54";

        [Test]
        public void testCreateFromDictionary() {
            var order = new Order(createLimitOrderDictionary());
            var orderDictionary = order.ToDictionary();

            CheckOrder(new Order(orderDictionary));
        }

        [Test]
        public void testConvertToDictionary()
        {
            var order = new Order(createLimitOrderDictionary());

            CheckOrder(order);
        }

        static void CheckOrder(Order order) {
            Assert.IsNotNull(order.ClientOrderID, "Failed to populate ClientOrderID");
            Assert.IsNotNull(order.side, "Failed to populate side");

            Assert.IsNotNull(order.orderType, "Failed to populate orderType");
            Assert.AreNotSame(order.limitPrice, -1, "Failed to populate limitPrice");
            Assert.AreNotSame(order.quantity, -1, "Failed to populate quantity");
            Assert.IsNotNull(order.symbol, "Failed to populate symbol");
            Assert.IsNotNull(order.securityIDSource, "Failed to populate securityIDSource");
            Assert.IsNotNull(order.timeInForce, "Failed to populate timeInForce");
            Assert.IsNotNull(order.side, "Failed to populate side");
        }

        private static IDictionary<string, string> createLimitOrderDictionary()
        {
            var orderRecord = new Dictionary<string, string> {
                {"ClientOrderID", "UT-" + DateTime.Now.ToShortTimeString()},
                {"Side", "BUY"},
                {"OrderType", "LIMIT"},
                {"LimitPrice", LIMIT_PRICE_STR},
                {"Quantity", LIMIT_ORDER_QUANTITY_STR},
                {"HandlingInst", "1"},
                {"Symbol", "ZZVTV"},
                {"SecurityType", "Equity"},
                {"TimeInForce", "DAY"}
            };

            return orderRecord;
        }
    }
}
