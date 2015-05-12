using System.Text.RegularExpressions;
using Q.Trading;

namespace Q.Systems {
    public class NDayBreak : NDayBreakBase {
        public NDayBreak(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol, bars => bars.high, bars => bars.low) {}

        protected override void onNewBar() {
            tradingLogic();
        }
        
        protected void tradingLogic() {
            if (isEmpty(positions())) placeInitialOrders();
            else placeStopsAndPyramids();
        }

        protected override void onNewTick(Bar partialBar, Tick tick) {}
        protected override void onClose() {}

        protected override void onFilled(Position position, Trade trade) {
            base.onFilled(position, trade);
            if (!initialEntry(trade)) return;

            cancelAllOrders();
            placeFirstDayExit(position);
        }

        void placeFirstDayExit(Position position) {
            double entryPrice = position.longShort(breakOutHigh, breakOutLow);
            var exitPrice = entryPrice - position.direction() * parameter<double>("FirstDayATR") * atr;
            placeOrder(position.exit("1st Day E" + lOrS(), protectiveStop(exitPrice), oneBar()));
        }

        static bool initialEntry(Trade trade) {
            return Regex.IsMatch(trade.description, "BreakOut.*");
        }
    }
}