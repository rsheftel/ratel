using System.Collections;
using System.Collections.Generic;
using NUnit.Framework;
using TradingScreenApiService.Position;
using TradingScreenApiService.Util;

namespace TradingScreenApiServiceTests.Position
{
    [TestFixture]
    public class PositionCacheTest
    {

        [Test]
        public void TestPositionUpdate() {

            var positionCache = new PositionCache();
            var tradeRecord = MessageUtil.CreateTradeRecord("IBM", "Buy", 100, 110.89, "QF.Test");
            var position = positionCache.Update(tradeRecord);
            Assert.AreEqual(1, positionCache.Count);

            VerifyPositionRecord(position);

            Assert.AreEqual("IBM", position["BID"]);
            Assert.AreEqual("QF.Test", position["Strategy"]);
            Assert.AreEqual("100", position["SharesBought"]);
            Assert.AreEqual("0", position["SharesSold"]);
            Assert.AreEqual("100", position["Position"]);

            // Lets double up
            var doubledPosition = positionCache.Update(tradeRecord);
            Assert.AreEqual(1, positionCache.Count);
            VerifyPositionRecord(doubledPosition);
            Assert.AreEqual("IBM", doubledPosition["BID"]);
            Assert.AreEqual("QF.Test", doubledPosition["Strategy"]);
            Assert.AreEqual("200", doubledPosition["SharesBought"]);
            Assert.AreEqual("0", doubledPosition["SharesSold"]);
            Assert.AreEqual("200", doubledPosition["Position"]);

            // Add the other side
            var tradeRecord3 = MessageUtil.CreateTradeRecord("IBM", "Sell", 50, 111.64, "QF.Test");
            var position3 = positionCache.Update(tradeRecord3);
            Assert.AreEqual(1, positionCache.Count);

            VerifyPositionRecord(position3);
            Assert.AreEqual("IBM", position3["BID"]);
            Assert.AreEqual("QF.Test", position3["Strategy"]);
            Assert.AreEqual("200", position3["SharesBought"]);
            Assert.AreEqual("50", position3["SharesSold"]);
            Assert.AreEqual("150", position3["Position"]);

            // Add a different position
            var tradeRecord4 = MessageUtil.CreateTradeRecord("MSFT", "Sell", 50, 55.32, "QF.Test");
            var position4 = positionCache.Update(tradeRecord4);
            Assert.AreEqual(2, positionCache.Count);

            VerifyPositionRecord(position3);
            Assert.AreEqual("MSFT", position4["BID"]);
            Assert.AreEqual("QF.Test", position4["Strategy"]);
            Assert.AreEqual("0", position4["SharesBought"]);
            Assert.AreEqual("50", position4["SharesSold"]);
            Assert.AreEqual("-50", position4["Position"]);

            var positions = positionCache.Positions;
            Assert.IsNotNull(positions);
            Assert.AreEqual(2, positions.Count);

            // purge
            positionCache.Clear();
            Assert.AreEqual(0, positionCache.Count);
        }

        static void VerifyPositionRecord(IDictionary<string, string> position) {
            Assert.IsNotNull(position);
            Assert.Contains("BID", (ICollection) position.Keys);
            Assert.Contains("Strategy", (ICollection)position.Keys);
            Assert.Contains("SharesBought", (ICollection)position.Keys);
            Assert.Contains("SharesSold", (ICollection)position.Keys);
            Assert.Contains("Position", (ICollection)position.Keys);
        }
    }

}
