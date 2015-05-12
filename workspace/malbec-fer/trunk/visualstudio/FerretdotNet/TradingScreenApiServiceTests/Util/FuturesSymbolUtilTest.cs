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

            // Test the funny ric root
            var fullSymbol5 = FuturesSymbolUtil.CombineRootMaturityMonthYear("VX:VE", "201003");
            Assert.AreEqual("VXH0:VE", fullSymbol5, "Failed to combine symbol");
        }

        [Test]
        public void TestExtractMaturityMonthYear()
        {
            // Test everything that we might get
            const string mmy = "200906";

            var mmy1 = FuturesSymbolUtil.ExtractMaturityMonthFromSymbol("TYM9");
            Assert.AreEqual(mmy, mmy1);

            var mmy2 = FuturesSymbolUtil.ExtractMaturityMonthFromSymbol("P M9");
            Assert.AreEqual(mmy, mmy2);

            // do some RIC parsing
            var mmy3 = FuturesSymbolUtil.ExtractMaturityMonthFromSymbol("TTAH0");
            Assert.AreEqual("201003", mmy3);

            var mmy4 = FuturesSymbolUtil.ExtractMaturityMonthFromSymbol("6BH0" );
            Assert.AreEqual("201003", mmy4);

            var mmy5 = FuturesSymbolUtil.ExtractMaturityMonthFromSymbol("VXH0:VE");
            Assert.AreEqual("201003", mmy5);
        }

        [Test]
        public void TestExtractSymbolRoot() {
            // Test everything that we might get

            const string bbid = "TYM9 Comdty";
            const string ric = "TYM9";

            var root1 = FuturesSymbolUtil.ExtractSymbolRoot(bbid);
            Assert.AreEqual("TY", root1, "Failed to extract root from Bloomberg with Yellow key");

            var root2 = FuturesSymbolUtil.ExtractSymbolRoot(ric);
            Assert.AreEqual("TY", root2, "Failed to extract root from Bloomberg/RIC");

            const string shortRootSymbol = "P M9";
            var root3 = FuturesSymbolUtil.ExtractSymbolRoot(shortRootSymbol);
            Assert.AreEqual("P", root3, "Failed to extract root from a short symbol root");

            const string shortSymbol = "P";
            var root4 = FuturesSymbolUtil.ExtractSymbolRoot(shortSymbol);
            Assert.AreEqual("P", root4, "Failed to extract short root");

            const string shortSymbolSpace = "P ";
            var root4S = FuturesSymbolUtil.ExtractSymbolRoot(shortSymbolSpace);
            Assert.AreEqual("P", root4S, "Failed to extract short root");


            const string shortSymbol2 = "TU";
            var root41 = FuturesSymbolUtil.ExtractSymbolRoot(shortSymbol2);
            Assert.AreEqual("TU", root41, "Failed to extract short root");

            // do some RIC parsing
            const string bigRic = "TTAH0";

            var root5 = FuturesSymbolUtil.ExtractSymbolRoot(bigRic);
            Assert.AreEqual("TTA", root5, "Failed to extract big root from RIC");

            const string numbers = "6BH0";

            var root6 = FuturesSymbolUtil.ExtractSymbolRoot(numbers);
            Assert.AreEqual("6B", root6, "Failed to extract numeric root");

            // Try futures options
            const string futuresOptions = "TYH9C";

            var root7 = FuturesSymbolUtil.ExtractSymbolRoot(futuresOptions);
            Assert.AreEqual("TY_C", root7, "Failed to extract futures option root");

            // Try futures options
            const string futuresOptionsRoot = "TY_P";

            var root8 = FuturesSymbolUtil.ExtractSymbolRoot(futuresOptionsRoot);
            Assert.AreEqual("TY_P", root8, "Failed to extract futures option root");

            // Try futures options
            const string weirdSymbol = "ESH8 08";

            var root9 = FuturesSymbolUtil.ExtractSymbolRoot(weirdSymbol);
            Assert.AreEqual("ES", root9, "Failed to extract weird root");

            var root10 = FuturesSymbolUtil.ExtractSymbolRoot("VXH0:VE");
            Assert.AreEqual("VX", root10, "Failed to extract weird root");
        }
    }
}
