using System;
using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;

namespace Q.Systems {
    [TestFixture]
    public class TestTDSequential : OneSymbolSystemTest<TDSequential> {
        Bar lastBar;

        public override void setUp() {
            base.setUp();
            lastBar = null;
        }

        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                {"SetupLength", 3},
                {"CountdownLength", 5},
                {"WaitForFlipOnEntry", 1}, //wait for opp direction 'flip' before entering a trade
                {"Version", 1}, //1 = TD Setup, 2 = TDCountdown, 3 = TDCountdown Aggressive
                {"WaitForExitLevel", 1}, //0 = set trailing stop immediately, 1 = set trailing stop once exit level reached
                {"InitEquity", 1000000000},
                {"FixEquity", 1},
                {"Risk", 0.01}}); 
        }

        [Test]
        public void testSetup() {
            close(95,95,95,95, date("2008-01-01"));
            close(99, 99,99,99, date("2008-01-02"));
            close(98,98,98,98, date("2008-01-03"));
            close(97,97,97,97,date("2008-01-04"));
            close(96,96,96,96, date("2008-01-05"));  
            close(99.5,99.5,99.5,99.5,date("2008-01-06")); 
            noOrders();  //no trade because no flip to start it
            close(97.5, 97.5, 97.5, 97.5, date("2008-01-07")); // Bearish price flip
            noOrders();  
            close(95, 96.5, 85, 96.5, date("2008-01-08"));
            noOrders();  
            close(94, 97, 94, 95, date("2008-01-09"));
            noOrders(); // no flip yet
            close(100, 100, 100, 100, date("2008-01-10"));
            hasOrders(symbol().buy("LongEntry", market(), 377, oneBar()));
            fill(0, 100);
            hasOrders(symbol().sell("TrailingStop", stop(73.5), 377, oneBar()));
            processClose(102,102,102,102, date("2008-01-11"));
            hasOrders(symbol().sell("TrailingStop", stop(73.5), 377, oneBar()));
            processBar(102,102,102,102, date("2008-01-11"));
            hasOrders(symbol().sell("TrailingStop", stop(75.5), 377, oneBar()));
            processClose(103,103,103,103, date("2008-01-12"));
            hasOrders(symbol().sell("TrailingStop", stop(75.5), 377, oneBar()));
            processBar(103,103,103,103, date("2008-01-12"));
            hasOrders(symbol().sell("TrailingStop", stop(76.5), 377, oneBar()));
            processClose(104,105,104,105, date("2008-01-13"));
            hasOrders(symbol().sell("TrailingStop", stop(76.5), 377, oneBar()));
            processBar(104,105,104,105, date("2008-01-13"));
            hasOrders(symbol().sell("TrailingStop", stop(78.5), 377, oneBar()));
            processClose(89,89,89,89, date("2008-01-14"));
            hasOrders(symbol().sell("TrailingStop", stop(78.5), 377, oneBar()),
                position().exitLong("LongExitS&R", market(), oneBar()),
                symbol().sell("ShortEntry", market(), 588, oneBar()));
            fill(1, 89);
            fill(0, 89);
            processBar(89,89,89,89, date("2008-01-14"));
            hasOrders(symbol().buy("TrailingStop", stop(106), 588, oneBar()));
        }

        void close(double open, double high, double low, double closePrice, DateTime date) {
            close(open, high, low, closePrice, date, null);
        }

        
        void close(double open, double high, double low, double close, DateTime date, Order order) {
            if(lastBar != null)
                processBar(lastBar);
            if(order == null) noOrders();
            else hasOrders(order);

            lastBar = new Bar(open, high, low, close, date);
            processClose(lastBar);
        }

        protected override int leadBars() {
            return 5;
        }

    }
    
    [TestFixture]
    public class TestTDCountdown : OneSymbolSystemTest<TDSequential> {
        Bar lastBar;

        public override void setUp() {
            base.setUp();
            lastBar = null;
        }

        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                {"SetupLength", 3},
                {"CountdownLength", 4},
                {"WaitForFlipOnEntry", 1}, //wait for opp direction 'flip' before entering a trade
                {"Version", 2}, //1 = TD Setup, 2 = TDCountdown, 3 = TDCountdown Aggressive
                {"InitEquity", 1000000000},
                {"FixEquity", 1},
                {"Risk", 0.01}}); 
        }

        [Test]
        public void testCountdown() {
            close(95,95,95,95, date("2008-01-01"));
            close(99, 99,99,99, date("2008-01-02"));
            close(98,98,98,98, date("2008-01-03"));
            close(97,97,97,97,date("2008-01-04"));
            close(96,96,96,96, date("2008-01-05"));  
            close(99.5,99.5,99.5,99.5,date("2008-01-06")); //setupCount = 0
            close(97,97,97,97, date("2008-01-07")); //setupCount = -1
            close(94, 96, 94, 96, date("2008-01-08")); //setupCount = -2
            close(95, 95, 95, 95, date("2008-01-09")); //setupCount = -3, setupInPlace, .... start Countdown = 1
            close(96, 97, 91.5, 94.5, date("2008-01-10"));
            close(96, 96, 93, 94.5, date("2008-01-11")); // countdown = 2
            close(97, 97, 94, 97, date("2008-01-12"));
            close(94, 97, 92, 92, date("2008-01-13")); //countdown = 3
            close(95, 95, 92, 92, date("2008-01-14")); //countdown = 4. ready to trade
            close(95,95,95,95, date("2008-01-15")); //got the bullishpriceflip. Trade
            hasOrders(symbol().buy("LongEntry", market(), 1111, oneBar()));
            fill(0, 95);
            hasOrders(symbol().sell("TrailingStop", stop(86), 1111, oneBar()));
            processBar(95,95,95,95, date("2008-01-15"));
            processClose(96, 98, 96, 98, date("2008-01-16")); //setupCount = 2
            hasOrders(symbol().sell("TrailingStop", stop(86), 1111, oneBar()));
            processBar(96,98,96,98,date("2008-01-16"));
            hasOrders(symbol().sell("TrailingStop", stop(89), 1111, oneBar()));
            processClose(93, 95, 93, 95, date("2008-01-17")); //setupCount =3, setupInPlace, ... start Countdown
            hasOrders(symbol().sell("TrailingStop", stop(89), 1111, oneBar()));
            processBar(93, 95, 93, 95, date("2008-01-17"));
            hasOrders(symbol().sell("TrailingStop", stop(89), 1111, oneBar()));
            processClose(94, 99, 94, 99, date("2008-01-18")); // countdown = 1
            hasOrders(symbol().sell("TrailingStop", stop(89), 1111, oneBar()));
            processBar(94, 99, 94, 99, date("2008-01-18"));
            hasOrders(symbol().sell("TrailingStop", stop(90), 1111, oneBar()));
            processClose(95, 96, 94, 96, date("2008-01-19")); // countdown = 2
            hasOrders(symbol().sell("TrailingStop", stop(90), 1111, oneBar()));
            processBar(95, 96, 94, 96, date("2008-01-19"));
            hasOrders(symbol().sell("TrailingStop", stop(90), 1111, oneBar()));
            processClose(96, 100, 96, 100, date("2008-01-20")); // countdown = 3
            hasOrders(symbol().sell("TrailingStop", stop(90), 1111, oneBar()));
            processBar(96, 100, 96, 100, date("2008-01-20"));
            hasOrders(symbol().sell("TrailingStop", stop(91), 1111, oneBar()));
            processClose(98,98,97,98, date("2008-01-21")); // countdown = 4
            hasOrders(symbol().sell("TrailingStop", stop(91), 1111, oneBar()));
            processBar(98,98,97,98, date("2008-01-21"));
            hasOrders(symbol().sell("TrailingStop", stop(91), 1111, oneBar()));
            processClose(94, 94, 94, 94, date("2008-01-22")); //trade after the flip
            hasOrders(symbol().sell("TrailingStop", stop(91), 1111, oneBar()),
                position().exitLong("LongExitS&R", market(), oneBar()),
                symbol().sell("ShortEntry", market(), 1000, oneBar()));
            fill(1, 94);
            fill(0, 94);

            hasOrders(symbol().buy("TrailingStop", stop(104), 1000, oneBar()));
            processBar(94,94,94,94, date("2008-01-22"));
            hasOrders(symbol().buy("TrailingStop", stop(104), 1000, oneBar()));    

        }

        void close(double open, double high, double low, double closePrice, DateTime date) {
            close(open, high, low, closePrice, date, null);
        }

        
        void close(double open, double high, double low, double close, DateTime date, Order order) {
            if(lastBar != null)
                processBar(lastBar);
            if(order == null) noOrders();
            else hasOrders(order);

            lastBar = new Bar(open, high, low, close, date);
            processClose(lastBar);
        }

        protected override int leadBars() {
            return 5;
        }

    }

}