using Q.Systems.Examples;

namespace Q.Trading.Slippage {
    public class SlippageTestSystem : EmptySystem {
        Order nextOrder;

        public SlippageTestSystem(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {}
        protected override void onNewBar() {
            if(nextOrder == null) return;
            placeOrder(nextOrder);
            nextOrder = null;
        }
        public void setNextOrder(Order order) {
            nextOrder = order;
        }
    }
}