using NUnit.Framework;
using Q.Util;

namespace Q.Trading {
    [TestFixture]
    public class TestPosition : DbTestCase {
        static readonly Symbol symbol = new Symbol("RE.TEST.TY.1C", 1000);

        [Test]
        public void testPnlCalc() {
            var position = new Position(symbol);
            addTrade(position, Direction.LONG, 100, 25.50);
            addTrade(position, Direction.SHORT, 50, 24.50); // -50
            addTrade(position, Direction.LONG, 200, 23.50);
            addTrade(position, Direction.SHORT, 125, 26.50); // 50 + 225 = 275
            addTrade(position, Direction.SHORT, 125, 25.50); // 250
            AlmostEqual(475.0 * 1000 - 0.015625 * 600 * 1000, position.pnl(true, false), 0.001);
        }

        static void addTrade(Position position, Direction direction, int size, double price) {
            var order = new Order("desc", symbol, new Market(), direction, size, OneBar.ONE).placed();
            order.refersTo(position);
            var trade = new Trade(order, price, size, 0.015625, 1);
            order.fill(trade);
        }
    }
}
