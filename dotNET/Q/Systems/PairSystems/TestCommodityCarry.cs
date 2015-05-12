using NUnit.Framework;
using Q.Trading;
using Q.Util;
using O=Q.Util.Objects;

namespace Q.Systems.PairSystems {
    [TestFixture]
    public class TestCommodityCarry : OnePairSystemTest<CommodityCarry, CommodityCarryPairGenerator> {

        public override void setUp() {
            base.setUp();
            pairSystem.enterTestMode();
        }
        
        protected override Pair initializePair() {
            insertMarket("RE.TEST.TY.2C", 0);
            insertMarket("RE.TEST.TY.4C", 0);
            return base.initializePair();
        }

        [Test]
        public void testSomething() {
            close(99.2, 98.4, 98.2, 98.2);
            close(99.7, 98.3, 98.5, 98.2);
            close(100.2, 98.4, 99.1, 98.2);
            close(99.7, 98.5, 98.5, 98.2);
            noOrders();
            close(99.2, 98.5, 98.1, 98.2);
            hasOrders(pair().buy("long payout achieved", limit(98.5), limit(98.2), 1000, oneBar()));
            fill(0, 98.5, 98.2);
            close(99.2, 98.5, 98, 98.2);
            noOrders();
            close(98.7, 98.1, 97.3, 97.6);
            close(98.3, 97.6, 97, 97.1);
            close(98, 97, 191, 54);
            var expected = new Order[4];
            expected[0] = position(pair().left).exit("exit long small payout ratio", limit(97), oneBar());
            expected[2] = position(pair().right).exit("exit long small payout ratio", limit(54), oneBar());
            expected[1] = pair().sell("short payout achieved", limit(97), limit(54), 1000, oneBar())[0];
            expected[3] = pair().sell("short payout achieved", limit(97), limit(54), 1000, oneBar())[1];
            hasOrders(expected);
            fill(0, 97, 54);
            fill(0, 97, 54);
            close(98, 97, 91, 84);
            hasOrders(
                position(pair().left).exit("exit short small payout ratio", limit(97), oneBar()),
                position(pair().right).exit("exit short small payout ratio", limit(84), oneBar())
            );
            // manager
        }

        protected override Parameters parameters() {
            return base.parameters()
                .overwrite("volWindow", "5")
                .overwrite("payoutRatioCutoff", "0.4");
        }

        void close(double c2, double c3, double c4, double c5) {
            var date = nextTime();
            pairSystem.leftPrior.add(date, new Bar(c2, date));
            pairSystem.rightPrior.add(date, new Bar(c4, date));
            processClose(c3, c5);
        }

        protected override int leadBars() {
            return 4;
        }
    }

    [TestFixture]
    public class TestCommodityCarryPairGenerator : DbTestCase {
        [Test]
        public void testPairGenerator() {
            var symbols = O.list(new Symbol("TY.1C"), new Symbol("TY.2C"), new Symbol("TY.3C"), new Symbol("TY.4C"));
            var pg = new CommodityCarryPairGenerator(new SystemArguments(symbols, new Parameters {
                {"LeadBars", 0},
                {"systemId", LIVE_SYSTEM.id()}
            }));
            AreEqual(O.list(
                new Pair(symbols[0], symbols[1]), 
                new Pair(symbols[0], symbols[2]), 
                new Pair(symbols[0], symbols[3]), 
                new Pair(symbols[1], symbols[2]),
                new Pair(symbols[1], symbols[3]),
                new Pair(symbols[2], symbols[3])
            ), O.list(pg.pairs()));
        }
    }

}
