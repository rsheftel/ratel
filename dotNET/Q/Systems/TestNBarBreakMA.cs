using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;
using O = Q.Util.Objects;

namespace Q.Systems {
    [TestFixture]
    public class TestNBarBreakMA : TestNBarBreakMABase {

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
            noOrders();
            processBar(1,2,1,2);
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
        }
    }


    [TestFixture]
    public class TestNBarBreakMAFilters : TestNBarBreakMABase {
        public override void setUp() {
            base.setUp();
            processBar(100, 110, 100, 105);
            processBar(105, 105, 90, 95);
            processBar(95, 103, 95, 98);
            processBar(98, 110, 95, 100);
            processBar(100, 100, 98, 99);
        }

        [Test]
        public void Entries() {
            noOrders();
            //For all these bars the MA fast is < slow, so only sell orders
            processBar(99, 105, 99, 99);
            hasOrders(sell("BreakOut S", protectiveStop(95), 114, oneBar()));
            processBar(99, 120, 95, 120);
            hasOrders(sell("BreakOut S", protectiveStop(95), 82, oneBar()));
            //After this bar the MA flips to fast > slow, so buy orders
            processBar(120, 130, 92, 130);
            hasOrders(buy("BreakOut L", protectiveStop(130), 57, oneBar()));
            //But this bar is not highest high, so nothing filled, more orders
            processBar(95, 100, 91, 100);
            hasOrders(buy("BreakOut L", protectiveStop(130), 45, oneBar()));
            //This bar the MA from prior is good, and the highest high
            fill(0,130);
            processBar(99,135,90,135);
            hasOrders(
                sell("Stop EL", protectiveStop(130 - 2.0 * symbolSystem.atr), 45, oneBar()),
                sell("BreakDn EL", protectiveStop(90), 45, oneBar())
            );
        }
        
        [Test]
        public void ShortEntry() {
            noOrders();
            processBar(99, 105, 99, 99);
            hasOrders(sell("BreakOut S", protectiveStop(95), 114, oneBar()));
            fill(0,95);
            processBar(99,120,90,90);
            hasOrders(
                buy("Stop ES", protectiveStop(95 + 2.0 * symbolSystem.atr), 114, oneBar()),
                buy("BreakDn ES", protectiveStop(120), 114, oneBar())
            );
        }
        //Test the condition where maShort = maFast

        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                { "BreakDays", 4.0 },
                { "ATRLen", 3.0 },
                { "Risk", 0.02 },
                { "nATR", 2.0 },
                { "upATR", 5.0 },
                { "MaxPyramid", 1.0 },
                { "ATRlong", 100.0 },
                { "InitEquity", 100000000.0 },
                { "FixEquity", 1.0 },
                { "MASlow", 10},
                { "MAFast", 5}
            });
        }

    }

    [TestFixture]
    public class TestNBarBreakMASameMA : TestNBarBreakMABase {
        public override void setUp() {
            base.setUp();
            O.zeroTo(arguments().leadBars, i => {
                processBar(100, 150, 90, 100);
                noOrders();
            });
        }

        [Test]
        public void bothOrdersWhenMAequal() {
            noOrders();
            processBar(100,150,90,100);
            hasOrders(
                buy("BreakOut L", protectiveStop(150), 16,oneBar()),
                sell("BreakOut S", protectiveStop(90), 16, oneBar())
            );
        }
    }

    public abstract class TestNBarBreakMABase : OneSymbolSystemTest<NBarBreakMA> {
        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                { "BreakDays", 20.0 },
                { "ATRLen", 10.0 },
                { "Risk", 0.02 },
                { "nATR", 2.0 },
                { "upATR", 1.0 },
                { "MaxPyramid", 2.0 },
                { "ATRlong", 100.0 },
                { "InitEquity", 100000000.0 },
                { "FixEquity", 1.0 },
                { "MASlow", 10},
                { "MAFast", 5}
            });
        }

        protected override int leadBars() {
            return 5;
        }
    }
}
