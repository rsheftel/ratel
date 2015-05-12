using Q.Trading;
using System;

namespace Q.Systems {
    public class TrendingMAClose : TrendingMABase {
        public TrendingMAClose(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol, bars => bars.close) {}

        protected override void onFilled(Position position, Trade trade) {
            cancelAllOrders();
            if (position.isEntry(trade))
                addDynamicExit(new DollarTrailingStop(position, bars.close, riskDollars, "Exit "+lOrS()+" StopOut", bridge), true);
        }

        protected override void onNewBar() {
        }
        
        protected override void onNewTick(Bar partialBar, Tick tick) {
        }

        protected override void onClose() {
           
            if (hasPosition()) placeExits(); 
            else placeEntries();
        }
        
        protected override void placeEntries() {
            if (ma > upperBand) placeOrder(symbol.buy("Upper Range Break", stop(signal), tradeSize(), oneBar()));
            if (ma < lowerBand) placeOrder(symbol.sell("Lower Range Break", stop(signal), tradeSize(), oneBar()));       
        }

        protected override void placeExits() {
            var reversalExit = Math.Round((upperBand + lowerBand) / 2, 4);
            if ((ma - reversalExit) * position().direction() < 0) 
                placeOrder(position().exit("Exit " + lOrS() + " Reversal", stop(signal), oneBar()));
        }

        public override bool runOnClose() {
            return true;
        }
        
        
        
    }
}