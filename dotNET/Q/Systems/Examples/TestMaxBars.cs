using NUnit.Framework;
using Q.Trading;
using Q.Util;

namespace Q.Systems.Examples {
    [TestFixture]
    public class TestMaxBars : OneSymbolSystemTest<MaxBarsSystem>{
        [Test]
        public void testMaxBars() {
            var slippage = 0.1;
            symbol().setSlippageForTest(slippage);
            processBar(15, 15, 15, 15);
            processBar(0,0,0,0);
            hasOrders(symbol().buy("buy stuff", market(), 100, fillOrKill()));
            fill(0, 10);
            AreEqual(0 - 2 * slippage, Objects.the(symbolSystem.dynamicExits)[0]);
            noOrders();
            processBar(5,5,5,5);
            noOrders();
            processBar(15,15,15,15);
            AreEqual(15 - 2 * slippage, Objects.the(symbolSystem.dynamicExits)[0]);
            hasOrders(position().exit("MaxBars", limit(15 - 2 * slippage), oneBar()));
            processBar(0,0,0,0);
            hasOrders(Order.ANY, position().exit("get out", market(), fillOrKill()));
            fill(1, 0);
            noOrders();
            processBar(-5, -5, -5, -5);
            hasOrders(symbol().sell("sell stuff", market(), 100, fillOrKill()));
            fill(0, -10);
            AreEqual(-5 + 2 * slippage, Objects.the(symbolSystem.dynamicExits)[0]);
            noOrders();
            processBar(-5,-5,-5,-5);
            noOrders();
            processBar(-15,-15,-15,-15);
            hasOrders(position().exit("MaxBars", limit(-15 + 2 * slippage), oneBar()));
        }

        protected override int leadBars() {
            return 0;
        }

        protected override Parameters parameters() {
            return base.parameters().overwrite("maxBarsInTrade", "2");
        }
    }

    public class MaxBarsSystem : EmptySystem {
        public MaxBarsSystem(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {}

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
            if(!position.isEntry(trade)) return;
            addDynamicExit(new MaxBarsExit(position, bars.close, parameter<int>("maxBarsInTrade"), 2 * slippage(position.symbol), "MaxBars"), false);
        }

    }
}