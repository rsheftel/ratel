using Q.Systems;
using Q.Util;
using systemdb.metadata;

namespace Q.Trading.Slippage {
    public class TestTimeVaryingSlippageBase : OneSymbolSystemTest<SlippageTestSystem> {
        protected override Symbol initializeSymbol() {
            var symbol = base.initializeSymbol();
            MarketTable.MARKET.setSlippageCalculator(symbol.name, typeof (BasicTimeVaryingSlippage).FullName);
            return symbol;
        }

        protected override int leadBars() {
            return 0;
        }

        class BasicTimeVaryingSlippage : SlippageCalculator {
            public BasicTimeVaryingSlippage(Symbol symbol, BarSpud bars) : base(symbol, bars) {}
            public override double slippage() {
                return 0.1 * (int) bars[0].time.DayOfWeek;
            }
        }

        protected void buySellSamePrice(string dateString, double expectedSlippage) {
            symbolSystem.setNextOrder(symbol().buy("buy some", market(), 10, oneBar()));
            var date = Objects.date(dateString); // Monday
            processBar(0, 0, 0, 0, date);
            fill(0, 100.00);
            date = date.AddDays(1);
            symbolSystem.setNextOrder(position().exit("get out", market(), oneBar()));
            processBar(0, 0, 0, 0, date);
            fill(0, 100.0);
            date = date.AddDays(1);
            processBar(0, 0, 0, 0, date);
            AlmostEqual(-expectedSlippage * 10 * symbol().bigPointValue, bridge().statistics().netProfit(), 0.000001);
        }
    }
}