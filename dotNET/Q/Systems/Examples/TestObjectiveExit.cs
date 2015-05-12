using NUnit.Framework;
using Q.Trading;
using O=Q.Util.Objects;

namespace Q.Systems.Examples {
    [TestFixture]
    public class TestObjectiveExit : OneSymbolSystemTest<ExitSystem>{
        [Test]
        public void testObjectiveExit() {
            processBar(15, 15, 15, 15);
            processBar(0,0,0,0);
            hasOrders(symbol().buy("buy stuff", market(), 100, fillOrKill()));
            fill(0, 10); // bought 100 at 10, so exit should be at 20
            AreEqual(20, O.the(symbolSystem.dynamicExits)[0]);
            hasOrders(position().exit("ObjectiveReached", limit(20), oneBar()));
            processBar(5,5,5,5);
            hasOrders(position().exit("ObjectiveReached", limit(20), oneBar()));
            processBar(15,15,15,15);
            AreEqual(20, O.the(symbolSystem.dynamicExits)[0]);
            hasOrders(position().exit("ObjectiveReached", limit(20), oneBar()));
            processBar(0,0,0,0);
            hasOrders(Order.ANY, position().exit("get out", market(), fillOrKill()));
            fill(1, 0);
            noOrders();
            processBar(-5, -5, -5, -5);
            hasOrders(symbol().sell("sell stuff", market(), 100, fillOrKill()));
            fill(0, -10);
            hasOrders(position().exit("ObjectiveReached", limit(-20), oneBar()));
            processBar(-5,-5,-5,-5);
            hasOrders(position().exit("ObjectiveReached", limit(-20), oneBar()));
            processBar(-15,-15,-15,-15);
            hasOrders(position().exit("ObjectiveReached", limit(-20), oneBar()));
        }

        protected override int leadBars() {
            return 0;
        }
    }

    public class ExitSystem : EmptySystem {
        public ExitSystem(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {}

        protected override void onNewBar() {
            if(hasPosition()) {
                if(bars.close == 0)
                    placeOrder(position().exit("get out", market(), fillOrKill()));
                return;
            }
            if(bars.close >= 0)
                placeOrder(symbol.buy("buy stuff", market(), 100, fillOrKill()));
            else
                placeOrder(symbol.sell("sell stuff", market(), 100, fillOrKill()));
        }

        protected override void onFilled(Position position, Trade trade) {
            if(trade.description.Equals("get out")) return;
            addDynamicExit(new ObjectiveExit(position, bars.close, trade.price + position.direction() * 10, "ObjectiveReached"), false);
        }

    }
}
