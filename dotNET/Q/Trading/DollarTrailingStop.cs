using System;
using Q.Spuds.Core;
using Q.Spuds.Indicators;

namespace Q.Trading {
    public class DollarTrailingStop : DynamicExit {
        readonly Spud<double> close;
        readonly double stopLoss;
        readonly QREBridgeBase bridge;
        readonly Spud<double> highWaterMark;
        readonly Spud<double> tradePnl;

        public DollarTrailingStop(Position position, Spud<double> close, double stopLoss, string name, QREBridgeBase bridge) 
            : base(position, name, STOP, close.manager) 
        {
            this.close = close;
            this.stopLoss = stopLoss;
            this.bridge = bridge;
            tradePnl = close.transform(price => position.pnlNoSlippage(price, bridge.arguments().runInNativeCurrency, bridge.fxRate(position.symbol)));
            highWaterMark = dependsOn(new Max(tradePnl));
        }

        protected override double exitLevel() {
            return position.priceAtPnlNoSlippage(Math.Max(0, highWaterMark) - stopLoss, bridge.arguments().runInNativeCurrency, bridge.fxRate(position.symbol));
        }

        protected override void cleanup() {
            highWaterMark.removeChild(this);
            tradePnl.removeChild(highWaterMark);
            close.removeChild(tradePnl);
            manager.remove(highWaterMark);
            manager.remove(tradePnl);
        }
    }
}