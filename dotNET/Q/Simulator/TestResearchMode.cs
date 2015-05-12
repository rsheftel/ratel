using NUnit.Framework;
using Q.Trading;
using Q.Util;
using Market=systemdb.metadata.Market;
using O=Q.Util.Objects;

namespace Q.Simulator {
    [TestFixture]
    public class TestResearchMode : DbTestCase {
        [Test]
        public void testCanRunSystem() {
            var className = "Q.Systems.NDayBreak";
            var parameters = new Parameters(className) {
                {"ATRLen", 10},
                {"ATRlong", 100},
                {"BreakDays", 30},
                {"FirstDayATR", 1},
                {"FixEquity", 1},
                {"InitEquity", 6000000},
                {"LeadBars", 50},
                {"MaxPyramid", 1},
                {"Risk", 0.02},
                {"nATR", 2},
                {"upATR", 2},
                {"systemId", 39}
            };
            var markets = O.list(new Symbol(new Market("RE.TEST.TY.1C")));
            var arguments = new SystemArguments(markets, parameters);
            var simulator = new Simulator(arguments, "NOSENDMESSAGES");
            simulator.processBars(300);
            AlmostEqual(293099.30, simulator.pnl(), 0.01);
        }
    }
}
