using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;

namespace Q.Systems {
    [TestFixture]
    public class TestDTDRichCheapV2ProfitObjective : TestDTDRichCheapV2Base {
        const double TRIGGER_LONG = -1.5;
        const double TRIGGER_SHORT = 2;
        const double EXIT_LONG = 1.99;
        const double EXIT_SHORT = -1;
        const int TIME_STOP_BARS = 10;
        const double MAX_SPREAD = 0.1200;
        const double PROFIT_OBJECTIVE_MULTIPLE = 2.1;
        
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

        [Test]
        public void testWithProfitObjectiveShort() {
                closeWithSpread(1, 13, 0.01);
                noOrders();
                bar(1, 1, 13, 0.01, true);
                closeWithSpread(1, 20, 0.01);
                hasOrders(symbol().sell("SE", market(), 100, oneBar()));
                fill(0,1.0);
                bar(1,1,20,0.01, true);
                noOrders();
        }

        [Test]
        public void testWithProfitObjectiveLong() {
                closeWithSpread(1, 0, 0.01);
                noOrders();
                bar(1, 1, 0, 0.01, true);
                closeWithSpread(1, -1, 0.01);
                hasOrders(symbol().buy("LE", market(), 10, oneBar()));
                fill(0,1);
                bar(1, 1, -1, 0.01, true);
                noOrders();
        }
    }
}