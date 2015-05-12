using NUnit.Framework;

namespace Q.Trading.Slippage {
    [TestFixture]
    public class TestTimeVaryingSlippage : TestTimeVaryingSlippageBase {
        [Test]
        public void testSlippageWorks() {
            buySellSamePrice("2009/05/18", 0.3);
        }
    }
}
