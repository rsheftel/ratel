using System;
using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;

namespace Q.Systems {
    [TestFixture]
    public class TestFadeMonthEndPush : OneSymbolSystemTest<FadeMonthEndPush> {
        Bar lastBar;

        public override void setUp() {
            base.setUp();
            lastBar = null;
        }

        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                {"NDays", 2},
                {"Multiple",2},
                {"ATRType",1},
                {"ATRLen",3},
                {"financialCalendar", 1}, // 1 == 'nyb'
                {"InitEquity", 100000000},
                {"FixEquity", 1},
                {"Risk", 0.01}});
        }

        [Test]
        public void testShortFade() {
            close(100, 100, 100, 100, date("2008-01-25"));
            close(101, 101, 101, 101, date("2008-01-28"));
            close(102, 104,101,102,date("2008-01-29"));
            close(103, 103.5, 102.5, 103.5, date("2008-01-30"));
            close(102, 115, 102, 115, date("2008-01-31"));
            hasOrders(symbol().sell("ShortEntry", market(), 292, oneBar()));
            fill(0, 0.0);
            close(101,102,100,101, date("2008-02-01"));
            hasOrders(position().exit("ExitPosition", market(), oneBar()));
        }

        [Test]
        public void testLongFade() {
            close(100, 100, 100, 100, date("2008-01-25")); // lead
            close(99, 99, 97, 98, date("2008-01-28")); // lead
            close(101, 103,97,101,date("2008-01-29")); // lead
            close(100, 100, 100, 100, date("2008-01-30")); 
            close(95, 97, 80, 80, date("2008-01-31"));
            hasOrders(symbol().buy("LongEntry", market(), 185, oneBar()));
            fill(0, 0.0);
            close(101,102,100,101, date("2008-02-01"));
            hasOrders(position().exit("ExitPosition", market(), oneBar()));
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
    public class TestFadeMonthEndPushNewParameters : OneSymbolSystemTest<FadeMonthEndPush> {
        Bar lastBar;
        
        public override void setUp() {
            base.setUp();
            lastBar = null;
        }

        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                {"NDays", 1},
                {"Multiple",2},
                {"ATRType",1},
                {"ATRLen",3},
                {"financialCalendar", 1}, // 1 == 'nyb'
                {"InitEquity", 100000000},
                {"FixEquity", 1},
                {"Risk", 0.01}});
        }

        [Test]
        public void testShortFade() {
            close(100, 100, 100, 100, date("2008-01-25"));
            close(101, 101, 101, 101, date("2008-01-28"));
            close(102, 104,101,102,date("2008-01-29"));
            close(103, 103.5, 102.5, 103.5, date("2008-01-30"));
            close(102, 115, 102, 115, date("2008-01-31"));
            hasOrders(symbol().sell("ShortEntry", market(), 292, oneBar()));
            fill(0, 0.0);
            close(101,102,100,101, date("2008-02-01"));
            hasOrders(position().exit("ExitPosition", market(), oneBar()));
        }

        [Test]
        public void testLongFade() {
            close(100, 100, 100, 100, date("2008-01-25"));
            close(99, 99, 97, 98, date("2008-01-28"));
            close(101, 103,97,101,date("2008-01-29"));
            close(100, 100, 100, 100, date("2008-01-30"));
            close(95, 97, 80, 80, date("2008-01-31"));
            hasOrders(symbol().buy("LongEntry", market(), 185, oneBar()));
            fill(0, 0.0);
            close(101,102,100,101, date("2008-02-01"));
            hasOrders(position().exit("ExitPosition", market(), oneBar()));
        }

        [Test]
        public void testNoTrade() {
            close(100, 100, 100, 100, date("2008-01-25"));
            close(99, 99, 97, 98, date("2008-01-28"));
            close(101, 103,90,90,date("2008-01-29"));
            close(100, 100, 100, 100, date("2008-01-30"));
            close(101, 103, 97, 101, date("2008-01-31"));
            noOrders();
            close(101,102,100,101, date("2008-02-01"));
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
    
    [TestFixture]
    public class TestFadeMonthEndPushStopLoss : OneSymbolSystemTest<FadeMonthEndPushStopLoss> {
        Bar lastBar;

        
        public override void setUp() {
            base.setUp();
            lastBar = null;
        }

        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                {"NDays", 1},
                {"Multiple",2},
                {"ATRType",1},
                {"ATRLen",3},
                {"financialCalendar", 1}, // 1 == 'nyb'
                {"InitEquity", 100000000},
                {"FixEquity", 1},
                {"StopLossMultiple", 2},
                {"Risk", 0.01}});
        }

        [Test]
        public void testShortFade() {
            close(100, 100, 100, 100, date("2008-01-25"));
            close(101, 101, 101, 101, date("2008-01-28"));
            close(102, 104,101,102,date("2008-01-29"));
            close(103, 103.5, 102.5, 103.5, date("2008-01-30"));
            close(102, 115, 102, 115, date("2008-01-31"));
            hasOrders(symbol().sell("ShortEntry", market(), 292, oneBar()));
            fill(0, 0.0);
            processBar(101,102,100,101, date("2008-02-01"));
            hasOrders(symbol().buy("CoverStopLoss", stop(107.849315068493), 292, oneBar()));
            fill(0, 105.0);
            processClose(101,102,100,101,date("2008-02-01"));
            noOrders();
        }

        [Test]
        public void testLongFade() {
            close(100, 100, 100, 100, date("2008-01-25"));
            close(99, 99, 97, 98, date("2008-01-28"));
            close(101, 103,97,101,date("2008-01-29"));
            close(100, 100, 100, 100, date("2008-01-30"));
            close(95, 97, 80, 80, date("2008-01-31"));
            hasOrders(symbol().buy("LongEntry", market(), 185, oneBar()));
            fill(0, 0.0);
            processBar(101,102,100,101, date("2008-02-01"));
            hasOrders(symbol().sell("SellStopLoss", stop(90.1891891891892), 185, oneBar()));
            fill(0, 99.0);
            processClose(101,102,100,101, date("2008-02-01"));
            noOrders();
        }

        [Test]
        public void testNoTrade() {
            close(100, 100, 100, 100, date("2008-01-25"));
            close(99, 99, 97, 98, date("2008-01-28"));
            close(101, 103,90,90,date("2008-01-29"));
            close(100, 100, 100, 100, date("2008-01-30"));
            close(101, 103, 97, 101, date("2008-01-31"));
            noOrders();
            close(101,102,100,101, date("2008-02-01"));
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