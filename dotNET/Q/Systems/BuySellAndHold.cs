using Q.Trading;
using Q.Util;

namespace Q.Systems {
    public class BuySellAndHold : SymbolSystem {
        readonly int buyOrSell;
        readonly int tradeSize;

        public BuySellAndHold(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {
            buyOrSell = parameter<int>("buyOrSell");
            tradeSize = parameter<int>("TradeSize");
            if(!inputsValid()) deactivate();
        }

        protected override void onFilled(Position position, Trade trade) {
            
        }

        protected override void onNewBar() {
            if(hasPosition()) return;
            if (buyOrSell == 1) {
                placeOrder(symbol.buy("Buy Entry", market(), tradeSize, oneBar()));
                return;
            }
            if (buyOrSell == -1)
            {
                placeOrder(symbol.sell("Sell Entry", market(), tradeSize, oneBar()));
                return;
            }
            Bomb.toss("Bad Parameters should have deactivated system");
            
        }

        protected override void onNewTick(Bar partialBar, Tick tick) {
           
        }

        protected override void onClose() {
          
        }

        internal bool inputsValid() {
            return list(1, -1).Contains(buyOrSell);
        }
    }
}