using System;
using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;
using O=Q.Util.Objects;

namespace Q.Systems {
    [TestFixture]
    public class TestFXCarry : OneSymbolSystemTest<FXCarry> {
        DateTime lastTime = O.now();

        public override void setUp() {
            base.setUp();
            symbolSystem.payoutRatio.enterTestMode();
            processBar(-1, 0, -1, 0, lastTime);
            lastTime = lastTime.AddDays(1);
            bar(1, 0);
            emailer.allowMessages();
        }

        protected override Symbol initializeSymbol() {
            insertSymbol("RE.TEST.POUT.C", createEmptyTestSeries());
            return base.initializeSymbol();
        }

        [Test]
        public void testTriggerEntry() {
            bar(22, 0); // unstop
            noOrders();
            bar(2, 0);
            noOrders();
            bar(3, 1.0);
            noOrders();
            bar(4, 1.01);
            hasOrders(symbol().buy("AttractiveBuy", market(), 10, oneBar()));
            bar(5, 3);
            hasOrders(symbol().buy("AttractiveBuy", market(), 10, oneBar()));
            bar(6, 3.01);
            noOrders();
        }

        [Test]
        public void testStartsStoppedOut() {
            bar(2, 1.01);
            noOrders();
            bar(22, 1.01);
            hasOrders(1);
        }

        [Test]
        public void testTriggerExit() {
            bar(22, 0); // unstop
            bar(2, 1.01);
            hasOrders(symbol().buy("AttractiveBuy", market(), 10, oneBar()));
            fill(0, 3.0);
            bar(3, 1.01);
            hasOrders(1);
            bar(4, 0.51);
            hasOrders(1);
            bar(5, 0.5);
            hasOrders(Order.ANY, position().exitLong("NotAttractiveSell", market(), oneBar()));
        }

        [Test]
        public void testTrailingStop() {
            bar(22, 0); // unstop
            bar(2, 1.01);
            hasOrders(symbol().buy("AttractiveBuy", market(), 10, oneBar()));
            fill(0, 13.0);
            bar(12, 2.0);
            hasOrders(position().exitLong("StopOut", stop(3), oneBar()));
            bar(14, 2.0);
            hasOrders(position().exitLong("StopOut", stop(4), oneBar()));
            bar(6, 2.0);
            hasOrders(position().exitLong("StopOut", stop(4), oneBar()));            
            fill(0, 4);
            bar(6, 2.0);
            noOrders();
            bar(26, 2.0);
            noOrders();
            bar(26.01, 2.0);
            hasOrders(symbol().buy("AttractiveBuy", market(), 10, oneBar()));
            fill(0, 26);
            bar(28, 2.0);
            hasOrders(position().exitLong("StopOut", stop(18), oneBar()));
            fill(0, 15.5);
            bar(15.0, 2.0);
            noOrders();
            bar(19.0, 2.0);
            noOrders();
            bar(23.0, 2.0);
            noOrders();
            bar(27.0, 2.0);
            noOrders();
            bar(100, 2.0);
            hasOrders(symbol().buy("AttractiveBuy", market(), 10, oneBar()));
        }

        void bar(double tri, double payoutRatio) {
            lastTime = lastTime.AddDays(1);
            symbolSystem.payoutRatio.add(lastTime, payoutRatio);
            processBar(tri, tri, tri, tri, lastTime); // tri
        }

        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                { "MaxTrigger", 3 },
                { "RecoveryAmount", 20000 },
                { "RecoveryPeriod", 2 },
                { "StopLoss", 10000 },
                { "TradeSize", 10 },
                { "Trigger", 1 },
                { "TriggerCushion", 0.5 }
            });
        }

        protected override int leadBars() {
            return 2;
        }
    }
}
