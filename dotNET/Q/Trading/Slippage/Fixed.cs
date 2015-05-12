using Q.Util;
using systemdb.metadata;

namespace Q.Trading.Slippage {
    public class Fixed : SlippageCalculator {
        static readonly LazyDictionary<Symbol, double> fixedSlippages = new LazyDictionary<Symbol, double>(s => MarketTable.MARKET.fixedSlippage(s.name));

        public Fixed(Symbol symbol, BarSpud bars) : base(symbol, bars) {}

        public static void clearCache() {
            fixedSlippages.clear();
        }

        public static void setSlippageForTest(Symbol symbol, double newSlippage) {
            fixedSlippages.overwrite(symbol, newSlippage);
        }

        public override double slippage() {
            return fixedSlippages.get(symbol);
        }
    }
}
