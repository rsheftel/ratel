using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;
using O = Q.Util.Objects;

namespace Q.Systems {
    [TestFixture]
    public class TestNDayBreak : OneSymbolSystemTest<NDayBreak> {

        public override void setUp() {
            base.setUp();
            O.zeroTo(arguments().leadBars, i => {
                processBar(1, 3, 1, 2);
                noOrders();
            });
        }

        [Test]
        public void testStopsAndScaleUps() {
            processBar(1, 3, 1, 2);
            hasOrders(2);
            fill(0, 3.0);
            processBar(1, 3, 1, 2);
            hasOrders(
                buy("ScaleUp L", protectiveStop(5.0), 500, oneBar()),
                sell("Stop EL", protectiveStop(-1.0), 500, oneBar()),
                sell("BreakDn EL", protectiveStop(1.0), 500, oneBar())
            );
            fill(0, 5.0);
            processBar(1, 3, -4, 2);
            hasOrders(
                sell("Stop EL", protectiveStop(5.0 - 2.0 * symbolSystem.atr), 1000, oneBar()),
                sell("BreakDn EL", protectiveStop(-4), 1000, oneBar())
            );
            processBar(1, 3, -400, 2);
            hasOrders(Order.ANY, Order.ANY, sell("Vol Double EL", limit(2 - symbolSystem.atr), 1000, oneBar()));
        }

        [Test]
        public void testInitialOrders() {
            AreEqual(2.0, symbolSystem.atr[0]);
            processBar(1, 3, 1, 2);
            hasOrders(
                buy("BreakOut L", protectiveStop(3.0), 500, oneBar()),
                sell("BreakOut S", protectiveStop(1.0), 500, oneBar())
            );
            processBar(1, 2, 1, 2);
            processBar(1, 2, 1, 2);

            hasOrders(
                buy("BreakOut L", protectiveStop(3.0), 534, oneBar()),
                sell("BreakOut S", protectiveStop(1.0), 534, oneBar())
            );
            fill(0, 3.0);
            var firstDayExit = 3.0 - 0.5 * symbolSystem.atr;
            hasOrders(
                sell("1st Day EL", protectiveStop(firstDayExit), 534, oneBar())
            );
            fill(0, 2.5);
            noOrders();
            processBar(1,2,1,2);
            fill(1, 1.0);
            var level = 1.0 + 0.5 * symbolSystem.atr;
            hasOrders(buy("1st Day ES", protectiveStop(level), 551, oneBar()));
            fill(0, 1.5);
        }

        [Test]
        public void testTick() {
            processBar(1, 3, 1, 2);
            var preTick = orders();
            processTick(1.0);
            AreEqual(preTick, orders());
            processTick(3.0);
            AreEqual(preTick, orders());
            emailer.allowMessages();
            fill(0, 3.0);
            var level = 3.0 - 0.5 * symbolSystem.atr;
            hasOrders(
                sell("1st Day EL", protectiveStop(level), 500, oneBar())
            );

        }

        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                { "BreakDays", 20.0 },
                { "ATRLen", 10.0 },
                { "Risk", 0.02 },
                { "nATR", 2.0 },
                { "upATR", 1.0 },
                { "MaxPyramid", 2.0 },
                { "FirstDayATR", 0.5 },
                { "ATRlong", 100.0 },
                { "InitEquity", 100000000.0 },
                { "FixEquity", 1.0 }
            });
        }

        protected override int leadBars() {
            return 5;
        }
    }
}
