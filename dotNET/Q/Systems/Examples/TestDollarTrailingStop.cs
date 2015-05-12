using System;
using NUnit.Framework;
using Q.Spuds.Core;
using Q.Trading;
using O=Q.Util.Objects;

namespace Q.Systems.Examples {
        [TestFixture]
    public class TestDynamicExitsWithNativeCurrencies : TestDollarTrailingStopBase {
            protected override bool runInNativeCurrency() {
                return true;
            }

            [Test]
            public void testStopWorks() {
                processBar(15, 15, 15, 15);
                processBar(0,0,0,0);
                hasOrders(symbol().buy("buy stuff", market(), 100, fillOrKill()));
                fill(0, 10); // bought 100 at 10, so stop should be at 0
                AreEqual(0, O.the(symbolSystem.dynamicExits)[0]);
                hasOrders(position().exit("TrailingStoppage", stop(0), oneBar()));
                processBar(5,5,5,5);
                hasOrders(position().exit("TrailingStoppage", stop(0), oneBar()));
                processBar(15,15,15,15);
                AreEqual(5, O.the(symbolSystem.dynamicExits)[0]);
                AreEqual(0, O.the(symbolSystem.dynamicExits)[1]);
                AreEqual(0, O.the(symbolSystem.dynamicExits)[2]);
                hasOrders(position().exit("TrailingStoppage", stop(5), oneBar()));
                processBar(0,0,0,0);
                hasOrders(Order.ANY, position().exit("get out", market(), fillOrKill()));
                fill(1, 0);
                noOrders();
                processBar(-5, -5, -5, -5);
                hasOrders(symbol().sell("sell stuff", market(), 100, fillOrKill()));
                fill(0, -10);
                hasOrders(position().exit("TrailingStoppage", stop(0), oneBar()));
                processBar(-5,-5,-5,-5);
                hasOrders(position().exit("TrailingStoppage", stop(0), oneBar()));
                processBar(-15,-15,-15,-15);
                hasOrders(position().exit("TrailingStoppage", stop(-5), oneBar()));
            }


    }

    public class TestDollarTrailingStopBase : OneSymbolSystemTest<TestDollarTrailingStopBase.StopSystem> {
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
                addDynamicExit(new DollarTrailingStop(position, bars.close, 1e6, "TrailingStoppage", bridge), false);
            }
        }

        DateTime nextBarDate = default(DateTime);
        SymbolSpud<Bar> fxSpud;

        public override void setUp() {
            base.setUp();
            nextBarDate = date("2009/05/05");
            if (arguments().runInNativeCurrency) return;
            fxSpud = bridge().fxRates.get(symbol());
            fxSpud.enterTestMode();
        }

        protected void bar(double fx, double close) {

            fxSpud.overwrite(nextBarDate, new Bar(0, 0, 0, fx, nextBarDate));
            processBar(close, close, close, close, nextBarDate);
            nextBarDate = nextBarDate.AddDays(1);
        }

        protected override Symbol initializeSymbol() {
            var symbol = base.initializeSymbol();
            symbol.setCurrencyForTest("EUR");
            return symbol;
        }

        protected override int leadBars() {
            return 0;
        }
    }

    [TestFixture]
    public class TestDollarTrailingStop : TestDollarTrailingStopBase{
        [Test]
        public void testStopWorks() {
            bar(1, 15);
            bar(2, 0);
            hasOrders(symbol().buy("buy stuff", market(), 100, fillOrKill()));
            fill(0, 10); // bought 100 at 10
            hasOrders(position().exit("TrailingStoppage", stop(5), oneBar()));
            bar(1, 10);
            hasOrders(position().exit("TrailingStoppage", stop(10), oneBar()));
            bar(3, 15);
            hasOrders(position().exit("TrailingStoppage", stop(35/3.0), oneBar()));
            bar(99, 0);
            hasOrders(Order.ANY, position().exit("get out", market(), fillOrKill()));
            fill(1, 0); 
            noOrders();
            bar(2, -5);
            hasOrders(symbol().sell("sell stuff", market(), 100, fillOrKill()));
            fill(0, -10);
            hasOrders(position().exit("TrailingStoppage", stop(-5), oneBar()));
            bar(1, -10);
            hasOrders(position().exit("TrailingStoppage", stop(-10), oneBar()));
            bar(3, -15);
            hasOrders(position().exit("TrailingStoppage", stop(-35/3.0), oneBar()));
        }
    }

    [TestFixture]
    public class TestDollarTrailingStopClose : OneSymbolSystemTest<TestDollarTrailingStopClose.StopSystem>{
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
                addDynamicExit(new DollarTrailingStop(position, bars.close, 1e6, "TrailingStoppage", bridge), true);
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
            fill(0, 10); // bought 100 at 10, so stop should be at 0
            noOrders();
            close(5);
            hasOrders(position().exit("TrailingStoppage", stop(0), oneBar()));
            close(15);
            AreEqual(5, O.the(symbolSystem.dynamicExitsOnClose)[0]);
            AreEqual(0, O.the(symbolSystem.dynamicExitsOnClose)[1]);
            AreEqual(0, O.the(symbolSystem.dynamicExitsOnClose)[2]);
            hasOrders(position().exit("TrailingStoppage", stop(5), oneBar()));
            close(0);
            hasOrders(Order.ANY, position().exit("get out", market(), fillOrKill()));
            fill(1, 0);
            noOrders();
            close(-5);
            hasOrders(symbol().sell("sell stuff", market(), 100, fillOrKill()));
            fill(0, -10);
            noOrders();
            close(-5);
            hasOrders(position().exit("TrailingStoppage", stop(0), oneBar()));
            close(-15);
            hasOrders(position().exit("TrailingStoppage", stop(-5), oneBar()));
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