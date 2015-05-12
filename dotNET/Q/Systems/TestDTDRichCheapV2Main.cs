using NUnit.Framework;
using Q.Trading;
using Q.Util;

namespace Q.Systems {
    [TestFixture]
    public class TestDTDRichCheapV2Main : TestDTDRichCheapV2Base {
        const double TRIGGER_LONG = -2.5;
        const int TRIGGER_SHORT = 2;
        const int EXIT_LONG = 1;
        const int EXIT_SHORT = -1;
        const int TIME_STOP_BARS = 10;
        const double MAX_SPREAD = 0.1200;
        const double PROFIT_OBJECTIVE_MULTIPLE = 0;

        [Test]
        public void testInputVerification() {
            makeSystem("exitShortLevel", 1.9999, true);
            makeSystem("exitShortLevel", 2, false);
            makeSystem("exitShortLevel", 2.0001, false);

            makeSystem("exitLongLevel", -2.4999, true);
            makeSystem("exitLongLevel", -2.5001, false);
            makeSystem("exitLongLevel", -2.5, false);

            makeSystem("exitShortLevel", -2.4999, true);
            makeSystem("exitShortLevel", -2.5001, false);
            makeSystem("exitShortLevel", -2.5, false);

            makeSystem("exitLongLevel", 1.9999, true);
            makeSystem("exitLongLevel", 2, false);
            makeSystem("exitLongLevel", 2.0001, false);


        }

        void makeSystem(string parameter, double value, bool inputsValid) {
            var these = new Parameters {
                {"systemId", systemId},
                {"RunMode", (double) RunMode.RIGHTEDGE },
                {"exitLongLevel", EXIT_LONG},
                {"exitShortLevel", EXIT_SHORT},
                {"lengthZScore", leadBars()},
                {"longSize", 10},
                {"lossStopLevel", 1000000},
                {"shortSize", 100},
                {"triggerLong", TRIGGER_LONG},
                {"triggerShort", TRIGGER_SHORT},
                {"timeStopBars", TIME_STOP_BARS},
                {"trailingStopFlag", 0},
                {"maxSpread", MAX_SPREAD},
                {"profitObjectiveMultiple", PROFIT_OBJECTIVE_MULTIPLE},
                {parameter, value}
            };
            var qre = new QREBridge<IndependentSymbolSystems<DTDRichCheapV2>>(arguments(these));
            var ss = qre.system.systems_[symbol()];
            AreEqual(inputsValid, ss.dtd != null);
        }

        [Test] 
        public void testProbableLbo() {
            closeWithRC(13);
            hasOrders(symbol().sell("SE", market(), 100, oneBar()));
            fill(0, 10.0);
            newBarWithRC(13, true);
            newBarWithDTDRC(4, 10, false); // no effect
            noOrders();
            newBarWithDTDRC(4, 10, false); // no effect
            noOrders();
            newBarWithDTDRC(4.1, 10, false); // should be interested now
            noOrders();
            newBarWithDTDRC(4.1, 10, false); // would have triggered here but the delta between dtd and dtd[3] is not yet at 2.
            noOrders();
            newBarWithDTDRC(1, 10, false);
            noOrders();
            newBarWithDTDRC(4.1, 10, false);
            noOrders();
            newBarWithDTDRC(4.1, 10, false); // still not at the necessary delta
            noOrders();
            var position = symbolSystem.position();
            closeWithDTDRC(4.1, 10); // boom
            hasOrders(position.exit("Cover Probable LBO", market(), oneBar()));
            fill(0, 10);
            newBarWithDTDRC(4.1, 10, true);
            newBarWithDTDRC(3, 1000, false); // system won't do anything here because it is waiting to see if the LBO backs off
            noOrders();
            newBarWithDTDRC(2.5, 10000, false); // hmm, dtd is falling, maybe itthe lbo fell through
            noOrders();
            closeWithDTDRC(2.5, 100000);
            hasOrders(1);
            newBarWithDTDRC(2.5, 100000, true); // think it fell through! let's buy something...
        }

        [Test]
        public void testStopsAtStopLoss() {
            closeWithRC(13);
            hasOrders(symbol().sell("SE", market(), 100, oneBar()));
            fill(0, 10.0);
            noOrders();
            newBarWithRC(13, true);
            bar(11, 1.5, 10, 0.01, false);
            noOrders();
            bar(15, 1.5, 10, 0.01, false);
            noOrders();
            bar(19, 1.5, 10, 0.01, false);
            noOrders();
            bar(19.9999, 1.5, 10, 0.01, false); // almost stopped.
            noOrders();
            var position = symbolSystem.position();
            bar(20, 1.5, 10, 0.01, false);
            noOrders();
            close(20.01, 1.5, 10, 0.01);
            hasOrders(position.exit("Cover Stop Loss", market(), oneBar()));
            fill(0, 20.01);
            bar(20.01, 1.5, 100, 0.01, true);
            noOrders();
            bar(25, 1.5, 200, 0.01, false);
            noOrders(); // z score has to cross zero. price is irrelavant now.
            close(15, 1.5, -260, 0.01);
            hasOrders(symbol().buy("LE", market(), 10, oneBar()));
            bar(15, 1.5, -260, 0.01, true); // force z score to cross zero and activate a long entry
        }

        [Test]
        public void testStopWorksBothDirections() {
            closeWithRC(-8);
            hasOrders(symbol().buy("LE", market(), 10, oneBar()));
            fill(0, 10.0);
            noOrders();
            newBarWithRC(-8, true);
            bar(5, 1.5, 5, 0.01, false);
            noOrders();
            var position = symbolSystem.position();
            bar(-90, 1.5, 5, 0.01, false);
            noOrders();
            close(-90.01, 1.5, 5, 0.01);
            hasOrders(position.exit("Sell Stop Loss", market(), oneBar()));
            fill(0, 20.01);
            bar(-90.01, 1.5, 5, 0.01, true);
            bar(25, 1.5, -50, 0.01, false);
            noOrders(); // z score has to cross zero. price is irrelavant now.
            close(15, 1.5, 2000, 0.01); // force z score to cross zero and activate a long entry
            hasOrders(symbol().sell("SE", market(), 100, oneBar()));
            bar(15, 1.5, 2000, 0.01, true);
        }
        [Test]
        public void testShortEntryAndExit() {
            noOrders();
            closeWithRC(13);
            hasOrders(symbol().sell("SE", market(), 100, oneBar()));
            fill(0, 10.0);
            newBarWithRC(13, true);
            newBarWithRC(13, false);
            noOrders();
            newBarWithRC(5, false);
            noOrders();
            hasPosition(-100);
            closeWithRC(1);
            hasOrders(symbol().buy("SX", market(), 100, oneBar()));
            newBarWithRC(1, true);
        }

        [Test]
        public void testLongEntryAndExit() {
            noOrders();
            closeWithRC(-8);
            hasOrders(symbol().buy("LE", market(), 10, oneBar()));
            fill(0, 10.0);
            newBarWithRC(-8, true);
            newBarWithRC(-8, false);
            noOrders();
            newBarWithRC(5, false);
            noOrders();
            hasPosition(10);
            closeWithRC(10);
            hasOrders(symbol().sell("LX", market(), 10, oneBar()));
            newBarWithRC(10, true);
        }

        [Test]
        public void testTimeStop() {
            noOrders();
            closeWithRC(-8);
            hasOrders(symbol().buy("LE", market(), 10, oneBar()));
            fill(0, 10.0);
            newBarWithRC(-8, true);
            closeWithRC(-8);    //1
            noOrders();
            newBarWithRC(-8, true);
            closeWithRC(-8);   //2
            noOrders();
            newBarWithRC(-8, true);
            closeWithRC(-8);   //3
            noOrders();
            newBarWithRC(-8, true);
            closeWithRC(-8);   //4
            noOrders();
            newBarWithRC(-8, true);
            closeWithRC(-8);   //5
            noOrders();
            newBarWithRC(-8, true);
            closeWithRC(-8);   //6
            noOrders();
            newBarWithRC(-8, true);
            closeWithRC(-8);   //7
            noOrders();
            newBarWithRC(-8, true);
            closeWithRC(-8);   //8
            noOrders();
            newBarWithRC(-8, true);
            closeWithRC(-8);   //9
            noOrders();
            newBarWithRC(-8, true);
            closeWithRC(-8);   //10. This is > time stop, so exit
            hasOrders(symbol().sell("Sell Time Stop", market(), 10, oneBar()));
            fill(0, 10.0);
            newBarWithRC(-8, true);
            closeWithRC(-8);
            noOrders();
            newBarWithRC(-8, true);  // no trade until reset
            closeWithRC(0.5);
            newBarWithRC(0.5, true);
            closeWithRC(-20);
            hasOrders(symbol().buy("LE", market(), 10, oneBar()));
            fill(0, 10.0);
            newBarWithRC(-20, true);
        }

        [Test]
        public void testMaxSpreadSell() {
            closeWithSpread(1, 13, 0.1201);
            noOrders();
            bar(1, 1, 13, 0.1201, true);
            closeWithSpread(1, 16, 0.1200);
            hasOrders(symbol().sell("SE", market(), 100, oneBar()));
            fill(0, 1);
            bar(1, 1, 16, 0.12, true);
            noOrders();
        }

        [Test]
        public void testMaxSpreadBuy() {
            closeWithSpread(1, -8, 0.1201);
            noOrders();
            bar(1, 1, -8, 0.1201, true);
            closeWithSpread(1, -22, 0.1200);
            hasOrders(symbol().buy("LE", market(), 10, oneBar()));
            fill(0,1);
            bar(1, 1, -22, 0.12, true);
            noOrders();
        }

        [Test]
        public void testEndMarketPeriod() {
            closeWithSpread(1, -8, 0.1201);
            noOrders();
            bar(1, 1, -8, 0.1201, true);
            closeWithSpread(1, -22, 0.1200);
            hasOrders(symbol().buy("LE", market(), 10, oneBar()));
            fill(0,1);
            bar(1, 1, -22, 0.12, true);
            lastTime = Objects.date("2007-10-10");
            closeWithSpread(1, -22, 0.1200);
            hasOrders(symbol().sell("LX END MARKET PERIOD", market(), 10, oneBar()));
            fill(0,1);
            bar(1,1,-22,0.12, true);
            noOrders();
        }

    }
}