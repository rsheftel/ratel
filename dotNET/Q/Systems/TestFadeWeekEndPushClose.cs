using System;
using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;

namespace Q.Systems {
    [TestFixture]
    public class TestFadeWeekEndPushClose : OneSymbolSystemTest<FadeWeekEndPushClose> {
        Bar lastBar;

        public override void setUp() {
            base.setUp();
            lastBar = null;
        }

        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                {"NDays", 2},
                {"ExitDay", 2},
                {"Multiple",2},
                {"ATRLen",3},
                {"InitEquity", 100000000},
                {"FixEquity", 1},
                {"Risk", 0.01},
                {"StopLossMultiple", 1}});
        }

        [Test]
        public void testShortFade() {
            close(100, 100, 100, 100, date("2008-01-25"));
            close(101, 101, 101, 101, date("2008-01-28"));
            close(102, 104,101,102,date("2008-01-29"));
            close(103, 103.5, 102.5, 103.5, date("2008-01-30"));
            close(102, 115, 102, 115, date("2008-01-31"));
            close(101,120,100,120, date("2008-02-01"));
            hasOrders(symbol().sell("ShortEntry", market(), 146, oneBar()));
            fill(0, 120);
            processClose(118,118,118,118, date("2008-02-04"));
            processBar(118,118,118,118, date("2008-02-04"));
            processClose(119,119,119,119, date("2008-02-05"));
            hasOrders(symbol().buy("ExitDayReached", market(), 146, oneBar()));
            fill(0, 119);
            noOrders();
        }

        [Test]
        public void testLongFade() {
            close(100, 100, 100, 100, date("2008-01-25")); // lead
            close(99, 99, 97, 98, date("2008-01-28")); // lead
            close(101, 103,97,101,date("2008-01-29")); // lead
            close(100, 100, 100, 100, date("2008-01-30")); 
            close(95, 97, 80, 80, date("2008-01-31"));
            close(81,81,81,81, date("2008-02-01"));
            hasOrders(symbol().buy("LongEntry", market(), 223, oneBar()));
            fill(0, 81);
            processClose(80,80,80,80, date("2008-02-04"));
            processBar(80, 80, 80, 80, date("2008-02-04"));
            processClose(79, 79, 79,79, date("2008-02-05"));
            hasOrders(symbol().sell("ExitDayReached", market(), 223, oneBar()));
            fill(0, 79);
            noOrders();
        }

        [Test]
        public void testNoTrade() {
            close(100, 100, 100, 100, date("2008-01-25"));
            close(99, 99, 97, 98, date("2008-01-28"));
            close(101, 103,97,101,date("2008-01-29"));
            close(100, 100, 100, 100, date("2008-01-30"));
            close(101, 103, 97, 101, date("2008-01-31"));
            noOrders();
            close(101,102,100,101, date("2008-02-01"));
            noOrders();
            close(102, 103, 101, 102, date("2008-02-04"));
            noOrders();
            close(103, 104, 103, 103.5, date("2008-02-05"));
            noOrders();
        }

        [Test]
        public void testStoppedOut() {
            close(100, 100, 100, 100, date("2008-01-25"));
            close(101, 101, 101, 101, date("2008-01-28"));
            close(102, 104,101,102,date("2008-01-29"));
            close(103, 103.5, 102.5, 103.5, date("2008-01-30"));
            close(102, 115, 102, 115, date("2008-01-31"));
            close(101,120,100,120, date("2008-02-01"));
            hasOrders(symbol().sell("ShortEntry", market(), 146, oneBar()));
            fill(0, 120);
            close(127,127,127,127, date("2008-02-04"));
            hasOrders(symbol().buy("TrailingStopClose", market(), 146, oneBar()));
            fill(0, 127);  
        }

        void close(double open, double high, double low, double close, DateTime date) {
            if(lastBar != null)
                processBar(lastBar);
            noOrders();
            lastBar = new Bar(open, high, low, close, date);
            processClose(lastBar);
        }

        protected override int leadBars() {
            return 3;
        }
    }

    [TestFixture]
    public class TestFadeWeekEndPushCloseStopMultiple2 : OneSymbolSystemTest<FadeWeekEndPushClose> {
        Bar lastBar;

        public override void setUp() {
            base.setUp();
            lastBar = null;
        }

        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                {"NDays", 2},
                {"ExitDay", 3},
                {"Multiple",2},
                {"ATRLen",3},
                {"InitEquity", 100000000},
                {"FixEquity", 1},
                {"Risk", 0.01},
                {"StopLossMultiple", 2}});
        }

        [Test]
        public void testShortFade() {
            close(100, 100, 100, 100, date("2008-01-25"));
            close(101, 101, 101, 101, date("2008-01-28"));
            close(102, 104,101,102,date("2008-01-29"));
            close(103, 103.5, 102.5, 103.5, date("2008-01-30"));
            close(102, 115, 102, 115, date("2008-01-31"));
            close(101,120,100,120, date("2008-02-01"));
            hasOrders(symbol().sell("ShortEntry", market(), 73, oneBar()));
            fill(0, 120);
            processClose(118,118,118,118, date("2008-02-04"));
            processBar(118,118,118,118, date("2008-02-04"));
            processClose(119,119,119,119, date("2008-02-05"));
            processBar(119,119,119,119, date("2008-02-05"));
            processClose(118,118,118,118, date("2008-02-06"));
            hasOrders(symbol().buy("ExitDayReached", market(), 73, oneBar()));
            fill(0, 119);
            noOrders();
        }

        [Test]
        public void testLongFade() {
            close(100, 100, 100, 100, date("2008-01-25")); // lead
            close(99, 99, 97, 98, date("2008-01-28")); // lead
            close(101, 103,97,101,date("2008-01-29")); // lead
            close(100, 100, 100, 100, date("2008-01-30")); 
            close(95, 97, 80, 80, date("2008-01-31"));
            close(81,81,81,81, date("2008-02-01"));
            hasOrders(symbol().buy("LongEntry", market(), 111, oneBar()));
            fill(0, 81);
            processClose(80,80,80,80, date("2008-02-04"));
            processBar(80, 80, 80, 80, date("2008-02-04"));
            processClose(79, 79, 79,79, date("2008-02-05"));
            processBar(79,79,79,79,date("2008-02-05"));
            processClose(80, 80, 80, 80, date("2008-02-06"));
            hasOrders(symbol().sell("ExitDayReached", market(), 111, oneBar()));
            fill(0, 79);
            noOrders();
        }

        [Test]
        public void testStoppedOut() {
            close(100, 100, 100, 100, date("2008-01-25"));
            close(101, 101, 101, 101, date("2008-01-28"));
            close(102, 104,101,102,date("2008-01-29"));
            close(103, 103.5, 102.5, 103.5, date("2008-01-30"));
            close(102, 115, 102, 115, date("2008-01-31"));
            close(101,120,100,120, date("2008-02-01"));
            hasOrders(symbol().sell("ShortEntry", market(), 73, oneBar()));
            fill(0, 120);
            close(134,134,134,134, date("2008-02-04"));
            hasOrders(symbol().buy("TrailingStopClose", market(), 73, oneBar()));
            fill(0, 134);
            noOrders();    
        }

        void close(double open, double high, double low, double close, DateTime date) {
            if(lastBar != null)
                processBar(lastBar);
            noOrders();
            lastBar = new Bar(open, high, low, close, date);
            processClose(lastBar);
        }

        protected override int leadBars() {
            return 3;
        }
    }
}