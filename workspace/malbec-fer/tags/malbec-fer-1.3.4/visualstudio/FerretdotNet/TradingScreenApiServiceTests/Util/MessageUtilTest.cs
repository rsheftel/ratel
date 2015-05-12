using System.Collections.Generic;
using System.IO;
using NUnit.Framework;
using TradingScreenApiService.Util;

namespace TradingScreenApiServiceTests.Util
{
    [TestFixture]
    public class MessageUtilTest
    {

        [Test]
        public void TestOrderXmlToOrders() {

            var currentDirectory = Directory.GetCurrentDirectory();
            var fullFilePath = Path.Combine(currentDirectory, @"..\..\TradingScreenOrders.xml");

            var sr = new StreamReader(new FileStream(fullFilePath, FileMode.Open, FileAccess.Read));
            var fullFileContents = sr.ReadToEnd();
            sr.Close(); 

            // do the test
            var orders = MessageUtil.ParseXmlToDictionary(fullFileContents, 1);
            Assert.IsNotNull(orders);
            Assert.AreEqual(4, orders.Count);

            VerifyAllFields(orders);

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
