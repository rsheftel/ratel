using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;

namespace Q.Systems {
    [TestFixture]
    public class TestBuyHoldBadParameters : OneSymbolSystemTest<BuySellAndHold> {
        
        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                {"buyOrSell", -2},
                {"TradeSize", 1}});
        }

        [Test]
        public void testBadParameters() {
            processBar(100,100,100,100);
            noOrders();
            IsFalse(symbolSystem.isActive());
        }

        protected override int leadBars() {
            return 0;
        }
    }

    [TestFixture]
    public class TestBuySellAndHold : OneSymbolSystemTest<BuySellAndHold> {
        
        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                {"buyOrSell", 1},
                {"TradeSize",1}});
        }

        [Test]
        public void testInputVerification() {
            makeSystem("buyOrSell", 1, true);
            makeSystem("buyOrSell", -1, true);
            makeSystem("buyOrSell", 0, false);
            makeSystem("buyOrSell", 2, false);
            makeSystem("buyOrSell", -2, false);
        }

        void makeSystem(string parameter, double value, bool inputsValid) {
            var these = new Parameters {
                {"systemId", systemId},
                {"RunMode", (double) RunMode.RIGHTEDGE },
                {"TradeSize", 1},
                {parameter, value}
            };
            var qre = new QREBridge<IndependentSymbolSystems<BuySellAndHold>>(arguments(these));
            var ss = qre.system.systems_[symbol()];
            AreEqual(inputsValid, ss.inputsValid());
        }

        [Test] 
        public void testBuyOne() {
            processBar(100,100,100,100);
            hasOrders(symbol().buy("Buy Entry",market(), 1, oneBar()));
        }

        [Test]
        public void testBuyOneTwice() {
            processBar(100, 100, 100, 100);
            hasOrders(symbol().buy("Buy Entry",market(), 1, oneBar()));
            fill(0, 100);
            processBar(102, 102, 102, 102);
            noOrders();
            processBar(103, 103, 103, 103);
            noOrders();
        }

        protected override int leadBars() {
            return 0;
        }
    }

    
    [TestFixture]
    public class TestSellAndHold : OneSymbolSystemTest<BuySellAndHold> {
        
        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                {"buyOrSell", -1},
                {"TradeSize",2}});
        }

        [Test]
        public void testSellTwo() {
            processBar(100,100,100,100);
            hasOrders(symbol().sell("Sell Entry", market(), 2, oneBar()));
        }

        protected override int leadBars() {
            return 0;
        }
    }
}
