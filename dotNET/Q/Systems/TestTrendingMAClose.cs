using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;
using Q.Util;

namespace Q.Systems {
    [TestFixture]
    public class TestTrendingMAClose : OneSymbolSystemTest<TrendingMAClose> {
        Bar lastBar;

        public override void setUp() {
            base.setUp();
            lastBar = null;
            Objects.zeroTo(
                arguments().leadBars,
                i => {
                    processBar(8, 8, 8, 8);
                    noOrders();
                });
        }

        [Test]
        public void testEnterLongExit() {
            AreEqual(symbolSystem.bars[0].close, 8);
            close(7, 7, 7, 7);
            close(7, 7, 7, 7);
            close(7, 7, 7, 7);
            close(7.5, 7.5, 7.5, 7.5);
            noOrders();
            close(11, 11, 11, 11);
            requireLongEntry(11, 25);
            fill(0,11);
            
            close(11, 11, 11, 11);
            requireLongStop(7.000);
            close(10, 10, 10, 10);
            requireLongStop(7.000);
            close(10, 10, 10, 10);
            requireLongStop(7.000);
            close(10, 10, 10, 10);
            requireLongStop(7.000);
            close(9, 9, 9, 9);
            requireLongStop(7.000);
            close(8, 8, 8, 8);
            requireLongExit(8.0, 7.000);
            fill(1, 8.0);
            noOrders();
        }

        [Test]
        public void testEnterShortExit() {
            AreEqual(symbolSystem.bars[0].close, 8);
            close(8.5, 8.5, 8.5, 8.5);
            close(8.5, 8.5, 8.5, 8.5);
            close(8.5, 8.5, 8.5, 8.5);
            close(8, 8, 8, 8);
            noOrders();
            close(7, 7, 7, 7);
            requireShortEntry(7.0, 62);
            fill(0,7.0);

            close(7, 7, 7, 7);
            requireShortStop(8.61290322580645);
            close(7, 7, 7, 7);
            requireShortStop(8.61290322580645);
            close(7.5, 7.5, 7.5, 7.5);
            requireShortStop(8.61290322580645);
            close(7.5, 7.5, 7.5, 7.5);
            requireShortStop(8.61290322580645);
            close(7.5, 7.5, 7.5, 7.5);
            requireShortStop(8.61290322580645);
            close(8, 8, 8, 8);
            requireShortExit(8.0, 8.61290322580645);
            fill(1,8.0);
            noOrders();

                        
}

        [Test]
        public void testEnterLongStopOutActivate() {
            AreEqual(symbolSystem.bars[0].close, 8);
            close(7, 7, 7, 7);
            close(7, 7, 7, 7);
            close(7, 7, 7, 7);
            close(7.5, 7.5, 7.5, 7.5);
            noOrders();
            close(11, 11, 11, 11);
            requireLongEntry(11, 25);
            fill(0,11);
            close(12, 12, 12, 12);
            requireLongStop(8.0);
            close(7.5, 7.5, 7.5, 7.5);
            requireLongStop(8.0);
            fill(0,7.5);

            close(7.75,7.75,7.75,7.75);
            noOrders();
            close(7, 7, 7, 7);
            noOrders();
            close(6, 6, 6, 6);
            requireShortEntry(6.0, 16);
            fill(0, 6);
            close(6, 6, 6, 6);
            requireShortStop(12.25);
        }
        
        [Test]
        public void testEnterShortStopOutActivate() {
            AreEqual(symbolSystem.bars[0].close, 8);
            close(8.5, 8.5, 8.5, 8.5);
            close(8.5, 8.5, 8.5, 8.5);
            close(8.5, 8.5, 8.5, 8.5);
            close(8, 8, 8, 8);
            noOrders();
            close(7, 7, 7, 7);
            requireShortEntry(7.0, 62);
            fill(0,7.0);

            close(5, 5, 5, 5);
            requireShortStop(6.61290322580645);
            close(5, 5, 5, 5);
            requireShortStop(6.61290322580645);
            close(6.75, 6.75, 6.75, 6.75);
            requireShortStop(6.61290322580645);
            fill(0, 6.75);

            close(9, 9, 9, 9);
            noOrders();
            close(10, 10, 10, 10);
            requireLongEntry(10, 17);
            fill(0, 10.0);
            close(10, 10, 10, 10);
            requireLongStop(4.11764705882353);
        }

        void requireLongEntry(double longEntry, long size) {
            hasOrders(
                symbol().buy("Upper Range Break", stop(longEntry), size, oneBar())
                );
        }
        
        
        void requireLongExit(double reversalLevel, double stopLevel) {
            hasOrders(
                position().exit("Exit L StopOut", stop(stopLevel), oneBar()),
                position().exit("Exit L Reversal", stop(reversalLevel), oneBar()));
        }

        void requireLongStop(double stopLevel) {
            hasOrders(
                position().exit("Exit L StopOut", stop(stopLevel), oneBar())
            );
        }

        void requireShortEntry(double shortEntry, long size) {
            hasOrders(
                symbol().sell("Lower Range Break", stop(shortEntry), size, oneBar())
                );
        }
        
        void requireShortStop(double stopLevel) {
            hasOrders(
                position().exit("Exit S StopOut",stop(stopLevel), oneBar())
            );
        }
        void requireShortExit(double reversalLevel, double stopLevel) {
            hasOrders(
                position().exit("Exit S StopOut", stop(stopLevel), oneBar()),
                position().exit("Exit S Reversal", stop(reversalLevel), oneBar())
                );
        }
        
        void close(double open, double high, double low, double close) {
            if(lastBar != null)
                processBar(lastBar);
            lastBar = new Bar(open, high, low, close);
            processClose(lastBar);
        }


        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                { "MADays", 5.0 },
                { "RiskDollars", 1000000 },
                { "nATR", 4.0 },
                { "ATRLen", 5.0 },
                { "BollingerBandBarsBack", 4.0 },
                { "BollingerBandDeviations", 1.5 }
            });
        }

        protected override int leadBars() {
            return 5;
        }
        
        protected override Symbol initializeSymbol() {
            return new Symbol("RE.TEST.TY.1C", 10000);
        }
    }
}