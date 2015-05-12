using System.Collections.Generic;
using NUnit.Framework;
using Q.Systems.PairSystems;
using Q.Trading;
using O=Q.Util.Objects;

namespace Q.Systems.MultiPairSystems {
    [TestFixture]
    public class TestCommodityCarryMaxTradesPerContract : OneSystemTest<CommodityCarryMaxTradesPerContract> {
        static readonly Symbol symbol1 = new Symbol("RE.TEST.TY.1C", 1000);
        static readonly Symbol symbol2 = new Symbol("RE.TEST.TY.2C", 1000);
        static readonly Symbol symbol3 = new Symbol("RE.TEST.TY.3C", 1000);
        static readonly Symbol symbol4 = new Symbol("RE.TEST.TY.4C", 1000);
        static readonly Symbol symbol5 = new Symbol("RE.TEST.TY.5C", 1000);

        public override void setUp() {
            base.setUp();
            O.eachValue(system().systems_, s=> s.enterTestMode());
        }

        [Test]
        public void testMaxTrades() {
            close(100, 99.2, 98.4, 98.2, 98.2);	 // 5/25/2009,
            close(100, 99.7, 98.3, 98.5, 98.2);	 // 5/26/2009,
            close(100, 100.2, 98.4, 99.1, 98.2); // 5/27/2009,
            close(100, 99.7, 98.5, 98.5, 98.2);	 // 5/28/2009,
            close(100, 99.2, 98.5, 98.1, 98.2);	 // 5/29/2009,
            hasOrders(
                symbol3.buy("long payout achieved", limit(98.5), 1000, oneBar()),
                symbol5.sell("long payout achieved", limit(98.2), 1000, oneBar())
            );
            fill(symbol3, 0, 98.5);
            fill(symbol5, 0, 98.2);
            close(100, 99.2, 98.5, 98, 98.2);	 // 6/1/2009, 
            close(99.5, 98.7, 98.1, 97.3, 97.6); // 6/2/2009, 
            close(99, 98.3, 97.6, 97, 97.1);	 // 6/3/2009, 
            close(100, 98, 97, 91, 84);		 // 6/4/2009, 
            hasOrders(
                symbol2.buy("long payout achieved", limit(98), 1000, oneBar()),
                symbol3.sell("long payout achieved", limit(97), 1000, oneBar()),
                symbol3.sell("exit long small payout ratio", limit(97), 1000, oneBar()),
                symbol5.buy("exit long small payout ratio", limit(84), 1000, oneBar())
            );
            fill(symbol2, 0, 98);
            fill(symbol3, 0, 97);
            fill(symbol3, 0, 97);
            fill(symbol5, 0, 84);
            close(99, 97, 96.9, 90, 80);	 // 6/5/2009, 
            close(99, 97, 96.8, 90, 80);	 // 6/8/2009, 
            hasOrders(
                symbol2.sell("exit long small payout ratio", limit(97), 1000, oneBar()),
                symbol3.buy("exit long small payout ratio", limit(96.8), 1000, oneBar())
            );
            fill(symbol2, 0, 97);
            fill(symbol3, 0, 96.8);
            close(99.2, 96.8, 96.7, 89.9, 80.1); // 6/9/2009, 
            close(99.1, 96.9, 96.8, 89.8, 80);	 // 6/10/2009,
            hasOrders(
                symbol2.sell("short payout achieved", limit(96.9), 1000, oneBar()),
                symbol3.sell("short payout achieved", limit(96.8), 1000, oneBar()),
                symbol4.buy("short payout achieved", limit(89.8), 1000, oneBar()),
                symbol4.buy("short payout achieved", limit(89.8), 1000, oneBar())
            );
            fill(symbol2, 0, 96.9);
            fill(symbol3, 0, 96.8);
            fill(symbol4, 0, 89.8);
            fill(symbol4, 0, 89.8);
            close(99, 96.8, 96.8, 89.9, 81);	 // 6/11/2009,
            close(99, 95, 94, 92, 90);		 // 6/12/2009,
            hasOrders(
                symbol2.buy("long payout achieved", limit(95), 1000, oneBar()),
                symbol2.buy("exit short small payout ratio", limit(95), 1000, oneBar()),
                symbol3.sell("long payout achieved", limit(94), 1000, oneBar()),
                symbol3.buy("exit short small payout ratio", limit(94), 1000, oneBar()),
                symbol4.sell("exit short small payout ratio", limit(92), 1000, oneBar()),
                symbol4.sell("exit short small payout ratio", limit(92), 1000, oneBar())
            );

        }

        void close(double c1, double c2, double c3, double c4, double c5) {
            var date = nextTime();
            var bars = new Dictionary<Symbol, Bar> {
                {symbol1, new Bar(c1, date)},
                {symbol2, new Bar(c2, date)},
                {symbol3, new Bar(c3, date)},
                {symbol4, new Bar(c4, date)},
                {symbol5, new Bar(c5, date)},
            };
            O.each(system().systems_, (pair, pairSystem) => {
                pairSystem.leftPrior.add(date, bars[CommodityCarry.prior(pair.left)]);
                pairSystem.rightPrior.add(date, bars[CommodityCarry.prior(pair.right)]);
            });
            bars.Remove(symbol1);
            processClose(lastBar => bars);
        }
        protected override SystemArguments arguments() {
            return new SystemArguments(O.list(symbol2, symbol3, symbol4, symbol5), parameters());
        }

        protected override Parameters parameters() {
            return base.parameters()
                .overwrite("volWindow", "5")
                .overwrite("payoutRatioCutoff", "0.4")
                .overwrite("maxTradesPerContract", "2");
        }

        protected override void initializeSymbols() {
            insertMarket("RE.TEST.TY.2C", 0);
            insertMarket("RE.TEST.TY.3C", 0);
            insertMarket("RE.TEST.TY.4C", 0);
            insertMarket("RE.TEST.TY.5C", 0);
        }

        protected override int leadBars() {
            return 4;
        }
    }
}
