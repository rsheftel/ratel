using Q.Util;

namespace Q.Trading.Slippage {
    public abstract class SlippageCalculator : Objects {
        protected readonly Symbol symbol;
        protected readonly BarSpud bars;

        protected SlippageCalculator(Symbol symbol, BarSpud bars) {
            this.symbol = symbol;
            this.bars = bars;
        }

        public abstract double slippage();
    }
}