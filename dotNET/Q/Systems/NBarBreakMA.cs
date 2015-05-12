using System.Drawing;
using Q.Spuds.Indicators;
using Q.Trading;

namespace Q.Systems {
    public class NBarBreakMA : NDayBreakBase {
        private readonly EWMA maSlow;
        private readonly EWMA maFast;

        public NBarBreakMA(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol, bars => bars.high, bars => bars.low) {
            maSlow = new EWMA(bars.close, parameter<double>("MASlow"));
            maFast = new EWMA(bars.close, parameter<double>("MAFast"));
            addToPlot(maSlow,"maSlow",Color.BlueViolet);
            addToPlot(maFast,"maFast",Color.DeepPink);
        }

        protected override void onNewBar() {
            if (hasPosition()) placeStopsAndPyramids();
            else placeEntryOrders();
        }

        protected void placeEntryOrders() {
            if (maFast >= maSlow) placeLongEntry();
            if (maFast <= maSlow) placeShortEntry();
        }

        protected override void onNewTick(Bar partialBar, Tick tick) {}
        protected override void onClose() {}

        protected override void onFilled(Position position, Trade trade) {
            base.onFilled(position, trade);
            if (!position.isEntry(trade)) return;
            cancelAllOrders();
        }
    }
}