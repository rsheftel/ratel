using NUnit.Framework;
using Q.Trading;

namespace Q.Systems.Examples {
    [TestFixture]
    public class TestSameLookingPositionCanGetFilledTwice 
        : OneSymbolSystemTest<TestSameLookingPositionCanGetFilledTwice.MySystem> {

        [Test]
        public void testSameLookingPositionCanGetFilledTwice() {
            processBar(new Bar(0, 0, 0, 0));
            hasOrders(1);
            fill(0, 1);
            HasCount(1, positions());
            processBar(new Bar(0, 0, 0, 0));
            hasOrders(1);
            fill(0, 1);
            HasCount(2, positions());
        }

        public class MySystem : EmptySystem {
            public MySystem(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {}

            protected override void onNewBar() {
                placeOrder(symbol.buy("foo", market(), 1, oneBar()));
            }
        }

        protected override int leadBars() {
            return 0;
        }
    }
}
