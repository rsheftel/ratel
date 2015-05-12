using NUnit.Framework;
using Q.Trading;
using O=Q.Util.Objects;

namespace Q.Systems.Examples {
    [TestFixture]
    public class TestOrderDuration : OneSymbolSystemTest<EmptySystem> {
        [Test]
        public void testOrderDuration() {
            var fillKill = symbol().buy("fill or kill", stop(10), 100, fillOrKill());
            processBar(1,1,1,1);
            symbolSystem.placeOrder(fillKill);
            hasOrders(fillKill);
            processTick(1);
            noOrders();
            fillKill = symbol().buy("fill or kill", stop(10), 100, fillOrKill());
            symbolSystem.placeOrder(fillKill);
            processBar(1, 1, 1, 1);
            noOrders();
            var bar = symbol().buy("one bar", stop(10), 100, oneBar());
            symbolSystem.placeOrder(bar);
            hasOrders(bar);
            processTick(1);
            hasOrders(bar);
            processTick(1);
            hasOrders(bar);
            O.freezeNow("2001/02/03 04:05:06");
            processBar(1, 1, 1, 1, O.now());
            noOrders();
            var dayOrder = symbol().buy("day", stop(10), 100, oneDay());
            symbolSystem.placeOrder(dayOrder);
            hasOrders(dayOrder);
            processBar(1,1,1,1, O.now().AddHours(1));
            hasOrders(dayOrder);
            processBar(1,1,1,1, O.now().AddDays(1));
            noOrders();
        }

        protected override int leadBars() {
            return 0;
        }
    }

    public class EmptySystem : SymbolSystem {
        public EmptySystem(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {
            bars.close.prepare();
        }
        protected override void onNewBar() {}
        protected override void onNewTick(Bar partialBar, Tick tick) {}
        protected override void onClose() {}
        protected override void onFilled(Position position, Trade trade) {}
        public override bool runOnNewTick() {
            return true;
        }
    }
}