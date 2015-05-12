using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;

namespace Q.Systems {
    [TestFixture]
    public class TestReverseDTDATRSizing : TestReverseDTDBase {
        const int TRADE_SIZE = 0;   // 0 trade size == use ATR SIZING
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
                {"tradeSize", TRADE_SIZE},
                {"stopLoss", 1000000},
                {"triggerLong", TRIGGER_LONG},
                {"triggerShort", TRIGGER_SHORT},
                {"timeStopBars", TIME_STOP_BARS},
                {"minPrice", MIN_PRICE},
                {"trailingStopFlag", 1}
            });
        }
        
        [Test]
        public void testATRSizing() {
            close(1, 1, 0, 2);
            noOrders();
            bar(1, 1, 0, 2, true);
            close(9, 1, 0, 2);
            noOrders();
            bar(9,1,0,2, true);
            close(5,1, 15,2);
            hasOrders(symbol().sell("SE", market(), 65, oneBar()));
            fill(0,2);
            bar(5,1,15,2, true);
            
        }
    }
}