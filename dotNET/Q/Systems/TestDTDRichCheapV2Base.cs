using System;
using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;
using Q.Util;

namespace Q.Systems {
    [TestFixture]
    public class TestDTDRichCheapV2Base : OneSymbolSystemTestCDS<DTDRichCheapV2> {
        const double TRIGGER_LONG = -2.5;
        const int TRIGGER_SHORT = 2;
        const int EXIT_LONG = 1;
        const int EXIT_SHORT = -1;
        const int TIME_STOP_BARS = 10;
        const double MAX_SPREAD = 0.1200;
        const double PROFIT_OBJECTIVE_MULTIPLE = 0;
        protected DateTime lastTime = Objects.date("2003-06-25");
        public override void setUp() {
            base.setUp();
            symbolSystem.richCheap.enterTestMode();
            symbolSystem.dtd.enterTestMode();
            symbolSystem.spread.enterTestMode();
            Objects.zeroTo(leadBars(), i => newBarWithRC(i, false));
        }

        protected override int leadBars() {
            return 10;
        }

        protected void newBarWithRC(double rc, bool hasClose) {
            newBarWithDTDRC(1, rc, hasClose);
        }

        protected void newBarWithDTDRC(double dtd, double rc, bool hasClose) {
            bar(1, dtd, rc, 0.01, hasClose);
        }

        protected void closeWithRC(double rc) {
            closeWithDTDRC(1, rc);
        }

        protected void closeWithDTDRC(double dtd, double rc) {
            close(1, dtd, rc, 0.01);
        }

        protected void closeWithSpread(double dtd, double rc, double spread) {
            close(1, dtd, rc, spread);
        }

        protected void close(double tri, double dtd, double rc, double spread) {
            symbolSystem.dtd.add(lastTime, dtd);
            symbolSystem.richCheap.add(lastTime, rc);
            symbolSystem.spread.add(lastTime, spread);
            processClose(tri, tri, tri, tri, lastTime); // tri
        }

        protected void bar(double tri, double dtd, double rc, double spread, bool hasClose) {
            if(!hasClose) symbolSystem.dtd.add(lastTime, dtd);
            if(!hasClose) symbolSystem.richCheap.add(lastTime, rc);
            if(!hasClose) symbolSystem.spread.add(lastTime, spread);
            processBar(tri, tri, tri, tri, lastTime); // tri
            lastTime = lastTime.AddDays(1);
        }

        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                {"exitLongLevel", EXIT_LONG},
                {"exitShortLevel", EXIT_SHORT},
                {"lengthZScore", leadBars()},
                {"longSize", 10},
                {"lossStopLevel", 1000000},
                {"shortSize", 100},
                {"triggerLong", TRIGGER_LONG},
                {"triggerShort", TRIGGER_SHORT},
                {"timeStopBars", TIME_STOP_BARS},
                {"maxSpread", MAX_SPREAD},
                {"profitObjectiveMultiple", PROFIT_OBJECTIVE_MULTIPLE},
                {"trailingStopFlag", 0}
            });
        }
    }
}