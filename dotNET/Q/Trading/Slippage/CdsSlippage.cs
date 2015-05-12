using Q.Spuds.Core;


namespace Q.Trading.Slippage {
    public class CdsSlippage : SlippageCalculator {
        readonly SymbolSpud<double> spreads;
        readonly SymbolSpud<double> dv01;

        public CdsSlippage(Symbol symbol, BarSpud bars) : base(symbol, bars) {
            spreads = symbol.relatedSuffix("SPREAD").doubles(bars);
            dv01 = symbol.relatedSuffix("DV01").doubles(bars);
        }
        public override double slippage() {
            var bpSlippage = CdsSlippageTable.slippageInBps(spreads[0] * 10000);
            if (!dv01.hasContent()) return 1000;
            return bpSlippage * dv01[0];
        }
    }
}