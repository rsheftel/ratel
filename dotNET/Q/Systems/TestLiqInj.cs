using System;
using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;
using O=Q.Util.Objects;

namespace Q.Systems {


    public class TestLiqInj : OneSymbolSystemTest<LiqInj> {
        DateTime lastTime = O.now();
        Bar lastBar;

        public override void setUp() {           
            base.setUp();
            symbolSystem.zScore.enterTestMode();
            symbolSystem.residual.enterTestMode();
            symbolSystem.tc.enterTestMode();
            symbolSystem.rSquare.enterTestMode();
            symbolSystem.hedge.enterTestMode();
            symbolSystem.scale.enterTestMode();
            lastBar = null;
            O.zeroTo(leadBars(), i => close(1, 0, 0, 0, 0, 0, -999999));
        }

        
        protected override Symbol initializeSymbol() {
            var emptyTestSeries = createEmptyTestSeries();
            insertSymbol("PTZ10.TEST.TY.1C", emptyTestSeries);
            insertSymbol("PTC10.TEST.TY.1C", emptyTestSeries);
            insertSymbol("PTV10.TEST.TY.1C", emptyTestSeries);
            insertSymbol("PTR10.TEST.TY.1C", emptyTestSeries);
            insertSymbol("PTH10.TEST.TY.1C", emptyTestSeries);
            insertSymbol("PTS10.TEST.TY.1C", emptyTestSeries);
            return base.initializeSymbol();
        }

        protected void enterLong() {
            close(2, -1.01, 21, 10, 0.021, 2, 5.1);
            close(-1, -1.01, 21, 10, 0.021, 2, 5.1);
            hasOrders(symbol().buy("Entry Long", market(), 50, oneBar()));
            fill(0, 0);
        }

        protected void enterShort() {
            close(2, 1.01, 21, 10, 0.021, 2, 5.1);
            close(-1, 1.01, 21, 10, 0.021, 2, 5.1);
            hasOrders(symbol().sell("Entry Short", market(), 50, oneBar()));
            fill(0, 0);
        }

        protected void close(double tri, double zScore, double residual, double tc, double rSquare, double hedge, double scale) {
            if(lastBar != null)
                processBar(lastBar);
            lastTime = lastTime.AddDays(1);
            closeWithoutProcessing(tri, zScore, residual, tc, rSquare, hedge, scale);
        }

        protected void closeWithoutProcessing(double tri, double zScore, double residual, double tc, double rSquare, double hedge, double scale) {
            symbolSystem.zScore.add(lastTime, zScore);
            symbolSystem.residual.add(lastTime, residual);
            symbolSystem.tc.add(lastTime, tc);
            symbolSystem.rSquare.add(lastTime, rSquare);
            symbolSystem.hedge.add(lastTime, hedge);
            if(scale != -999999)
                symbolSystem.scale.add(lastTime, scale);
            lastBar = new Bar(tri, tri, tri, tri, lastTime);
            processClose(lastBar);
        }

        protected void tick(double tri, double zScore, double residual, double rSquare, double hedge) {
            lastTime = lastTime.AddSeconds(1);
            symbolSystem.zScore.addTick(lastTime, zScore);
            symbolSystem.residual.addTick(lastTime, residual);
            symbolSystem.rSquare.addTick(lastTime, rSquare);
            symbolSystem.hedge.addTick(lastTime, hedge);
            processTick(tri, lastTime);
        }

        protected override int leadBars() {
            return 3;
        }

        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                {"ATRLength", 2},
                {"acfHalfLife", 10},
                {"acfLag", 3},
                {"acfTrigger", 10},
                {"hedgeMax", 5},
                {"hedgeMin", 2},
                {"hedgeSwitch", 0},
                {"maxBarInTrade", 3},
                {"nATR", 1},
                {"pScoreMin", 2},
                {"rSquareMin", 0.02},
                {"scaleMin", 5},
                {"startSize", 100000},
                {"stopMultiple", 2},
                {"zScoreMin", 1.0}
            });
        }
    }
    [TestFixture]
    public class TestLiqInjVersion10 : TestLiqInj {
        protected override Symbol initializeSymbol() {
            return new Symbol("PTT10.ESIG", 1000);
        }

        [Test]
        public void testScale() {
            close(0, 0, 0, 0, 0, 0, -999999); // no scale
            IsFalse(symbolSystem.isValidScale);
            close(0, 0, 0, 0, 0, 0, 5);
            IsFalse(symbolSystem.isValidScale);
            close(0, 0, 0, 0, 0, 0, 5.1);
            IsTrue(symbolSystem.isValidScale);
            close(0, 0, 0, 0, 0, 0, 16);
            IsFalse(symbolSystem.isValidScale);
            close(0, 0, 0, 0, 0, 0, 15.9);
            IsTrue(symbolSystem.isValidScale);
        }

        [Test]
        public void testLongShortSignal() {
            close(0, -1, 21, 10, 0, 0, 0.0);
            IsFalse(symbolSystem.isLongSignal);
            IsFalse(symbolSystem.isShortSignal);
            IsFalse(symbolSystem.isTriggered);
            close(0, -1.01, 21, 10, 0, 0, 0.0);
            IsTrue(symbolSystem.isLongSignal);
            IsFalse(symbolSystem.isShortSignal);
            IsTrue(symbolSystem.isTriggered);
            close(0, 1, 21, 10, 0, 0, 0.0);
            IsFalse(symbolSystem.isLongSignal);
            IsFalse(symbolSystem.isShortSignal);
            IsFalse(symbolSystem.isTriggered);
            close(0, 1.01, 21, 10, 0, 0, 0.0);
            IsFalse(symbolSystem.isLongSignal);
            IsTrue(symbolSystem.isShortSignal);
            IsTrue(symbolSystem.isTriggered);
            close(0, -1.01, 20, 10, 0, 0, 0.0);
            IsTrue(symbolSystem.isLongSignal);
            IsFalse(symbolSystem.isShortSignal);
            IsFalse(symbolSystem.isTriggered);
            close(0, 1.01, 20, 10, 0, 0, 0.0);
            IsFalse(symbolSystem.isLongSignal);
            IsTrue(symbolSystem.isShortSignal);
            IsFalse(symbolSystem.isTriggered);
        }

        [Test]
        public void testIsValidFit() {
            close(0, 0, 0, 0, 0.02, 2, 0.0);
            IsFalse(symbolSystem.isValidFit);
            close(0, 0, 0, 0, 0.021, 2, 0.0);
            IsTrue(symbolSystem.isValidFit);
            close(0, 0, 0, 0, 0.021, 5, 0.0);
            IsTrue(symbolSystem.isValidFit);
            close(0, 0, 0, 0, 0.021, 1.9, 0.0);
            IsFalse(symbolSystem.isValidFit);
            close(0, 0, 0, 0, 0.021, 5.1, 0.0);
            IsFalse(symbolSystem.isValidFit);
            close(0, 0, 0, 0, 0.021, -2, 0.0);
            IsTrue(symbolSystem.isValidFit);
            close(0, 0, 0, 0, 0.021, -5, 0.0);
            IsTrue(symbolSystem.isValidFit);
            close(0, 0, 0, 0, 0.021, -1.9, 0.0);
            IsFalse(symbolSystem.isValidFit);
            close(0, 0, 0, 0, 0.021, -5.1, 0.0);
            IsFalse(symbolSystem.isValidFit);
        }

        [Test]
        public void testIsValidAcf() {
            close(0, 1, 0, 0, 0, 0, 0.0);
            close(0, 2, 0, 0, 0, 0, 0.0);
            close(0, 3, 0, 0, 0, 0, 0.0);
            close(0, 4, 0, 0, 0, 0, 0.0);
            IsFalse(symbolSystem.isValidAcf);
            close(0, -3, 0, 0, 0, 0, 0.0);
            IsTrue(symbolSystem.isValidAcf);
        }

        [Test]
        public void testIsConfirmed() {
            close(0, 1, 0, 0, 0, 0, 0.0);
            close(0, 2, 0, 0, 0, 0, 0.0);
            close(0, 3, 0, 0, 0, 0, 0.0);
            close(2, 4, 21, 10, 0.021, 2, 5.1);
            IsFalse(symbolSystem.isConfirmed);
            IsFalse(symbolSystem.isValidAcf);
            close(-1, -3, 21, 10, 0.021, 2, 5.1);
            IsTrue(symbolSystem.isConfirmed);
            close(-2, -2, 21, 10, 0.02, 2, 5.1);
            IsFalse(symbolSystem.isConfirmed);
            IsTrue(symbolSystem.isValidAcf);
            IsFalse(symbolSystem.isValidFit);
            close(-3, 0, 21, 10, 0.021, 2, 5.1);
            IsFalse(symbolSystem.isConfirmed);
            IsTrue(symbolSystem.isValidAcf);
            IsFalse(symbolSystem.isTriggered);
            close(-10000, -1.01, 21, 10, 0.021, 2, 5.1);
            IsFalse(symbolSystem.isConfirmed);
            IsTrue(symbolSystem.isValidAcf);
            AreEqual(0, symbolSystem.tradeSize());
        }

        [Test]
        public void testIsAccepted() {
            close(0, 1, 0, 0, 0, 0, 0.0);
            close(0, 2, 0, 0, 0, 0, 0.0);
            close(0, 3, 0, 0, 0, 0, 0.0);
            close(2, 4, 21, 10, 0.021, 2, 5.1);
            IsFalse(symbolSystem.isAccepted);
            close(-1, -1.01, 21, 10, 0.021, 2, 5.1);
            IsTrue(symbolSystem.isAccepted);
            hasOrders(symbol().buy("Entry Long", market(),40,oneBar()));
            fill(0, 0);
            close(-2, -1.1, 21, 10, 0.021, 2, 5.1);
            IsFalse(symbolSystem.isAccepted);
            close(-3, 1.01, 21, 10, 0.021, 2, 5.1);
            IsTrue(symbolSystem.isAccepted);
            hasOrders(
                position().exit("Exit Long On Signal", market(), oneBar()),
                symbol().sell("Entry Short", market(), 100, oneBar())
            );
            fill(0, 0);
            fill(0, 0);
            close(-4, 1.01, 21, 10, 0.021, 2, 5.1);
            IsFalse(symbolSystem.isAccepted);
            close(-3, -1.01, 21, 10, 0.021, 2, 5.1);
            IsTrue(symbolSystem.isAccepted);
        }

        [Test]
        public void testInitialEntry() {
            close(2, -1.01, 21, 10, 0.021, 2, 5.1);
            close(-1, -1.01, 21, 10, 0.021, 2, 5.1);
            hasOrders(symbol().buy("Entry Long", market(), 50, oneBar()));
            close(-2, 1.01, 21, 10, 0.021, 2, 5.1);
            hasOrders(symbol().sell("Entry Short", market(), 50, oneBar()));
            close(10, -1.01, 21, 10, 0.021, 2, 5.1);
            noOrders();
        }

        [Test]
        public void testExitInvalidFit() {
            enterLong();
            close(0, -1.01, 21, 10, 0.021, 1, 5.1);
            hasOrders(position().exit("Invalid Hedge", market(), oneBar()));
        }

        [Test]
        public void testExitBarsHeld() {
            enterLong();
            close(-2, 3, 21, 10, 0.021, 2, 5.1);
            close(-1, -1.01, 21, 10, 0.021, 2, 5.1);
            close(-2, -1.01, 21, 10, 0.021, 2, 5.1);
            IsFalse(symbolSystem.isConfirmed);
            hasOrders(position().exit("Bar Limit", market(), oneBar()));
            close(-1, -1.01, 21, 10, 0.021, 2, 5.1);
            IsTrue(symbolSystem.isConfirmed);
            noOrders();// bar limit ignored when system is in confirmed state
            hasPosition(50); 
            IsTrue(symbolSystem.isValidAcf);
            IsTrue(symbolSystem.isTriggered);
            IsTrue(symbolSystem.isValidFit);
            AreEqual(100, symbolSystem.tradeSize());
        }

        [Test]
        public void testExitOnTarget() {
            enterLong();
            close(1051, -1.01, 20, 10, 0.021, 2, 5.1);
            hasOrders(position().exit("Exit Long On Target", market(), oneBar()));
            IsTrue(symbolSystem.isValidAcf);
            IsFalse(symbolSystem.isTriggered);
            close(1051, -1.01, 21, 10, 0.021, 2, 5.1);
            close(1051, -1.01, 21, 10, 0.021, 2, 5.1);
            close(1051, -1.01, 21, 10, 0.021, 2, 5.1);
            hasOrders(position().exit("Exit Long On Target", market(), oneBar()), Order.ANY);
            IsFalse(symbolSystem.isValidAcf);         
            IsTrue(symbolSystem.isTriggered);
        }

        [Test]
        public void testExitIntradayOnTargetButNotOnClose() {
            emailer.allowMessages();
            enterLong();
            tick(2000,0.5, 21, 0.021, 2);
            hasOrders(position().exit("Exit Long On Target", market(), oneBar()));
            fill(0, 1);
            tick(0, -0.5, 21, 0.021, 2);
            noOrders();
            closeWithoutProcessing(0,-0.5, 21, 10, 0.021, 2, 5.1);
            hasOrders(symbol().buy("Sync Long To Close", market(), 50, oneBar()));
        }

        [Test]
        public void testExitShortIntradayOnStopButNotOnClose() {
            emailer.allowMessages();
            enterShort();
            
            tick(1000,0.5, 21, 0.021, 2);
            hasOrders(position().exit("Stop Loss", market(), oneBar()));
            fill(0, 1);
            noPositions();
            tick(0, -0.5, 21, 0.021, 2);
            noOrders();
            closeWithoutProcessing(0,-0.5, 21, 10, 0.021, 2, 5.1);
            hasOrders(symbol().sell("Sync Short To Close", market(), 50, oneBar()));
        }

        [Test]
        public void testExitLongIntradayOnStopButNotOnClose() {
            emailer.allowMessages();
            enterLong();
            
            tick(-1000,0.5, 21, 0.021, 2);
            hasOrders(position().exit("Stop Loss", market(), oneBar()));
            fill(0, 1);
            tick(0, -0.5, 21, 0.021, 2);
            noOrders();
            closeWithoutProcessing(0,-0.5, 21, 10, 0.021, 2, 5.1);
            hasOrders(symbol().buy("Sync Long To Close", market(), 50, oneBar()));
        }

        [Test]
        public void testReversePositionIntradayButComeBackInOriginalTradeOnClose()
        {
            emailer.allowMessages();
            enterLong();
            tick(3, 1.01, 21, 0.021, 2);
            hasOrders(
                position().exit("Exit Long On Signal", market(), oneBar()),
                symbol().sell("Entry Short", market(), 100, oneBar())
            );
            fill(0, 0);
            fill(0, 0);
            tick(3,-1, 21, 0.021, 2);
            noOrders(); 
            closeWithoutProcessing(0, -0.5, 21, 10, 0.021, 2, 5.1);
            hasOrders(
                position().exit("Sync Flatten Position", market(), oneBar()),
                symbol().buy("Sync Long To Close", market(), 50, oneBar())
            );
        }

        [Test]
        public void testSyncUpToClose() {
            emailer.allowMessages();
            enterLong();
            tick(3, 1.01, 21, 0.021, 2);
            hasOrders(
                position().exit("Exit Long On Signal", market(), oneBar()),
                symbol().sell("Entry Short", market(), 100, oneBar())
            );
            fill(0, 0);
            fill(0, 0);
            tick(3, -1.1, 21, 0.021, 2);
            hasOrders(
                position().exit("Exit Short On Signal", market(), oneBar()),
                symbol().buy("Entry Long", market(), 100, oneBar())
            );
            fill(0, 0);
            fill(0, 0);
            closeWithoutProcessing(0, -0.5, 21, 10, 0.021, 2, 5.1);
            hasOrders(
                symbol().sell("Sync Down To Close", market(), 50, oneBar())
            );
        }

        [Test]
        public void testDoesNotCheckBarsHeldInLiveWithNoPosition() {
            close(2, 0, 21, 10, 0.021, 2, 5.1);
            close(-1, 0, 21, 10, 0.021, 2, 5.1);
            noOrders();
            tick(0, 0, 0, 0, 0);
        }
        
        [Test]
        public void testDoesNotCheckBarsHeldOnTick() {
            enterLong();
            close(2, 0, 21, 10, 0.021, 2, 5.1);
            close(-1, 0, 21, 10, 0.021, 2, 5.1);          
            noOrders();
            tick(-1, 0, 20, 0.021, 2);
            noOrders();
        }
        
        [Test]
        public void testChecksBarsHeldOnClose() {
            enterLong();
            close(2, 0, 21, 10, 0.021, 2, 5.1);
            noOrders();
            close(-1, 0, 21, 10, 0.021, 2, 5.1);
            noOrders();
            close(-1, 0, 21, 10, 0.021, 2, 5.1);
            hasOrders(position().exit("Bar Limit", market(), oneBar()));
        }

                        
        [Test] 
        public void testRunsLimitedOnCloseInLiveMode() {
            // set the time to early in the day, so that we haven't passed the close
            O.freezeNow(date("2008/10/08 11:00:00"));
            enterLong();
            IsTrue(symbolSystem.runOnClose());
            tick(-17, -0.5, 21, 0.021, 2);
            IsTrue(symbolSystem.runOnClose());
            IsNotNull(symbolSystem.onCloseTimer);
        }

        [Test]
        public void testRunsLimitedOnCloseDoesNotRunPassedClose() {
            // set the time after the close
            O.freezeNow(date("2008/10/08 16:15:00"));
            tick(-17, -0.5, 21, 0.021, 2);
            IsNull(symbolSystem.onCloseTimer);
        }

        [Test]
        public void testExitOnOppositeSignal() {
            enterLong();
            close(-2, 3, 21, 10, 0.021, 2, 5.1);
            hasOrders(
                position().exit("Exit Long On Signal", market(), oneBar()),
                symbol().sell("Entry Short", market(), 50, oneBar())
            );
            fill(0, 0);
            fill(0, 0);
            close(-3, -1.01, 21, 10, 0.021, 2, 5.1);
            hasOrders(
                position().exit("Exit Short On Signal", market(), oneBar()),
                symbol().buy("Entry Long", market(), 100, oneBar())
            );
        }

        [Test]
        public void testStopLoss() {
            enterLong();
            close(-2101, -0.5, 21, 10, 0.021, 2, 5.1);
            hasOrders(position().exit("Stop Loss", market(), oneBar()));
            fill(0, 1);
            IsFalse(symbolSystem.isActive());
            close(0, 0.5, 21, 10, 0.021, 2, 5.1);
            IsFalse(symbolSystem.isActive());
            close(0, -0.51, 21, 10, 0.021, 2, 5.1);
            IsTrue(symbolSystem.isActive());
        }  

        [Test]
        public void testStopLossInLiveButNotOnClose() {
            emailer.allowMessages();
            enterLong();
            tick(-2101, -1.5, 21, 0.021, 2);
            hasOrders(position().exit("Stop Loss", market(), oneBar()));
            fill(0, 1);
            IsTrue(symbolSystem.isActive());
            tick(100, 1.01, 21, 0.021, 2);
            tick(100, -1.01, 21, 0.021, 2);
            IsTrue(symbolSystem.isActive());
            closeWithoutProcessing(-1, -1.01, 21, 10, 0.021, 2, 5.1);
            hasOrders(symbol().buy("Sync Long To Close", market(), 50, oneBar()));
        }

        [Test]
        public void testIsValidHedge() {
            IsFalse(LiqInj.isValidHedge(0.99, 0, 1, 2));
            IsTrue(LiqInj.isValidHedge(1, 0, 1, 2));
            IsTrue(LiqInj.isValidHedge(2, 0, 1, 2));
            IsFalse(LiqInj.isValidHedge(2.01, 0, 1, 2));
            IsFalse(LiqInj.isValidHedge(-0.99, 0, 1, 2));
            IsTrue(LiqInj.isValidHedge(-1, 0, 1, 2));
            IsTrue(LiqInj.isValidHedge(-2, 0, 1, 2));
            IsFalse(LiqInj.isValidHedge(-2.01, 0, 1, 2));

            IsFalse(LiqInj.isValidHedge(0.99, 1, 1, 2));
            IsTrue(LiqInj.isValidHedge(1, 1, 1, 2));
            IsTrue(LiqInj.isValidHedge(2, 1, 1, 2));
            IsFalse(LiqInj.isValidHedge(2.01, 1, 1, 2));
            IsFalse(LiqInj.isValidHedge(-0.99, 1, 1, 2));
            IsFalse(LiqInj.isValidHedge(-1, 1, 1, 2));
            IsFalse(LiqInj.isValidHedge(-2, 1, 1, 2));
            IsFalse(LiqInj.isValidHedge(-2.01, 1, 1, 2));

            IsFalse(LiqInj.isValidHedge(0.99, -1, 1, 2));
            IsFalse(LiqInj.isValidHedge(1, -1, 1, 2));
            IsFalse(LiqInj.isValidHedge(2, -1, 1, 2));
            IsFalse(LiqInj.isValidHedge(2.01, -1, 1, 2));
            IsFalse(LiqInj.isValidHedge(-0.99, -1, 1, 2));
            IsTrue(LiqInj.isValidHedge(-1, -1, 1, 2));
            IsTrue(LiqInj.isValidHedge(-2, -1, 1, 2));
            IsFalse(LiqInj.isValidHedge(-2.01, -1, 1, 2));
        }

        [Test]
        public void testSize() {
            close(2, 0, 0, 0, 0, 0, 0);
            AreEqual(200, symbolSystem.tradeSize());
            close(3, 0, 0, 0, 0, 0, 0);
            AreEqual(100, symbolSystem.tradeSize());
            close(2, 0, 0, 0, 0, 0, 0);
            close(-1, 0, 0, 0, 0, 0, 0);
            AreEqual(50, symbolSystem.tradeSize());
        }

        [Test]
        public void testVersion() {
            AreEqual(symbolSystem.version,"10");
        }
    }

    [TestFixture]
    public class TestLiqInjVersion11 : TestLiqInj {
        protected override Symbol initializeSymbol() {
            return new Symbol("PTT11.EWCEWA", 1000);
        }

        [Test]
        public void testVersion() {
            AreEqual(symbolSystem.version,"11");
        }
    }
}


