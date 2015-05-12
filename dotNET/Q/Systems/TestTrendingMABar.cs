using NUnit.Framework;
using System.Collections.Generic;
using Q.Trading;
using Q.Util;

namespace Q.Systems {
    [TestFixture]
    public class TestTrendingMABar : OneSymbolSystemTest<TrendingMABar>{
        public override void setUp() {
            base.setUp();
            Objects.zeroTo(
                arguments().leadBars,
                i => {
                    processBar(6, 7, 6, 7);
                    noOrders();
                });
        }

    [Test]
        public void testList() {
            // enter long and exit
            // enter short and exit
            // enter long and stop and reactivate
            // enter short/stop/reactivate
        }    
       
    [Test]
        public void testEnterLongExit() {
            AreEqual(symbolSystem.bars[0].close, 7);
            processBar(7, 8, 7, 8);
            requireEntry(6.5662, 5.2671, 25); 
            fill(0, 9.0);
            processBar(9, 10, 8, 9.5);
            requireLongExit(4.2917, 5.50);
            processBar(11, 13, 10, 12);
            requireLongExit(1.4167, 8.00);
            processBar(12, 13, 12, 13);
            requireLongExit(-0.9583, 9.0);
            processBar(12, 13, 12, 13);
            requireLongExit(-1.0833, 9.0);
            processBar(9, 9, 9, 9);
            requireLongExit(3.6667, 9.0);
            processBar(9, 9, 9, 9);
            requireLongExit(9.6250, 9.0);
            fill(1,9.5);
            noOrders();
       }
   
        [Test]
        public void testEnterShortExit() {
            AreEqual(symbolSystem.bars[0].close, 7);
            processBar(7, 8, 7, 8);
            requireEntry(6.5662, 5.2671, 25); 
            fill(1, 5.0);
            processBar(5, 5, 5, 5);
            requireShortExit(7.4167, 9.0);
            processBar(4.5, 5, 4.5, 5);
            requireShortExit(8.625, 9.0);
            processBar(5, 5.5, 5, 5.5);
            requireShortExit(9.0, 9.0);
            processBar(6, 7, 6, 7);
            requireShortExit(8.7917, 9.0);
            processBar(6, 7, 6, 7);
            requireShortExit(6.0833, 9.0);
            fill(1, 6.5);
            noOrders();

        }

        [Test]
        public void EnterLongStopOutActivate() {
            AreEqual(symbolSystem.bars[0].close, 7);
            processBar(7, 8, 7, 8);
            requireEntry(6.5662, 5.2671, 25); 
            fill(0, 9.0);

            processBar(9, 10, 8, 9.5);
            requireLongExit(4.2917, 5.5);
            processBar(11, 13, 10, 12);
            requireLongExit(1.4167, 8.0);
            processBar(12, 13, 12, 13);
            requireLongExit(-0.9583,9.0);
            fill(0, 8.5);
            noOrders();
            processBar(8.5, 8.5, 8.5, 8.5);
            requireEntry(9.6479, -5.5646, 10);
            processBar(9, 9, 9, 9);
            requireEntry(11.0074, 0.4926, 10);
            processBar(9, 9, 9, 9);
            requireEntry(12.5624, 8.7709, 13);
            fill(1, 8.0);
            processBar(8, 8, 8, 8);
            requireShortExit(15.1667, 15.6923076923077);
        }
        
        [Test]
        public void enterShortStopOutActivate() {
            AreEqual(symbolSystem.bars[0].close, 7);
            processBar(7, 8, 7, 8);
            requireEntry(6.5662, 5.2671, 25); 
            fill(1, 5.0);
            
            processBar(5, 5, 5, 5);
            requireShortExit(7.4167, 9.0);
            processBar(1.5, 1.5, 1.5, 1.5);
            requireShortExit(11.1250, 5.5);
            processBar(1, 1, 1, 1);
            requireShortExit(13.9167, 5.0);
            fill(0, 6.0);
            processBar(6.0, 6.5, 6, 6.5);
            requireEntry(18.9255, 5.1579, 9);
            processBar(7, 7.5, 7, 7.5);
            requireEntry(10.7465, 2.9202, 9);
            fill(0, 11);
            processBar(11, 12, 10, 10.5);
            requireLongExit(-2.6250, -0.111111111111111);
        } 
        
        void requireEntry(double longEntry, double shortEntry, long size) {
            hasOrders(
                symbol().buy("Upper Range Break", stop(longEntry), size, oneBar()),
                symbol().sell("Lower Range Break", stop(shortEntry), size, oneBar())
                );
        }
        
        void requireLongExit(double reversalLevel, double stopLevel) {
            hasOrders(
                position().exit("Exit L StopOut", stop(stopLevel), oneBar()),
                position().exit("Exit L Reversal", stop(reversalLevel), oneBar()));
        }
        
        void requireShortExit(double reversalLevel, double stopLevel) {
            hasOrders(
                position().exit("Exit S StopOut", stop(stopLevel), oneBar()),
                position().exit("Exit S Reversal", stop(reversalLevel), oneBar())
                );
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
            return 7;
        }
        
        protected override Symbol initializeSymbol() {
            return new Symbol("RE.TEST.TY.1C", 10000);
        }
    }
}