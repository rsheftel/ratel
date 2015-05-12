using NUnit.Framework;
using Q.Util;

namespace Q.Trading {
    [TestFixture]
    public class TestOrderDetails : DbTestCase {
        [Test]
        public void testMarket() {
            var order = Order.market();
            IsTrue(order.canFill(Direction.LONG, 1.0));
            IsTrue(order.canFill(Direction.SHORT, 1.0));
            AreEqual(1.0, order.fillPrice(1.0, true));
            AreEqual(1.0, order.fillPrice(1.0, false));
        }

        [Test]
        public void testStop() {
            var order = Order.stop(1.0);
            IsFalse(order.canFill(Direction.LONG, 0.99));
            IsTrue(order.canFill(Direction.LONG, 1.0));
            IsFalse(order.canFill(Direction.SHORT, 1.01));
            IsTrue(order.canFill(Direction.SHORT, 1.0));
            AreEqual(2.0, order.fillPrice(2.0, true));
            AreEqual(1.0, order.fillPrice(2.0, false));
        }
        
        [Test]
        public void testLimit() {
            var order = Order.limit(1.0);
            IsFalse(order.canFill(Direction.LONG, 1.01));
            IsTrue(order.canFill(Direction.LONG, 1.0));
            IsFalse(order.canFill(Direction.SHORT, 0.99));
            IsTrue(order.canFill(Direction.SHORT, 1.0));
            AreEqual(2.0, order.fillPrice(2.0, true));
            AreEqual(1.0, order.fillPrice(2.0, false));
        }
                
        [Test]
        public void testStopLimitSame() {
            var converts = Order.stopLimit(1.0, 1.0);
            IsFalse(converts.canFill(Direction.LONG, 0.99));
            IsFalse(converts.canFill(Direction.LONG, 1.01)); // here we become a limit order
            IsTrue(converts.canFill(Direction.LONG, 0.99));
            var immediate = Order.stopLimit(1.0, 1.0);
            IsFalse(immediate.canFill(Direction.LONG, 0.99));
            IsTrue(immediate.canFill(Direction.LONG, 1.0));
        }
        
        [Test]
        public void testStopLimitSameShort() {
            var converts = Order.stopLimit(1.0, 1.0);
            IsFalse(converts.canFill(Direction.SHORT, 1.01));
            IsFalse(converts.canFill(Direction.SHORT, 0.99)); // here we become a limit order
            IsTrue(converts.canFill(Direction.SHORT, 1.01));
            var immediate = Order.stopLimit(1.0, 1.0);
            IsFalse(immediate.canFill(Direction.SHORT, 1.01));
            IsTrue(immediate.canFill(Direction.SHORT, 1.0));
        }

        [Test]
        public void testProtectiveStopSame() {
            var converts = Order.protectiveStop(1.0, 1.0);
            IsFalse(converts.canFill(Direction.LONG, 0.99));
            IsTrue(converts.canFill(Direction.LONG, 1.01));
            var immediate = Order.protectiveStop(1.0, 1.0);
            IsFalse(immediate.canFill(Direction.LONG, 0.99));
            IsTrue(immediate.canFill(Direction.LONG, 1.0));
        }
        
        [Test]
        public void testProtectiveStopSameShort() {
            var converts = Order.protectiveStop(1.0, 1.0);
            IsFalse(converts.canFill(Direction.SHORT, 1.01));
            IsTrue(converts.canFill(Direction.SHORT, 0.99));
            var immediate = Order.protectiveStop(1.0, 1.0);
            IsFalse(immediate.canFill(Direction.SHORT, 1.01));
            IsTrue(immediate.canFill(Direction.SHORT, 1.0));
        }

        [Test]
        public void testProtectiveStop() {
            var order = Order.protectiveStop(1.0, 2.0);
            IsFalse(order.canFill(Direction.LONG, 0.99));
            IsTrue(order.canFill(Direction.LONG, 1.01));
            AreEqual(1.0, order.fillPrice(2.0, false));
        }
        
        [Test]
        public void testProtectiveStopShort() {
            var order = Order.protectiveStop(1.0, 0.0);
            IsFalse(order.canFill(Direction.SHORT, 1.01));
            IsTrue(order.canFill(Direction.SHORT, 0.99));
            AreEqual(1.0, order.fillPrice(2.0, false));
        }

        [Test]
        public void testStopLimitDifferent() {
            var order = Order.stopLimit(3.0, 1.0);
            IsFalse(order.canFill(Direction.LONG, 0.99));
            IsFalse(order.canFill(Direction.LONG, 3.0));
            IsFalse(order.canFill(Direction.LONG, 1.01));
            IsTrue(order.canFill(Direction.LONG, 1.0));
            AreEqual(1.0, order.fillPrice(2.0, false));
            var otherWay = Order.stopLimit(1.0, 3.0);
            IsFalse(otherWay.canFill(Direction.LONG, 0.99));
            IsTrue(otherWay.canFill(Direction.LONG, 1.0));
        }
        
        [Test]
        public void testStopLimitDifferentShort() {
            var order = Order.stopLimit(1.0, 3.0);
            IsFalse(order.canFill(Direction.SHORT, 3.01));
            IsFalse(order.canFill(Direction.SHORT, 1.0));
            IsFalse(order.canFill(Direction.SHORT, 2.99));
            IsTrue(order.canFill(Direction.SHORT, 3.0));
            AreEqual(3.0, order.fillPrice(2.0, false));
            var otherWay = Order.stopLimit(3.0, 1.0);
            IsFalse(otherWay.canFill(Direction.SHORT, 3.01));
            IsTrue(otherWay.canFill(Direction.SHORT, 3.0));
        }
    }
}
