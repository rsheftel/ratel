using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;

namespace Q.Systems {
    [TestFixture]
    public class TestDTDRichCheapV2TrailingStop : TestDTDRichCheapV2Base {
        const double TRIGGER_LONG = -2.5;
        const int TRIGGER_SHORT = 2;
        const int EXIT_LONG = 1;
        const int EXIT_SHORT = -1;
        const int TIME_STOP_BARS = 10;
        const double MAX_SPREAD = 0.1200;
        const double PROFIT_OBJECTIVE_MULTIPLE = 0;
        
        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                {"exitLongLevel", EXIT_LONG},
                {"exitShortLevel", EXIT_SHORT},
                {"lengthZScore", leadBars()},
                {"longSize", 10},
                {"lossStopLevel", 1000000},
                {"shortSize", 10},
                {"triggerLong", TRIGGER_LONG},
                {"triggerShort", TRIGGER_SHORT},
                {"timeStopBars", TIME_STOP_BARS},
                {"maxSpread", MAX_SPREAD},
                {"profitObjectiveMultiple", PROFIT_OBJECTIVE_MULTIPLE},
                {"trailingStopFlag", 1}
            });
        }

        [Test]
        public void testTrailingStop() { 
            closeWithRC(13);
            hasOrders(symbol().sell("SE", market(), 10, oneBar()));
            fill(0, 10.0);
            noOrders();
            newBarWithRC(13, true);
            close(9, 1.5, 10, 0.01);
            noOrders();
            bar(9, 1.5, 10, 0.01, true);
            noOrders();
            bar(19, 1.5, 10, 0.01, false);
            noOrders();
            var position = symbolSystem.position();
            close(19.01, 1.5, 10, 0.01);
            hasOrders(position.exit("Cover Stop Loss", market(), oneBar()));
            fill(0, 19.01);
            bar(19.01, 1.5, 10, 0.01, true);
            bar(19.01, 1.5, 100, 0.01, false); 
            noOrders(); // noOrders until zScore passes back through zero
            close(20.0, 1.5, -300, 0.01);
            hasOrders(symbol().buy("LE", market(), 10, oneBar()));
            fill(0, 20.0);
            bar(20, 1.5, -300, 0.01, true);
            noOrders();
            close(25, 1.5, -300, 0.01);
            noOrders();
            bar(25, 1.5, -300, 0.01, true);
            noOrders();
            bar(15, 1.5, -300, 0.01, false);
            noOrders();
            position = symbolSystem.position();
            close(14.99, 1.5, -300, 0.01);
            hasOrders(position.exit("Sell Stop Loss", market(), oneBar()));
            fill(0, 14.99);
            bar(14.99, 1.5, -300, 0.01, true);
            noOrders();
            bar(15, 1.5, -1000, 0.01, false);
            noOrders();
        }
    }
}