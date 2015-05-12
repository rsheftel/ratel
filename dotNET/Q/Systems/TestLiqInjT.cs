using System;
using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;
using O=Q.Util.Objects;

namespace Q.Systems {


    public class TestLiqInjT : OneSymbolSystemTest<LiqInjT> {
        DateTime lastTime = O.now();
        Bar lastBar;

        public override void setUp() {    
            base.setUp();      
            symbolSystem.zScore.enterTestMode();
            symbolSystem.residual.enterTestMode();
            symbolSystem.betaShort.enterTestMode();
            symbolSystem.tc.enterTestMode();            
            symbolSystem.beta.enterTestMode();            
            lastBar = null;
            O.zeroTo(leadBars(), i => close(1, 0, 0, 0, 0,0));
        }
        
        protected override Symbol initializeSymbol() {
            var emptyTestSeries = createEmptyTestSeries();
            insertSymbol("PTZ10.TEST.TY.1C", emptyTestSeries);
            insertSymbol("PTC10.TEST.TY.1C", emptyTestSeries);
            insertSymbol("PTV10.TEST.TY.1C", emptyTestSeries);            
            insertSymbol("PTO10.TEST.TY.1C", emptyTestSeries);
            insertSymbol("PTB10.TEST.TY.1C", emptyTestSeries);
            return base.initializeSymbol();
        }



        protected void enterLong() {
            close(2, -1.01, 21, 10,2,2);
            close(-1, -1.01, 21, 10,2,2);
            hasOrders(symbol().buy("Entry Long", market(),10, oneBar()));
            fill(0, 0);
        }

        protected void close(double tri, double zScore, double residual, double tc,double beta,double betaShort) {
            if(lastBar != null)
                processBar(lastBar);
            lastTime = lastTime.AddDays(1);
            closeWithoutProcessing(tri, zScore, residual, tc, beta,betaShort);
        }

        protected void closeWithoutProcessing(double tri, double zScore, double residual, double tc,double beta,double betaShort) {
            symbolSystem.zScore.add(lastTime, zScore);
            symbolSystem.residual.add(lastTime, residual);
            symbolSystem.tc.add(lastTime, tc);            
            symbolSystem.beta.add(lastTime, beta);            
            symbolSystem.betaShort.add(lastTime, betaShort);            
            lastBar = new Bar(tri, tri, tri, tri, lastTime);
            processClose(lastBar);
        }        

        protected override int leadBars() {
            return 3;
        }

        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {               
                {"acfHalfLife", 10},
                {"acfLag", 3},
                {"acfTrigger", 10},
                {"betaMax", 5},
                {"betaMin", 2},
                {"maxBarInTrade", 3},
                {"pScoreMin", 2},
                {"risk", 100000},
                {"stopMultiple", 2},
                {"zScoreMin", 1.0}
            });
        }
    }
    [TestFixture]
    public class TestLiqInjTCloseSystem : TestLiqInjT {
        protected override Symbol initializeSymbol() {
            return new Symbol("LIT10.XLFXLB", 1000);
        }

        [Test]
        public void testLongShortSignal() {
            close(0, -1, 21, 10, 0,0);
            IsFalse(symbolSystem.isLongSignal);
            IsFalse(symbolSystem.isShortSignal);
            IsFalse(symbolSystem.isTriggered);
            close(0, -1.01, 21, 10, 0,0);
            IsTrue(symbolSystem.isLongSignal);
            IsFalse(symbolSystem.isShortSignal);
            IsTrue(symbolSystem.isTriggered);
            close(0, 1, 21, 10, 0,0);
            IsFalse(symbolSystem.isLongSignal);
            IsFalse(symbolSystem.isShortSignal);
            IsFalse(symbolSystem.isTriggered);
            close(0, 1.01, 21, 10, 0,0);
            IsFalse(symbolSystem.isLongSignal);
            IsTrue(symbolSystem.isShortSignal);
            IsTrue(symbolSystem.isTriggered);
            close(0, -1.01, 20, 10, 0,0);
            IsTrue(symbolSystem.isLongSignal);
            IsFalse(symbolSystem.isShortSignal);
            IsFalse(symbolSystem.isTriggered);
            close(0, 1.01, 20, 10, 0,0);
            IsFalse(symbolSystem.isLongSignal);
            IsTrue(symbolSystem.isShortSignal);
            IsFalse(symbolSystem.isTriggered);
        }

        [Test]
        public void testIsValidFit() {            
            close(0, 0, 0, 0,  2,2);
            IsTrue(symbolSystem.isValidFit);
            close(0, 0, 0, 0,  5,5);
            IsTrue(symbolSystem.isValidFit);
            close(0, 0, 0, 0, 1.9,1.9);
            IsFalse(symbolSystem.isValidFit);
            close(0, 0, 0, 0, 5.1,5.1);
            IsFalse(symbolSystem.isValidFit);
            close(0, 0, 0, 0, -2,-2);
            IsFalse(symbolSystem.isValidFit);
            close(0, 0, 0, 0,3,3);
            IsTrue(symbolSystem.isValidFit);
            close(0, 0, 0, 0, 3.5,3.5);
            IsTrue(symbolSystem.isValidFit);
            close(0, 0, 0, 0, 4.2,4.2);
            IsTrue(symbolSystem.isValidFit);
            close(0, 0, 0, 0,5.1,5.1);
            IsFalse(symbolSystem.isValidFit);
            close(0, 0, 0, 0,5,5);
            IsTrue(symbolSystem.isValidFit);            
        }

        [Test]
        public void testIsValidAcf() {
            close(0, 1, 0, 0, 0,0);
            close(0, 2, 0, 0, 0,0);
            close(0, 3, 0, 0, 0,0);
            close(0, 4, 0, 0, 0,0);
            IsFalse(symbolSystem.isValidAcf);
            close(0, -3, 0, 0,0,0);
            IsTrue(symbolSystem.isValidAcf);
        }

        [Test]
        public void testIsConfirmed() {
            close(0, 1, 0, 0, 0,0);
            close(0, 2, 0, 0, 0,0);
            close(0, 3, 0, 0, 0,0);
            close(2, 4, 21, 10,2,2);
            IsFalse(symbolSystem.isConfirmed);
            IsFalse(symbolSystem.isValidAcf);
            close(-1, -3, 21, 10,2,2);
            IsTrue(symbolSystem.isConfirmed);
            close(-2, -2, 21, 10, 5.1,5.1);
            IsFalse(symbolSystem.isConfirmed);
            IsTrue(symbolSystem.isValidAcf);
            IsFalse(symbolSystem.isValidFit);
            close(-3, 0, 21, 10, 2,2);
            IsFalse(symbolSystem.isConfirmed);
            IsTrue(symbolSystem.isValidAcf);
            IsFalse(symbolSystem.isTriggered);         
        }

        [Test]
        public void testIsAccepted() {
            close(0, 1, 0, 0, 0,0);
            close(0, 2, 0, 0, 0,0);
            close(0, 3, 0, 0, 0,0);
            close(2, 4, 21, 10,2,2);
            IsFalse(symbolSystem.isAccepted);
            close(-1, -1.01, 21, 10, 2,2);
            IsTrue(symbolSystem.isAccepted);
            hasOrders(symbol().buy("Entry Long", market(),10,oneBar()));
            fill(0, 0);
            close(-2, -1.1, 21, 10, 2,2);
            IsFalse(symbolSystem.isAccepted);
            close(-3, 1.01, 21, 10, 2,2);
            IsTrue(symbolSystem.isAccepted);
            hasOrders(
                position().exit("Exit Long On Signal", market(), oneBar()),
                symbol().sell("Entry Short", market(), 10, oneBar())
            );
            fill(0, 0);
            fill(0, 0);
            close(-4, 1.01, 21, 10, 2,2);
            IsFalse(symbolSystem.isAccepted);
            close(-3, -1.01, 21, 10, 2,2);
            IsTrue(symbolSystem.isAccepted);
        }

        [Test]
        public void testInitialEntry() {
            close(2, -1.01, 21, 10, 2,2);
            close(-1, -1.01, 21, 10, 2,2);
            hasOrders(symbol().buy("Entry Long", market(), 10, oneBar()));
            close(-2, 1.01, 21, 10, 2,2);
            hasOrders(symbol().sell("Entry Short", market(), 10, oneBar()));
            close(10, -1.01, 21, 10, 2,2);
            noOrders();
        }

        [Test]
        public void testExitInvalidFit() {
            enterLong();
            close(0, -1.01, 21, 10,1,1);
            hasOrders(position().exit("Invalid Hedge", market(), oneBar()));
        }

        [Test]
        public void testExitInvalidFitOnBadLongHedge() {
            enterLong();
            close(0, -1.01, 21, 10,5.1,5.1);
            hasOrders(position().exit("Invalid Hedge", market(), oneBar()));
        }

        [Test]
        public void testExitBarsHeld() {
            enterLong();
            close(-2, 3, 21, 10, 2,2);
            close(-1, -1.01, 21, 10, 2,2);
            close(-2, -1.01, 21, 10, 2,2);
            IsFalse(symbolSystem.isConfirmed);
            hasOrders(position().exit("Bar Limit", market(), oneBar()));
            close(-1, -1.01, 21, 10, 2,2);
            IsTrue(symbolSystem.isConfirmed);
            hasOrders(position().exit("Bar Limit", market(), oneBar()));            
            AreEqual(10, symbolSystem.tradeSize());
        }

        [Test]
        public void testExitOnTarget() {
            enterLong();
            close(1051, -1.01, 20, 10, 2,2);
            hasOrders(position().exit("Exit Long On Target", market(), oneBar()));
            IsTrue(symbolSystem.isValidAcf);
            IsFalse(symbolSystem.isTriggered);
            close(1051, -1.01, 21, 10, 2,2);
            close(1051, -1.01, 21, 10, 2,2);
            close(1051, -1.01, 21, 10, 2,2);
            hasOrders(position().exit("Exit Long On Target", market(), oneBar()), Order.ANY);
            IsFalse(symbolSystem.isValidAcf);         
            IsTrue(symbolSystem.isTriggered);
        }       
      
        [Test]
        public void testChecksBarsHeldOnClose() {
            enterLong();
            close(2, 0, 21, 10, 2,2);
            noOrders();
            close(-1, 0, 21, 10,2,2);
            noOrders();
            close(-1, 0, 21, 10,2,2);
            hasOrders(position().exit("Bar Limit", market(), oneBar()));
        }
     
        [Test]
        public void testExitOnOppositeSignal() {
            enterLong();
            close(-2, 3, 21, 10,  2,2);
            hasOrders(
                position().exit("Exit Long On Signal", market(), oneBar()),
                symbol().sell("Entry Short", market(), 10, oneBar())
            );
            fill(0, 0);
            fill(0, 0);
            close(-3, -1.01, 21, 10,  2,2);
            hasOrders(
                position().exit("Exit Short On Signal", market(), oneBar()),
                symbol().buy("Entry Long", market(), 10, oneBar())
            );
        }

        [Test]
        public void testStopLoss() {
            enterLong();
            close(-9, -0.5, 21, 10,  2,2);
            noOrders();
            close(-11, -0.5, 21, 10,  2,2);
            hasOrders(position().exit("Stop Loss", market(), oneBar()));
            fill(0, 1);
            IsFalse(symbolSystem.isActive());
            close(0, 0.5, 21, 10,  2,2);
            IsFalse(symbolSystem.isActive());
            close(0, -0.51, 21, 10,  2,2);
            IsTrue(symbolSystem.isActive());
        }         

        [Test]
        public void testIsValidBeta() {
            IsTrue(LiqInjT.isValidBeta(1, 3, 1,5));         
            IsFalse(LiqInjT.isValidBeta(0.9,3, 1,5));         
            IsFalse(LiqInjT.isValidBeta(5.1, 3,1,5));         
            IsTrue(LiqInjT.isValidBeta(5,3,1,5));            
            IsTrue(LiqInjT.isValidBeta(3,1,1,5));         
            IsFalse(LiqInjT.isValidBeta(3,0.9, 1,5));         
            IsFalse(LiqInjT.isValidBeta(3,5.1,1,5));         
            IsTrue(LiqInjT.isValidBeta(3,5,1,5));            
        }

        [Test]
        public void testSize() {
            close(2, 0, 21, 0, 0,0);
            AreEqual(10, symbolSystem.tradeSize());
            close(3, 0, 11, 0, 0,0);
            AreEqual(18, symbolSystem.tradeSize());
            close(2, 0,42, 0, 0,0);            
            AreEqual(5, symbolSystem.tradeSize());
        }        
    }
}