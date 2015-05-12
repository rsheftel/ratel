namespace Q.Trading.Slippage {
    public class TimesTwo : Fixed {
        public TimesTwo(Symbol symbol, BarSpud bars) : base(symbol, bars) {}
        public override double slippage() {
            return 2 * base.slippage();
        }
    }
}