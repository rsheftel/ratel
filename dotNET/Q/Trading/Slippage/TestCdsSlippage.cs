using NUnit.Framework;
using systemdb.metadata;

namespace Q.Trading.Slippage {
    [TestFixture]
    public class TestCdsSlippage : TestTimeVaryingSlippageBase {
        protected override Symbol initializeSymbol() {
            var symbol = new Symbol("CDS.TEST.CAH.5Y.ACB20", 10000);
            MarketTable.MARKET.setSlippageCalculator(symbol.name, typeof (CdsSlippage).FullName);
            return symbol;
        } 
    
        [Test]
        public void testCdsSlippage() {
            var slippage0726 = 0.38987221146;
            var slippage0727 = 0.462809254736;
            buySellSamePrice("2004/07/26", (slippage0726 + slippage0727));
        }
    }

    [TestFixture]
    public class TestCdsSlippageNoDv01 : TestTimeVaryingSlippageBase {
        protected override Symbol initializeSymbol(){
            var symbol = new Symbol("CDS.ALTEL.5Y.ACB20", 10000);
            MarketTable.MARKET.setSlippageCalculator(symbol.name, typeof (CdsSlippage).FullName);
            return symbol;
        }

        [Test]
        public void testCdsSlippageNoDv01() {
            var slippage0717 = 1000;
            var slippage0718 = 0.1970715513935;

            buySellSamePrice("2006/07/17", slippage0717 + slippage0718);
        }
    }
}