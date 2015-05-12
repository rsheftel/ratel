using NUnit.Framework;
using Q.Util;
using O = Q.Util.Objects;

namespace Q.Trading.Slippage {
    [TestFixture]
    public class TestCdsSlippageTable : DbTestCase {
        [Test]
        public void testCdsSlippageTable() {
            var spreadBps = O.list(double.NegativeInfinity,-1.0, 0.0, 50.0, 100.0, 125.0, 500.0, 1000.0, 1200.0, double.PositiveInfinity);
            var expectedSlippages = O.list(3.0, 3.0, 3.0, 6.5, 10.0, 10.8333, 20.0, 30.0, 30.0, 30.0);
            O.each(spreadBps, expectedSlippages, 
                (bp, slippage) => AlmostEqual(slippage, CdsSlippageTable.slippageInBps(bp), 0.0001));
        }
        
    }
}