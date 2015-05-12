using System;
using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;
using O = Q.Util.Objects;

namespace Q.Systems {
    [TestFixture]
    public class TestDTDRichCheap : OneSymbolSystemTestCDS<DTDRichCheap> {
        const double TRIGGER_LONG = -2.5;
        const int TRIGGER_SHORT = 2;
        const int EXIT_LONG = 1;
        const int EXIT_SHORT = -1;
        DateTime lastTime = O.now();
        public override void setUp() {
            base.setUp();
            symbolSystem.richCheap.enterTestMode();
            symbolSystem.dtd.enterTestMode();
            O.zeroTo(leadBars(), i => newBarWithRC(i));
        }

        void newBarWithRC(double rc) {
            newBarWithDTDRC(1, rc);
        }
        void newBarWithDTDRC(double dtd, double rc) {
            bar(1, dtd, rc);
        }

        void bar(double tri, double dtd, double rc) {
            symbolSystem.dtd.add(lastTime, dtd);
            symbolSystem.richCheap.add(lastTime, rc);
            processBar(tri, tri, tri, tri, lastTime); // tri
            lastTime = lastTime.AddDays(1);
        }

        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                {"exitLongLevel", EXIT_LONG},
                {"exitShortLevel", EXIT_SHORT},
                {"lengthZScore", leadBars()},
                {"longSize", 10},
                {"lossStopLevel", 10000000},
                {"shortSize", 100},
                {"triggerLong", TRIGGER_LONG},
                {"triggerShort", TRIGGER_SHORT}
            });
        }

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
                {parameter, value}
            };
            var qre = new QREBridge<IndependentSymbolSystems<DTDRichCheap>>(arguments(these));
            var ss = qre.system.systems_[symbol()];
            AreEqual(inputsValid, ss.dtd != null);
        }

        [Test] 
        public void testProbableLbo() {
            newBarWithRC(13);
            hasOrders(symbol().sell("SE", market(), 100, oneBar()));
            fill(0, 10.0);
            newBarWithDTDRC(4, 10); // no effect
            noOrders();
            newBarWithDTDRC(4, 10); // no effect
            noOrders();
            newBarWithDTDRC(4.1, 10); // should be interested now
            noOrders();
            newBarWithDTDRC(4.1, 10); // would have triggered here but the delta between dtd and dtd[3] is not yet at 2.
            noOrders();
            newBarWithDTDRC(1, 10);
            noOrders();
            newBarWithDTDRC(4.1, 10);
            noOrders();
            newBarWithDTDRC(4.1, 10); // still not at the necessary delta
            noOrders();
            var position = symbolSystem.position();
            newBarWithDTDRC(4.1, 10); // boom
            hasOrders(position.exit("Cover Probable LBO", market(), oneBar()));
            fill(0, 10);
            newBarWithDTDRC(3, 1000); // system won't do anything here because it is waiting to see if the LBO backs off
            noOrders();
            newBarWithDTDRC(2.5, 10000); // hmm, dtd is falling, maybe itthe lbo fell through
            noOrders();
            newBarWithDTDRC(2.5, 100000); // think it fell through! let's buy something...
            hasOrders(1);
        }

        [Test]
        public void testStopsAtStopLoss() {
            newBarWithRC(13);
            hasOrders(symbol().sell("SE", market(), 100, oneBar()));
            fill(0, 10.0);
            noOrders();
            bar(11, 1.5, 10);
            noOrders();
            bar(15, 1.5, 10);
            noOrders();
            bar(19, 1.5, 10);
            noOrders();
            bar(19.9999, 1.5, 10); // almost stopped.
            noOrders();
            var position = symbolSystem.position();
            bar(20, 1.5, 10);
            noOrders();
            bar(20.01, 1.5, 100);
            hasOrders(position.exit("Cover Stop Loss", market(), oneBar()));
            fill(0, 20.01);
            bar(25, 1.5, 200);
            hasOrders(0); // z score has to cross zero. price is irrelavant now.
            bar(15, 1.5, -2000); // force z score to cross zero and activate a long entry
            hasOrders(symbol().buy("LE", market(), 10, oneBar()));
        }

        [Test]
        public void testStopWorksBothDirections() {
            newBarWithRC(-8);
            hasOrders(symbol().buy("LE", market(), 10, oneBar()));
            fill(0, 10.0);
            noOrders();
            bar(5, 1.5, 5);
            noOrders();
            var position = symbolSystem.position();
            bar(-90, 1.5, 5);
            noOrders();
            bar(-90.01, 1.5, -20);
            hasOrders(position.exit("Sell Stop Loss", market(), oneBar()));
            fill(0, 20.01);
            bar(25, 1.5, -50);
            hasOrders(0); // z score has to cross zero. price is irrelavant now.
            bar(15, 1.5, 2000); // force z score to cross zero and activate a long entry
            hasOrders(symbol().sell("SE", market(), 100, oneBar()));
        }
        [Test]
        public void testShortEntryAndExit() {
            noOrders();
            newBarWithRC(13);
            hasOrders(symbol().sell("SE", market(), 100, oneBar()));
            fill(0, 10.0);
            newBarWithRC(13);
            noOrders();
            newBarWithRC(5);
            noOrders();
            hasPosition(-100);
            newBarWithRC(1);
            hasOrders(symbol().buy("SX", market(), 100, oneBar()));
        }

        [Test]
        public void testLongEntryAndExit() {
            noOrders();
            newBarWithRC(-8);
            hasOrders(symbol().buy("LE", market(), 10, oneBar()));
            fill(0, 10.0);
            newBarWithRC(-8);
            noOrders();
            newBarWithRC(5);
            noOrders();
            hasPosition(10);
            newBarWithRC(10);
            hasOrders(symbol().sell("LX", market(), 10, oneBar()));
        }

        protected override int leadBars() {
            return 10;
        }
    }
}
