using NUnit.Framework;
using TradingScreenApiService.Util;

namespace TradingScreenApiServiceTests.Util
{
    [TestFixture]
    public class FuturesSymbolUtilTest
    {
        [Test]
        public void TestCombineRootMaturityMonthYear()
        {
            // Test everything that we might get

            const string ric = "TY";
            const string mmy = "200906";

            var fullSymbol1 = FuturesSymbolUtil.CombineRootMaturityMonthYear(ric, mmy);
            Assert.AreEqual(fullSymbol1, "TYM9", "Failed to combine symbol");

            var fullSymbol2 = FuturesSymbolUtil.CombineRootMaturityMonthYear("P", mmy);
            Assert.AreEqual(fullSymbol2, "P M9", "Failed to combine symbol");

            // do some RIC parsing

            var fullSymbol3 = FuturesSymbolUtil.CombineRootMaturityMonthYear("TTA", "201003");
            Assert.AreEqual(fullSymbol3, "TTAH0", "Failed to combine symbol");

            var fullSymbol4 = FuturesSymbolUtil.CombineRootMaturityMonthYear("6B", "201003");
            Assert.AreEqual(fullSymbol4, "6BH0", "Failed to combine symbol");

        }
    }
}
