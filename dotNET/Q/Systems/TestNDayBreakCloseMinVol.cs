using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;
using O = Q.Util.Objects;

namespace Q.Systems {
    [TestFixture]
    public class TestNDayBreakCloseMinVol : OneSymbolSystemTest<NDayBreakCloseMinVol> {

        public override void setUp() {
            base.setUp();
            O.zeroTo(arguments().leadBars, i => {
                processBar(1, 3, 1, 2);
                noOrders();
            });
        }

        [Test]
        public void testStopsAndScaleUps() {
            processBar(2, 3, 1, 1);
            noOrders();
            processClose(1, 3, 1, 2);
            hasOrders(2);
            fill(0, 3.0);
            processBar(1, 3, 1, 2);
            processClose(1, 3, 1, 2);
            hasOrders(
                buy("ScaleUp L", protectiveStop(5.0), 500, oneBar()),
                sell("Stop EL", protectiveStop(-1.0), 500, oneBar()),
                sell("BreakDn EL", protectiveStop(1.0), 500, oneBar())
            );
            fill(0, 5.0);
            processBar(1, 3, 1, 2);
            processClose(2, 2, 2, 2);
            var level = 5.0 - 2.0 * symbolSystem.atr;
            hasOrders(
                sell("Stop EL", protectiveStop(level), 1000, oneBar()),
                sell("BreakDn EL", protectiveStop(1.0), 1000, oneBar())
            );
            processBar(1, 3, -4, 2);
            processClose(1, 3, -400, 2);
            hasOrders(Order.ANY, Order.ANY, sell("Vol Double EL", limit(2 - symbolSystem.atr), 1000, oneBar()));
        }

        [Test]
        public void testInitialOrders() {
            AreEqual(2.0, symbolSystem.atr[0]);
            processClose(1, 3, 1, 3);
            hasOrders(
                buy("BreakOut L", protectiveStop(3.0), 500, oneBar()),
                sell("BreakOut S", protectiveStop(2.0), 500, oneBar())
            );
            IsTrue(orders()[0].canFill(3, false));
            processBar(1, 3, 1, 2);
            processBar(1, 2, 1, 2);
            processClose(1, 2, 1, 2);
            hasOrders(
                buy("BreakOut L", protectiveStop(2.0), 534, oneBar()),
                sell("BreakOut S", protectiveStop(2.0), 534, oneBar())
            );
        }

        [Test]
        public void testMinATR() {
            var atr = symbolSystem.atr[0];
            AreEqual(2.0, atr);
            processClose(1, 3, 1, 3);
            hasOrders(2);
            processBar(1, 3, 1, 3);
            processBar(3, 3, 3, 3);
            processBar(3, 3, 3, 3);
            processBar(3, 3, 3, 3);
            processBar(3, 3, 3, 3);
            processClose(3, 3, 3, 3);
            atr = symbolSystem.atr[0];
            IsTrue(atr > 1.25 && atr < 1.50);
            hasOrders(1);
            processBar(3, 3, 3, 3);
            processBar(3, 3, 3, 3);
            processBar(3, 3, 3, 3);
            processClose(3, 3, 3, 3);
            atr = symbolSystem.atr[0];
            IsTrue(atr < 1.25);
            noOrders();
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
                { "FixEquity", 1.0 },
                { "minATRLong", 1.5 },
                { "minATRShort", 1.25 },
            });
        }

        protected override int leadBars() {
            return 5;
        }
    }
}
