using System;
using NUnit.Framework;
using Q.Trading;
using O=Q.Util.Objects;

namespace Q.Systems.Examples {
    [TestFixture]
    public class TestPriceTrailingStop : OneSymbolSystemTest<TestPriceTrailingStop.StopSystem>{
        public class StopSystem : EmptySystem {
            public StopSystem(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {
                bars.close.prepare();
            }

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
                addDynamicExit(new PriceTrailingStop(   position, bars.close, position.entry().price - position.direction()*10,
                                                        "TrailingStoppage"), false);
            }
        }

        [Test]
        public void testStopWorks() {
            processBar(15, 15, 15, 15);
            processBar(0,0,0,0);
            hasOrders(symbol().buy("buy stuff", market(), 100, fillOrKill()));
            fill(0, 20); // bought 100 at 20, so stop should be at 10
            AreEqual(10, O.the(symbolSystem.dynamicExits)[0]);
            hasOrders(position().exit("TrailingStoppage", stop(10), oneBar()));
            processBar(5,5,5,5);
            hasOrders(position().exit("TrailingStoppage", stop(10), oneBar()));
            processBar(15,15,15,15);
            AreEqual(10, O.the(symbolSystem.dynamicExits)[0]);
            AreEqual(10, O.the(symbolSystem.dynamicExits)[1]);
            AreEqual(10, O.the(symbolSystem.dynamicExits)[2]);
            hasOrders(position().exit("TrailingStoppage", stop(10), oneBar()));
            processBar(0,0,0,0);
            hasOrders(Order.ANY, position().exit("get out", market(), fillOrKill()));
            fill(1, 0);
            noOrders();
            processBar(-5, -5, -5, -5);
            hasOrders(symbol().sell("sell stuff", market(), 100, fillOrKill()));
            fill(0, -100);
            hasOrders(position().exit("TrailingStoppage", stop(-90), oneBar()));
            processBar(-95,-95,-95,-95);
            hasOrders(position().exit("TrailingStoppage", stop(-90), oneBar()));
            processBar(-92,-92,-92,-92);
            hasOrders(position().exit("TrailingStoppage", stop(-90), oneBar()));
        }

        protected override int leadBars() {
            return 0;
        }
    }

    [TestFixture]
    public class TestPriceTrailingStopClose : OneSymbolSystemTest<TestPriceTrailingStopClose.StopSystem>{
        Bar lastBar;

        public class StopSystem : EmptySystem {
            public StopSystem(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {
                bars.close.prepare();
            }

            public override bool runOnClose() { return true; }
            protected override void onClose() {
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
                addDynamicExit(new PriceTrailingStop(   position, bars.close, position.entry().price - position.direction()*10, 
                                                        "TrailingStoppage"), true);
            }
        }

        public override void setUp() {
            base.setUp();
            O.zeroTo(leadBars(), i=> processBar(0, 0, 0, 0));
        }
    
        [Test]
        public void testStopOnCloseWorks() {
            close(15);
            close(0);
            hasOrders(symbol().buy("buy stuff", market(), 100, fillOrKill()));
            fill(0, 60); // bought 100 at 60, so stop should be at 50
            noOrders();
            close(55);
            hasOrders(position().exit("TrailingStoppage", stop(50), oneBar()));
            close(52);
            AreEqual(50, O.the(symbolSystem.dynamicExitsOnClose)[0]);
            AreEqual(50, O.the(symbolSystem.dynamicExitsOnClose)[1]);
            AreEqual(50, O.the(symbolSystem.dynamicExitsOnClose)[2]);
            hasOrders(position().exit("TrailingStoppage", stop(50), oneBar()));
            close(0);
            hasOrders(Order.ANY, position().exit("get out", market(), fillOrKill()));
            fill(1, 0);
            noOrders();
            close(-5);
            hasOrders(symbol().sell("sell stuff", market(), 100, fillOrKill()));
            fill(0, -70);
            noOrders();
            close(-90);
            hasOrders(position().exit("TrailingStoppage", stop(-60), oneBar()));
            close(-75);
            hasOrders(position().exit("TrailingStoppage", stop(-60), oneBar()));
        }

        void close(int close) {
            if(lastBar != null) {
                processBar(lastBar);
                lastBar = new Bar(lastBar.close, lastBar.close, lastBar.close, lastBar.close);
                lastBar = lastBar.update(new Tick(close, 0, default(DateTime)));
            } else {
                lastBar = new Bar(close, close, close, close);
            }
            noOrders();
            processClose(lastBar);
        }

        protected override int leadBars() {
            return 2;
        }
    }
}