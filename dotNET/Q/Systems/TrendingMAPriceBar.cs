using System;
using Q.Spuds.Core;
using Q.Trading;

namespace Q.Systems {

    public class TrendingMAPriceBar : TrendingMABase {
        public TrendingMAPriceBar(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol, priceSeries) {}
//      (high + low + close) / 3 
        static Spud<double>  priceSeries(BarSpud bars) {
            var highLow = new Plus(bars.high, bars.low);
            return new Divide(new Plus(bars.close, highLow), bars.constant(3.0));
        }

        protected override void onFilled(Position position, Trade trade) {
            cancelAllOrders();
            if (position.isEntry(trade))
                addDynamicExit(new DollarTrailingStop(position, bars.close, riskDollars, "Exit "+lOrS()+" StopOut", bridge), false);
        }

        protected override void onNewBar() {
            if (hasPosition()) placeExits(); 
            else placeEntries();
        }
        
        protected override void onNewTick(Bar partialBar, Tick tick) {
        }

        protected override void onClose()
        {
            throw new NotImplementedException();
        }
        
        public override bool runOnClose() {
            return false;
        }

        protected override void placeEntries() {
            var entryUp = Math.Round(maDays * upperBand - shortSum, 4);
            var entryDn = Math.Round(maDays * lowerBand - shortSum, 4);
            info(commaSep(upperBand[0],lowerBand[0],shortSum[0]));
            info(signal.last10());
            placeOrder(symbol.buy("Upper Range Break", protectiveStop(entryUp), tradeSize(), oneBar()));
            placeOrder(symbol.sell("Lower Range Break", protectiveStop(entryDn), tradeSize(), oneBar()));
        }

        protected override void placeExits() {
            var reversalExit = Math.Round(maDays * (upperBand + lowerBand) / 2 - shortSum, 4);
            info(signal.last10());
            placeOrder(position().exit("Exit " + lOrS() + " Reversal", protectiveStop(reversalExit), oneBar()));
        }
    }        
}
