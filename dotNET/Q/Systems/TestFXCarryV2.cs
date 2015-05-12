using System;
using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;
using O=Q.Util.Objects;

namespace Q.Systems {
    [TestFixture]
    public class TestFXCarryV2 : OneSymbolSystemTest<FXCarryV2> {
        DateTime lastTime = O.now();
        Bar lastBar;

        public override void setUp() {
            base.setUp();
            lastBar = null;
           
            symbolSystem.payoutRatioLong.enterTestMode();
            symbolSystem.payoutRatioShort.enterTestMode();
            close(0, 0, 0);
            close(1, 0, 0);
            emailer.allowMessages();
        }

        protected override Symbol initializeSymbol() {
            var empty = createEmptyTestSeries();
            insertSymbol("RE.TES6MPOUT.C", empty);
            insertSymbol("RE.TES6MPOUT.P", empty);
            return base.initializeSymbol();
        }


        [Test]
        public void testTriggerEntryLong() {
            close(1, 0.01, 0);
            close(22, 0.01, 0); // unstop
            noOrders();
            close(2, 0.01, 0);
            noOrders();
            close(3, 1.0, 0);
            noOrders();
            close(4, 1.01, 0);
            hasOrders(symbol().buy("AttractiveBuy", market(), 11, oneBar()));
            close(5, 3, 0);
            noOrders();
            close(6, 3.01, 0);
            noOrders();
        }

        [Test]
         public void testTriggerEntryShort() {
            close(100, 0, 0.01);
            close(100, 0, 0.01);
            close(100, 0, 0.01);
            close(100, 0, 0.01);
            close(100, 0, 0.01);
            close(80, 0, 0.01); // unstop
            noOrders();
            close(98, 0, 0.01);
            noOrders();
            close(97, 0, 1);
            noOrders();
            close(96, 0, 1.01);
            hasOrders(symbol().sell("AttractiveSell", market(), 12, oneBar()));
            close(95, 0, 3);
            noOrders();
            close(94, 0, 3.01);
            noOrders();
        }

        [Test]
        public void testStartsStoppedOut() {
            close(1, 1.01, 0);
            noOrders();
            close(22, 1.01, 0);
            hasOrders(1);
        }

        [Test]
        public void testTriggerExit() {
            close(1, 0.01, 0);
            close(22, 0.01, 0); // unstop
            close(2, 1.01, 0);
            hasOrders(symbol().buy("AttractiveBuy", market(), 11, oneBar()));
            fill(0, 3.0);
            close(3, 1.01, 0);
            hasOrders(1);
            close(4, 0.51, 0);
            hasOrders(1);
            close(5, 0.5, 0);
            hasOrders(Order.ANY, position().exitLong("NotAttractiveSell", market(), oneBar()));
        }

        [Test]
        public void testTrailingStop() {
            close(1, 0.01, 0);
            close(22, 0.01, 0); // unstop
            close(2, 1.01, 0);
            hasOrders(symbol().buy("AttractiveBuy", market(), 11, oneBar()));
            fill(0, 13.0);
            close(12, 2.0, 0);
            hasOrders(position().exitLong("StopOut", stop(3.90909090909090), oneBar()));
            close(14, 2.0, 0);
            hasOrders(position().exitLong("StopOut", stop(4.90909090909090), oneBar()));
            close(6, 2.0, 0);
            hasOrders(position().exitLong("StopOut", stop(4.90909090909090), oneBar()));            
            fill(0, 4);
            close(6, 2.0, 0);
            noOrders();
            close(26, 2.0, 0);
            hasOrders(1);
            close(26.01, 2.0, 0);
            hasOrders(symbol().buy("AttractiveBuy", market(), 16, oneBar()));
            fill(0, 26);
            close(28, 2.0, 0);
            hasOrders(position().exitLong("StopOut", stop(21.75), oneBar()));
            fill(0, 15.5);
            close(15.0, 2.0, 0);
            noOrders();
            close(19.0, 2.0, 0);
            noOrders();
            close(23.0, 2.0, 0);
            noOrders();
            close(23.0, 2.0, 0);
            noOrders();
            close(100, 2.0, 0);
            hasOrders(symbol().buy("AttractiveBuy", market(), 5, oneBar()));
        }
        
       
        void close(double tri, double payoutRatioLong, double payoutRatioShort) {
              if(lastBar != null)
                processBar(lastBar);
            lastTime = lastTime.AddDays(1);
            symbolSystem.payoutRatioLong.add(lastTime, payoutRatioLong);
            symbolSystem.payoutRatioShort.add(lastTime, payoutRatioShort);
            lastBar = new Bar(tri, tri, tri, tri, lastTime);
            processClose(lastBar); // tri
            

        }


        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                { "MaxTrigger", 3 },
                { "BollingerBandBarsBack", 4 },
                { "BollingerBandDeviations", 1 },
                { "RiskDollars", 100000 },
                { "nATR", 1 },
                { "ATRLen", 5},
                { "Trigger", 1 },
                { "TriggerCushion", 0.5 }
            });
        }

        protected override int leadBars() {
            return 2;
        }
    }
}
