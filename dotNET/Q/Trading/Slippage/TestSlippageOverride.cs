using NUnit.Framework;

namespace Q.Trading.Slippage {
    [TestFixture]
    public class TestSlippageOverride : TestTimeVaryingSlippageBase {
        protected override Symbol initializeSymbol() {
            var symbol = base.initializeSymbol();
            symbol.setSlippageForTest(1.0);
            return symbol;
        }

        [Test]
        public void testOverrideWorks() {
            buySellSamePrice("2009/05/18", 2.0);
        }
    }
}