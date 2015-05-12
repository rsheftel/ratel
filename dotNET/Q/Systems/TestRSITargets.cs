using System;
using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;

namespace Q.Systems {
    [TestFixture]
    public class TestRSITargets : OneSymbolSystemTest<RSITargets> {
        DateTime current = date("1990/1/1");
        Bar bar;

        protected override int leadBars() {
            return 3;
        }

        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                {"LeadBars", 3},
                {"HalfLife", 5},
                {"EntryLevel", 30},
                {"ExitLevel", 60},
                {"InitEquity", 1000000},
                {"FixEquity", 1},
                {"Risk", 0.5},
                {"ATRLen", 3},
                {"nATR", 2}});
        }
    

        [Test]
        public void testRSITargetsBase () {
            close(100, 100, 100, 100);
            noOrders();
            close(101,101,101,101);
            noOrders();
            close(104,104,104,104);
            noOrders();
            close(102,102,102,102);
            hasOrders(symbol().sell("ShortEntry",market(),241,oneBar()));
            fill(0, 102);
            close(103,103,103,103);
            hasOrders(symbol().buy("CoverStopLoss", stop(104.074688796681), 241, oneBar()));
            close(104,104,104,104);
            hasOrders(symbol().buy("CoverStopLoss", stop(104.074688796681), 241, oneBar()));
            close(97,97,97,97);
            hasOrders(symbol().buy("CoverStopLoss", stop(99.074688796681), 241, oneBar()));
            close(94,94,94,94);
            hasOrders(position().exit("ExitShort",market(),oneBar()));
            fill(0,94.0);
            close(90,90,90,90);
            hasOrders(symbol().buy("LongEntry", market(), 91, oneBar()));
            fill(0,90.0);
            close(108,108,108,108);
            hasOrders(position().exit("ExitLong", market(), oneBar()));
            fill(0,108.0);
            close(113,113,113,113);
            hasOrders(symbol().sell("ShortEntry", market(), 43, oneBar()));
            fill(0,113.0);
            close(120,120,120,120);
            hasOrders(symbol().buy("CoverStopLoss",stop(124.627906976744), 43, oneBar()));
            fill(0, 130.0);
            close(121,121,121,121);
            noOrders();
            close(103,103,103,103);
            noOrders();
            close(135,135,135,135);
            hasOrders(symbol().sell("ShortEntry", market(), 19, oneBar()));
        }

        void close(double open, double high, double low, double close) {
            if(bar != null)
                processBar(bar);
            noOrders();
            bar = new Bar(open, high, low, close, current);
            processClose(bar);
            current = current.AddDays(1);
        }
    }

    
}