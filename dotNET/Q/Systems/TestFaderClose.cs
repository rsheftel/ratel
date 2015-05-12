using System;
using System.Collections.Generic;
using NUnit.Framework;
using Q.Recon;
using Q.Trading;
using Q.Util;
using O=Q.Util.Objects;

namespace Q.Systems {
    [TestFixture] public class TestFaderCloseMinTradeSize : TestFaderCloseBase {
        [Test] public void TradeSizeLessThanOne() {
            close(40);
            noOrders();
            close(54);
            hasOrders(sell("Enter Short", limit(54), 1, oneBar()));
        }

        public override void setUp() {
            base.setUp();
            lastBar = null;
            close(38.5);
            close(37.2);
            close(37.7);
            close(38);
            close(38.5);
            close(38.4);
            close(38.25);
            close(39);
            close(40);
            close(39);
            close(38);
            close(37.5);
            close(40.3);
        }

        protected override int leadBars() {
            return 8;
        }

        protected override Parameters parameters() {
            return base.parameters().overwrite(
                new Dictionary<string, double> {
                    {"maType", 2},
                    {"maLength", 8},
                    {"stDevLength", 10},
                    {"regressionProjectionBars", 5},
                    {"regressionBars", 8},
                    {"ZEntry", 2},
                    {"minPnLMultTC", 5},
                    {"stopMultiple", 3},
                    {"RiskDollars", 1},
                    //This very small RiskDollars should lead to zero size if not bounded
                    {"ZExit", 1},
                    {"rSqrScale", 0},
                    {"LeadBars", leadBars()}
                });
        }
    }

    public class TestFaderCloseConfirm : TestFaderCloseBase {
        [Test] public void confirm() {
            O.zeroTo(3, i => close(40));
            noOrders();

            //This close of 35 would be an order, but does not confirm
            close(35);
            noOrders();
            AreEqual(Direction.LONG, symbolSystem.tradeSetup());

            //Now these closes do pass confirm
            close(40);
            close(20);
            hasOrders(buy("Enter Long", limit(20), 2078, oneBar()));

            //Now try with shorts
            O.zeroTo(7, i => close(20));
            O.zeroTo(2, i => close(22));
            noOrders();
            AreEqual(Direction.SHORT, symbolSystem.tradeSetup());

            //How these pass the confirm
            close(38);
            hasOrders(sell("Enter Short", limit(38), 2123, oneBar()));
        }

        public override void setUp() {
            base.setUp();
            lastBar = null;
            close(38.5);
            close(37.2);
            close(37.7);
            close(38);
            close(38.5);
            close(38.4);
            close(38.25);
            close(39);
            close(40);
            close(39);
            close(38);
            close(37.5);
            close(40.3);
        }

        protected override int leadBars() {
            return 10;
        }

        protected override Parameters parameters() {
            return base.parameters().overwrite(
                new Dictionary<string, double> {
                    {"maType", 2},
                    {"maLength", 8},
                    {"stDevLength", 10},
                    {"regressionProjectionBars", 5},
                    {"regressionBars", 8},
                    {"ZEntry", 2},
                    {"minPnLMultTC", 1000},
                    {"stopMultiple", 3},
                    {"RiskDollars", 100000000},
                    {"ZExit", 1},
                    {"rSqrScale", 0},
                    {"LeadBars", leadBars()}
                });
        }
    }

    public abstract class TestFaderCloseBase : OneSymbolSystemTest<FaderClose> {
        DateTime lastTime = Objects.now();
        protected Bar lastBar;

        protected void close(double tri) {
            if (lastBar != null) processBar(lastBar);
            lastTime = lastTime.AddDays(1);
            lastBar = new Bar(tri, tri + 1, tri - 1, tri, lastTime);
            processClose(lastBar);
        }
    }

    [TestFixture] public class TestFaderClose : TestFaderCloseBase {
        [Test] public void testProjection() {
            close(10);
            AlmostEqual(31.2031, symbolSystem.maRaw, 0.001);
            AlmostEqual(9.1391, symbolSystem.priceStDev, 0.0001);
            AlmostEqual(33.0048, symbolSystem.levelProjection, 0.0001);
        }

        [Test] public void tradeSetup() {
            noOrders();
            close(40);
            noOrders();
            close(54);
            AreEqual(Direction.SHORT, symbolSystem.tradeSetup());
            close(50);
            noOrders();
            close(40);
            noOrders();
            close(30);
            AreEqual(Direction.LONG, symbolSystem.tradeSetup());
        }

        [Test] public void zScoreEnterAndExit() {
            //Short Enter and exit
            close(40);
            noOrders();
            close(54);
            hasOrders(sell("Enter Short", limit(54), 2871, oneBar()));
            fill(0, 10);
            close(53);
            noOrders();
            close(50);
            hasOrders(position().exitShort("SX Objective", limit(50), oneBar()));
            fill(0, 20);

            //Long Enter and exit
            close(39);
            noOrders();
            close(38);
            hasOrders(buy("Enter Long", limit(38), 2263, oneBar()));
            fill(0, 20);
            close(40);
            noOrders();
            close(55);
            hasOrders(position().exitLong("LX Objective", limit(55), oneBar()));
        }

        [Test] public void stopLossShort() {
            close(40);
            noOrders();
            close(43);
            hasOrders(sell("Enter Short", limit(43), 9675, oneBar()));
            fill(0, 43);
            close(53.5);
            hasOrders(position().exitShort("SX Stop", limit(53.5), oneBar()));
        }

        [Test] public void stopLossLong() {
            close(40);
            noOrders();
            close(36);
            hasOrders(buy("Enter Long", limit(36), 10784, oneBar()));
            fill(0, 36);
            close(26.5);
            hasOrders(position().exitLong("LX Stop", limit(26.5), oneBar()));
        }

        [Test] public void enterAndExitSameBar() {
            //Enter a long
            close(40);
            noOrders();
            close(36);
            hasOrders(buy("Enter Long", limit(36), 10784, oneBar()));
            fill(0, 36);
            //This bar is both an objective exit and a short entry, but will only exit
            close(55);
            hasOrders(position().exitLong("LX Objective", limit(55), oneBar()));
            fill(0, 55);
            //One more bar of the same is a Short entry
            close(70);
            hasOrders(sell("Enter Short", limit(70), 1516, oneBar()));
        }

        [Test] public void fullRun() {
            O.freezeNow("2009/01/15"); // so that params don't change out from under us
            var args = new SystemArguments(
                symbol(),
                new Parameters {
                    {"systemId", 63234},
                    {"RunMode", (double) RunMode.LIVE}
                });
            var simulator = new Simulator.Simulator(args, OrderTable.prefix);
            simulator.processBars(300);
            // using slippage for RE.TEST.TY.1C: 0.015625
            // Full run AlmostEqual(-1975093.75, simulator.pnl(), 0.01);
            AlmostEqual(-184296.88, simulator.pnl(), 0.01);
        }

        public override void setUp() {
            base.setUp();
            lastBar = null;
            close(38.5);
            close(37.2);
            close(37.7);
            close(38);
            close(38.5);
            close(38.4);
            close(38.25);
            close(39);
            close(40);
            close(39);
            close(38);
            close(37.5);
            close(40.3);
        }

        protected override int leadBars() {
            return 8;
        }

        protected override Parameters parameters() {
            return base.parameters().overwrite(
                new Dictionary<string, double> {
                    {"maType", 2},
                    {"maLength", 8},
                    {"stDevLength", 10},
                    {"regressionProjectionBars", 5},
                    {"regressionBars", 8},
                    {"ZEntry", 2},
                    {"minPnLMultTC", 5},
                    {"stopMultiple", 3},
                    {"RiskDollars", 100000000},
                    {"ZExit", 1},
                    {"rSqrScale", 0},
                    {"LeadBars", leadBars()}
                });
        }
    }
}