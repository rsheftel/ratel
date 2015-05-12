using System;
using System.Collections.Generic;
using System.IO;
using System.Reflection;
using NUnit.Framework;
using TradingScreenApiService.Util;

namespace TradingScreenApiServiceTests.Util
{
    [TestFixture]
    public class MessageUtilTest
    {

        [Test]
        public void TestOrderXmlToOrders() {

            var fullFileContents = LoadTestOrders();

            // do the test
            var orders = MessageUtil.ParseXmlToDictionary(fullFileContents, 1);
            Assert.IsNotNull(orders);
            Assert.AreEqual(5, orders.Count);

            var equityOrder = orders[0];
            Assert.IsTrue(MessageUtil.IsEquityOrder(equityOrder));

            VerifyAllFields(orders);
            equityOrder["BID"] = EquityTickerUtil.ConvertRicToTicker(equityOrder["RIC"]);

            var tradeRecord = MessageUtil.CreateTradeRecord(equityOrder);
            Assert.IsNotNull(tradeRecord);
            Assert.AreEqual("MSFT", tradeRecord["BID"]);
        }

        internal static string LoadTestOrders() {
            var currentCodeBase = new Uri(Assembly.GetExecutingAssembly().CodeBase);
            var baseDirectory = Path.GetDirectoryName(currentCodeBase.AbsolutePath);
            var fullFilePath = Path.Combine(baseDirectory, @"..\..\TradingScreenOrders.xml");

            Console.WriteLine(currentCodeBase);
            Console.WriteLine(fullFilePath);

            var sr = new StreamReader(new FileStream(fullFilePath, FileMode.Open, FileAccess.Read));
            var fullFileContents = sr.ReadToEnd();
            sr.Close();

            return fullFileContents;
        }

        static void VerifyAllFields(IEnumerable<IDictionary<string, string>> orders) {
            foreach (var order in orders) {
                Assert.IsNotNull(order["RIC"]);
                Assert.IsNotNull(order["ExecutedQuantity"]);
                Assert.IsNotNull(order["ExecutedValue"]);
                Assert.IsNotNull(order["ClearingBroker"]);
                Assert.IsNotNull(order["PlatformOrderId"]);
                Assert.IsNotNull(order["Side"]);
                Assert.IsNotNull(order["SecurityType"]);
                Assert.IsNotNull(order["Exchange"]);
                Assert.IsNotNull(order["Strategy"]);
            }
        }
    }
}
