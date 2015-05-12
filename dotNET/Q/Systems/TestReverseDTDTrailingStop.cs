using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;

namespace Q.Systems {
    [TestFixture]
    public class TestReverseDTDTrailingStop : TestReverseDTDBase {
        const double TRIGGER_LONG = -2.5;
        const int TRIGGER_SHORT = 2;
        const int EXIT_LONG = 1;
        const int EXIT_SHORT = -1;
        const int TIME_STOP_BARS = 10;
        const double MIN_PRICE = 0;
        
        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                {"exitLongLevel", EXIT_LONG},
                {"exitShortLevel", EXIT_SHORT},
                {"lengthZScore", leadBars()},
                {"tradeSize", 20},
                {"stopLoss", 100},
                {"triggerLong", TRIGGER_LONG},
                {"triggerShort", TRIGGER_SHORT},
                {"timeStopBars", TIME_STOP_BARS},
                {"minPrice", MIN_PRICE},
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
            close(9, 1.5, 10, 9);
            noOrders();
            bar(9, 1.5, 10, 9, true);
            noOrders();
            bar(19, 1.5, 10, 19, false);
            noOrders();
            var position = symbolSystem.position();
            close(19.01, 1.5, 10, 19.01);
            hasOrders(position.exit("Cover Stop Loss", market(), oneBar()));
            fill(0, 19.01);
            bar(19.01, 1.5, 10, 19.01, true);
            bar(19.01, 1.5, 100, 19.01, false); 
            noOrders(); // noOrders until zScore passes back through zero
            close(20.0, 1.5, -300, 20);
            hasOrders(symbol().buy("LE", market(), 1, oneBar()));
            fill(0, 20.0);
            bar(20, 1.5, -300, 20, true);
            noOrders();
            close(120, 1.5, -300, 120);
            noOrders();
            bar(20, 1.5, -300, 20, true);
            noOrders();
            position = symbolSystem.position();
            close(19.99, 1.5, -300, 19.99);
            hasOrders(position.exit("Sell Stop Loss", market(), oneBar()));
            fill(0, 19.99);
            bar(19.99, 1.5, -300, 19.99, true);
            noOrders();
            bar(15, 1.5, -1000, 15, false);
            noOrders();
        }
    }
}