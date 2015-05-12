using System;
using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;
using Q.Util;

namespace Q.Systems {
    [TestFixture]
    public class TestReverseDTDBase : OneSymbolSystemTestReverseDTD<ReverseDTD> {
        const double TRIGGER_LONG = -2.5;
        const int TRIGGER_SHORT = 2;
        const int EXIT_LONG = 1;
        const int EXIT_SHORT = -1;
        const int TIME_STOP_BARS = 10;
        const double MIN_PRICE = 0;
        protected DateTime lastTime = Objects.date("2003-06-25");
        public override void setUp() {
            base.setUp();
            symbolSystem.richCheap.enterTestMode();
            symbolSystem.dtd.enterTestMode();
            symbolSystem.stockPrice.enterTestMode();
            Objects.zeroTo(leadBars(), i => newBarWithRC(i, false));
        }

        protected override int leadBars() {
            return 10;
        }

        protected void newBarWithRC(double rc, bool hasClose) {
            newBarWithDTDRC(1, rc, hasClose);
        }

        protected void newBarWithDTDRC(double dtd, double rc, bool hasClose) {
            bar(2, dtd, rc, 2, hasClose);
        }

        protected void closeWithRC(double rc) {
            closeWithDTDRC(1, rc);
        }

        protected void closeWithDTDRC(double dtd, double rc) {
            close(2, dtd, rc, 2);
        }

        protected void close(double tri, double dtd, double rc, double stockPrice) {
            symbolSystem.dtd.add(lastTime, dtd);
            symbolSystem.richCheap.add(lastTime, rc);
            symbolSystem.stockPrice.add(lastTime, stockPrice);
            processClose(tri, tri, tri, tri, lastTime); // tri
        }

        protected void bar(double tri, double dtd, double rc, double stockPrice, bool hasClose) {
            if(!hasClose) symbolSystem.dtd.add(lastTime, dtd);
            if(!hasClose) symbolSystem.richCheap.add(lastTime, rc);
            if(!hasClose) symbolSystem.stockPrice.add(lastTime, stockPrice);
            processBar(tri, tri, tri, tri, lastTime); // tri
            lastTime = lastTime.AddDays(1);
        }

        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                {"exitLongLevel", EXIT_LONG},
                {"exitShortLevel", EXIT_SHORT},
                {"lengthZScore", leadBars()},
                {"tradeSize", 500},
                {"stopLoss", 1000000},
                {"triggerLong", TRIGGER_LONG},
                {"triggerShort", TRIGGER_SHORT},
                {"timeStopBars", TIME_STOP_BARS},
                {"trailingStopFlag", 0} ,
                {"minPrice", MIN_PRICE},
                {"ATRLen", 5}
            });
        }
    }
}