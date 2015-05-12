using System;
using Q.Trading;
using Q.Util;

namespace Q.Systems.Examples {
    public class OnCloseExample : SymbolSystem {
        bool firstBar = true;

        public OnCloseExample(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {}

        protected override void onNewBar() {
            Bomb.unless(isEmpty(positions()), () => "this system should not carry positions overnight");
            placeOrder(symbol.buy("buy at open", market(), 1, fillOrKill()));
            firstBar = false;
        }

        public override bool runOnClose() { return true; }
        protected override void onClose() {
            if(firstBar) return;
            placeOrder(position().exit("sell at close", stop(bar.close+1), fillOrKill()));
        }

        protected override void onNewTick(Bar partialBar, Tick tick) {
            throw new NotImplementedException();
        }
        protected override void onFilled(Position position, Trade trade) {
            LogC.info("filled " + trade + "\nbar: " + bar);
        }
    }
}