using System;
using System.Collections.Generic;
using FixExecutionAddin;
using NUnit.Framework;

namespace FixExecutionAddinTest {
    [TestFixture]
    public class OrderTest
    {
        const string LIMIT_ORDER_QUANTITY_STR = "14.59";
        const string LIMIT_PRICE_STR = "12.54";

        [Test]
        public void TestCreateFromDictionary() {
            var order = new Order(CreateLimitOrderDictionary());
            var orderDictionary = order.ToDictionary();

            CheckOrder(new Order(orderDictionary));
        }

        [Test]
        public void TestConvertToDictionary()
        {
            var order = new Order(CreateLimitOrderDictionary());

            CheckOrder(order);
        }

        [Test]
        public void TestMergeOrder() {
            var order = new Order(CreateLimitOrderDictionary()) { errorMsg = "Should be gone!" };
            var nextOrder = new Order(CreateLimitOrderDictionary());

            var merged = order.MergeWith(nextOrder);

            Assert.IsNull(merged.errorMsg, "Failed to replace errorMsg");
        }

        [Test]
        public void TestClientOrderIdNewOrder() {
            var order = new Order(CreateLimitOrderDictionary());

            CheckOrder(order);

            // TODO We are not using the EMSX logic
            //Assert.AreEqual(order.CacheKey.Substring(8,2), "-0", "FixClientOrderId is invalid");
        }

        [Test]
        public void TestClientOrderIdCancelReplace()
        {
            var order = new Order(CreateLimitOrderDictionary()) {fixMessageType = "G"};

            CheckOrder(order);

            // TODO we are not using the EMSX logic
            //Assert.AreEqual(order.CacheKey.Substring(8, 2), "-1", "CompositOrderId is invalid");
        }

        static void CheckOrder(Order order) {
            Assert.IsNotNull(order.userOrderId, "Failed to populate UserOrderId");
            Assert.IsNotNull(order.side, "Failed to populate side");

            Assert.IsNotNull(order.orderType, "Failed to populate orderType");
            Assert.AreNotSame(order.limitPrice, -1, "Failed to populate limitPrice");
            Assert.AreNotSame(order.quantity, -1, "Failed to populate quantity");
            Assert.IsNotNull(order.symbol, "Failed to populate symbol");
            Assert.IsNotNull(order.timeInForce, "Failed to populate timeInForce");
            Assert.IsNotNull(order.side, "Failed to populate side");

            Assert.IsTrue(order.CanCalculateCompositOrderId, "Cannot calculate id - missing UserOrderdd or OrderDate");
            Assert.LessOrEqual(order.userOrderId.Length, 6, "UserOrderId Length is too long");
            Assert.IsFalse(order.ModifiedUserOrderId, "UserOrderId was modified");
            // TODO we are using the UserOrderId not the EMSX logic - we may not use EMSX logic at all
            //Assert.GreaterOrEqual(order.CacheKey.Length, 10, "CompositeOrderId Length is too short: " + order.CacheKey);
        }

        internal static IDictionary<string, string> CreateLimitOrderDictionary()
        {
            var orderRecord = new Dictionary<string, string> {
                {"UserOrderId", "O" + string.Format("{0:ssssss}", DateTime.Now)},
                {"Side", "BUY"},
                {"OrderType", "LIMIT"},
                {"LimitPrice", LIMIT_PRICE_STR},
                {"Quantity", LIMIT_ORDER_QUANTITY_STR},
                {"HandlingInst", "1"},
                {"Symbol", "ZVZZT"},
                {"SecurityType", "Equity"},
                {"TimeInForce", "DAY"},
                {"OrderDate", DateTime.Now.ToShortDateString()}
            };

            return orderRecord;
        }
    }
}