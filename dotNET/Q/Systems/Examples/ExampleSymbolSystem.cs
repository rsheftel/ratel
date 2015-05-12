using Q.Spuds.Core;
using Q.Trading;
using Q.Util;

namespace Q.Systems.Examples {
    public class ExampleSymbolSystem : SymbolSystem {
        public readonly Spud<double> highest;
        internal bool doRunOnTick = true;

        public ExampleSymbolSystem(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {
            var lookback = parameter<int>("lookback");
            highest = bars.high.highest(lookback);
        }

        protected override void onNewBar() {
            trade();
        }

        public override bool runOnNewTick() {
            return doRunOnTick;
        }

        void trade() {
            if (highest.changed()) {
                if(hasPosition()) return;
                placeOrder(symbol.buy("enter long", stop(highest), 100, fillOrKill()));
            } else if(hasPosition()){
                placeOrder(position().exit("exit long", market(), fillOrKill()));
                placeOrder(position().exit("exit long 2", market(), fillOrKill()));
            }
        }

        protected override void onNewTick(Bar partialBar, Tick tick) {
            trade();
        }
        protected override void onClose() {}
        public override System.DateTime onCloseTime() {
            throw Bomb.toss("e3ek");
        }
        protected override void onFilled(Position position, Trade trade) {}
    }
}
