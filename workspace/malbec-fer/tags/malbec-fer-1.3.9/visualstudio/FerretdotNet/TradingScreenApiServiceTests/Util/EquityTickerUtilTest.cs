using NUnit.Framework;
using TradingScreenApiService.Util;

namespace TradingScreenApiServiceTests.Util
{
    [TestFixture]
    public class EquityTickerUtilTest
    {
        [Test]
        public void TestRicToTicker() {
            Assert.AreEqual("IBM", EquityTickerUtil.ConvertRicToTicker("IBM.N"));
            Assert.AreEqual("MSFT", EquityTickerUtil.ConvertRicToTicker("MSFT.O"));
            Assert.AreEqual("XLP", EquityTickerUtil.ConvertRicToTicker("XLP.P"));
            Assert.AreEqual("F", EquityTickerUtil.ConvertRicToTicker("F.N"));
            Assert.AreEqual("C", EquityTickerUtil.ConvertRicToTicker("C.N"));
        }
    }
}
