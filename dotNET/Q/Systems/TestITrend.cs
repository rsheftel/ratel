using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;
using O=Q.Util.Objects;

namespace Q.Systems {


    public class TestITrend : OneSymbolSystemTest<ITrend> {
        
        protected override Symbol initializeSymbol() {
            return new Symbol("ES.TS.1C",50);
        }
 
        protected override int leadBars() {
            return 1;
        }

        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {                               
                {"atrLength", 5},                
                {"nATRStop", 1},
                {"nATRTrigger", 1.6},
                {"sizeScaleSwitch", 0},
                {"risk", 10000},
                {"useDailyATR",0},
                {"timeStampClose",16},
                {"timeStampMark",10}
            });
        }
    }

    [TestFixture]
    public class TestTrendIntradaySpace1 : TestITrend {

        public void startingBars() {
            processBar(908, 909, 907.5, 908.25, O.date("2009-06-17 09:00:00"));            
            AreEqual(133, symbolSystem.tradeSize());
            processBar(904.25, 905.5, 902.75, 903.25, O.date("2009-06-17 10:00:00"));            
            AreEqual(99, symbolSystem.tradeSize());
            processBar(913, 913.5, 911.75, 912.25, O.date("2009-06-17 14:00:00"));            
            AreEqual(65, symbolSystem.tradeSize());
            processBar(905.5, 905.75, 905, 905, O.date("2009-06-17 16:00:00"));            
            AreEqual(55, symbolSystem.tradeSize());
            AreEqual(0.5, symbolSystem.scaleWins[0]);
            processBar(908.5, 909, 907.5, 908.5, O.date("2009-06-18 09:00:00"));
        }

        [Test]
        public void testTimeTolerance1() {
            startingBars();
            AreEqual(false,symbolSystem.hasBeenMarked);                   
            processBar(907.25, 913.5, 906.25, 913, O.date("2009-06-18 10:00:14"));
            AreEqual(true,symbolSystem.hasBeenMarked);                   
            AreEqual(false,symbolSystem.hasBeenClosed);                   
            hasOrders(buy("Entry L", limit(913.5), 48, oneDay()));
            fill(0, 913);            
            processBar(907.25, 913.5, 906.25, 913, O.date("2009-06-18 10:00:16"));
            hasOrders(sell("Stop L", protectiveStop(908.87), 48, oneDay()));      
            AreEqual(true,symbolSystem.hasBeenMarked);                   
        }

        [Test]
        public void testTimeTolerance2() {
            startingBars();
            processBar(907.25, 913.5, 906.25, 913, O.date("2009-06-18 09:59:46"));
            hasOrders(buy("Entry L", limit(913.5), 48, oneDay()));
            fill(0, 913);
            processBar(907.25, 913.5, 906.25, 913, O.date("2009-06-18 10:00:00"));
            hasOrders(sell("Stop L", protectiveStop(908.87), 48, oneDay()));
        }

        [Test]
        public void testTimeTolerance3() {
            startingBars();
            processBar(907.25, 913.5, 906.25, 913, O.date("2009-06-18 09:59:44"));
            noOrders();
        }

        [Test]
        public void testTimeTolerance4() {
            startingBars();
            processBar(907.25, 913.5, 906.25, 913, O.date("2009-06-18 10:00:16"));
            noOrders();
        }

        [Test]
        public void testCase1() {         
            startingBars();
            processBar(907.25,913.5,906.25,913,O.date("2009-06-18 10:00:10"));
            hasOrders(buy("Entry L", limit(913.5), 48, oneDay()));            
            fill(0,913);
            AreEqual(true,symbolSystem.hasBeenMarked);                              
            hasOrders(sell("Stop L", protectiveStop(908.87), 48, oneDay()));
            processBar(916.25,916.75,914.75,915,O.date("2009-06-18 14:00:00"));    
            hasOrders(sell("Stop L", protectiveStop(908.87), 48, oneDay()));
            AreEqual(4800,symbolSystem.lastTradePNL);
            AreEqual(true,symbolSystem.hasBeenMarked);                   
            AreEqual(false,symbolSystem.hasBeenClosed);    
            processBar(912.25,913.75,911.5,913.75,O.date("2009-06-18 16:00:00"));     
            hasOrders(
                sell("Stop L", protectiveStop(908.87), 48, oneDay()),
                sell("Exit EOD L", limit(908.75), 48, oneDay())
            );            
            fill(0,913.75);
            noOrders();
            AreEqual(false,symbolSystem.hasBeenMarked);                   
            AreEqual(true,symbolSystem.hasBeenClosed);    
            AreEqual(1800,symbolSystem.lastTradePNL);                                    
            processBar(921.25,921.75,920.5,921,O.date("2009-06-19 09:00:00"));                
            AreEqual(1,symbolSystem.scaleWins[0]);
            AreEqual(false,symbolSystem.hasBeenMarked);                   
            AreEqual(true,symbolSystem.hasBeenClosed);
            processBar(921.5,922.5,920.75,921.25,O.date("2009-06-19 10:00:00"));
            hasOrders(buy("Entry L", limit(921.75), 48, oneDay()));            
            fill(0,921.25);
            AreEqual(true,symbolSystem.hasBeenMarked);                   
            AreEqual(false,symbolSystem.hasBeenClosed);
            hasOrders(sell("Stop L", protectiveStop(917.08), 48, oneDay()));
            processBar(915.5,916.5,914.5,917.5,O.date("2009-06-19 14:00:00"));    
            hasOrders(sell("Stop L", protectiveStop(917.08), 48, oneDay()));
            AreEqual(-9000,symbolSystem.lastTradePNL);
            processBar(914.75,916,914.75,916,O.date("2009-06-19 16:00:00"));    
            hasOrders(
                sell("Stop L", protectiveStop(917.08), 48, oneDay()),
                sell("Exit EOD L", limit(911), 48, oneDay())
            );
            fill(0,916);
            AreEqual(-12600,symbolSystem.lastTradePNL);
            processBar(906.25,906.75,906,906.25,O.date("2009-06-22 09:00:00"));    
            AreEqual(0,symbolSystem.scaleWins[0]);
            AreEqual(1,symbolSystem.scaleWins[1]);
            AreEqual(0.5,symbolSystem.scaleWins[2]);
            AreEqual(0.5,symbolSystem.scaleWins[9]);
            noOrders();
            noPositions();
            processBar(902.5,903,899.5,900.25,O.date("2009-06-22 10:00:00"));    
            hasOrders(sell("Entry S", limit(899.75), 38, oneDay()));            
            fill(0,900.25);
            hasOrders(buy("Stop S", protectiveStop(905.49), 38, oneDay()));
            processBar(893,893.5,892.25,893,O.date("2009-06-22 14:00:00"));    
            hasOrders(buy("Stop S", protectiveStop(905.49), 38, oneDay()));
            AreEqual(13775,symbolSystem.lastTradePNL);
            processBar(888.5,889.5,888,884,O.date("2009-06-22 16:00:00"));    
            hasOrders(
                buy("Stop S", protectiveStop(905.49), 38, oneDay()),
                buy("Exit EOD S", limit(889),38, oneDay())
            );
            fill(0,884);
            noOrders();
            noPositions();
            AreEqual(30875,symbolSystem.lastTradePNL);
            processBar(893.5,893.75,892,892.25,O.date("2009-06-23 09:00:00"));    
            AreEqual(1,symbolSystem.scaleWins[0]);
            AreEqual(0,symbolSystem.scaleWins[1]);
            AreEqual(1,symbolSystem.scaleWins[2]);
            AreEqual(0.5,symbolSystem.scaleWins[3]);
            AreEqual(0.5,symbolSystem.scaleWins[9]);
            noOrders();
            noPositions();
            processBar(891.25,894,891,893.5,O.date("2009-06-23 10:00:00"));
            hasOrders(buy("Entry L", limit(894),35, oneDay()));            
            fill(0,893.5);
            hasOrders(sell("Stop L", protectiveStop(887.83), 35, oneDay()));
            processBar(889.75,890.75,888.25,887,O.date("2009-06-23 14:00:00"));    
            hasOrders(sell("Stop L", protectiveStop(887.83), 35, oneDay()));
            fill(0,887.83);
            noOrders();            
            AreEqual(-11375,symbolSystem.lastTradePNL);
            noPositions();
            processBar(889.5,890.25,889.25,890,O.date("2009-06-23 16:00:00"));
            AreEqual(0,symbolSystem.scaleWins[0]);
            AreEqual(1,symbolSystem.scaleWins[1]);
            AreEqual(0,symbolSystem.scaleWins[2]);
            AreEqual(1,symbolSystem.scaleWins[3]);
            AreEqual(0.5,symbolSystem.scaleWins[4]);
            AreEqual(0.5,symbolSystem.scaleWins[9]);
            noOrders();
            noPositions();
            processBar(893.5,893.75,892,892.25,O.date("2009-06-24 09:00:00"));   
            processBar(891.25,894,891,897.5,O.date("2009-06-24 10:00:00"));
            noOrders();
            noPositions();
            processBar(889.75,890.75,888.25,887,O.date("2009-06-24 14:00:00"));    
            noOrders();
            noPositions();
            processBar(889.5,890.25,889.25,890,O.date("2009-06-24 16:00:00"));
            AreEqual(0,symbolSystem.scaleWins[0]);
            AreEqual(1,symbolSystem.scaleWins[1]);
            AreEqual(0,symbolSystem.scaleWins[2]);
            AreEqual(1,symbolSystem.scaleWins[3]);
            AreEqual(0.5,symbolSystem.scaleWins[4]);
            AreEqual(0.5,symbolSystem.scaleWins[9]);
            noOrders();
            noPositions();
        }

        [Test]
        public void testScaleMap() {          
            // Switch 0
            AreEqual(1,symbolSystem.scaleMap(new[] {1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00},0));
            AreEqual(1,symbolSystem.scaleMap(new[] {0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00},0));
            AreEqual(1,symbolSystem.scaleMap(new[] {0.00,0.00,1,0.00,0.00,1,0.00,0.00,0.00,0.00},0));
            // Switch 1
            AreEqual(1,symbolSystem.scaleMap(new[] {1.00,1,1,1,1,1,1,1,1,1},1));
            AreEqual(0,symbolSystem.scaleMap(new[] {0.00,0,0,0,0,0,0,0,0,0},1));
            AlmostEqual(0.4,symbolSystem.scaleMap(new[] {1,1,1,1,0.00,0,0,0,0,0},1),0.0001);
            AlmostEqual(0.2,symbolSystem.scaleMap(new[] {0.00,0,0,1,1,1,1,0,0,0},1),0.0001);
            AlmostEqual(0,symbolSystem.scaleMap(new[] {0.00,0,0,0,0,0,1,1,1,1},1),0.0001);
            AlmostEqual(0.4,symbolSystem.scaleMap(new[] {0.00,1,0,1,0,1,0,1,0,1},1),0.0001);
            // Switch 2
            AreEqual(1,symbolSystem.scaleMap(new[] {1,1,1,1,1,1,1,1,1,1.00},2));
            AreEqual(0,symbolSystem.scaleMap(new[] {0,0,0,0,0,0,0,0,0.00,0},2));
            AlmostEqual(0.5,symbolSystem.scaleMap(new[] {1,1,1,1,0,0,0,0.00,0,0},2),0.0001);
            AlmostEqual(0.4,symbolSystem.scaleMap(new[] {0,0,0,1,1,1,1,0,0.00,0},2),0.0001);
            AlmostEqual(0.5,symbolSystem.scaleMap(new[] {0,1,0,1,0,1,0,1,0.00,1},2),0.0001);
        }       
    }

    [TestFixture]
    public class TestTrendIntradaySpace2 : TestITrend {
        [Test]
        public void testCase2() {         
            processBar(908,909,907.5,908.25,O.date("2009-06-17 09:00:00"));            
            AreEqual(53,symbolSystem.tradeSize());
            processBar(904.25,905.5,902.75,903.25,O.date("2009-06-17 10:00:00"));            
            AreEqual(40,symbolSystem.tradeSize());
            processBar(913,913.5,911.75,912.25,O.date("2009-06-17 14:00:00"));            
            AreEqual(26,symbolSystem.tradeSize());
            processBar(905.5,905.75,905,905,O.date("2009-06-17 16:00:00"));            
            AreEqual(0.5,symbolSystem.scaleWins[0]);
            processBar(908.5,909,907.5,908.5,O.date("2009-06-18 09:00:00"));    
            processBar(907.25,913.5,906.25,913,O.date("2009-06-18 10:00:00"));
            hasOrders(buy("Entry L", limit(913.5), 19, oneDay()));            
            fill(0,913);
            hasOrders(sell("Stop L", protectiveStop(908.87), 19, oneDay()));
            processBar(916.25,916.75,914.75,915,O.date("2009-06-18 14:00:00"));    
            hasOrders(sell("Stop L", protectiveStop(908.87), 19, oneDay()));
            AreEqual(1900,symbolSystem.lastTradePNL);
            processBar(912.25,913.75,911.5,913.75,O.date("2009-06-18 16:00:00"));     
            hasOrders(
                sell("Stop L", protectiveStop(908.87), 19, oneDay()),
                sell("Exit EOD L", limit(908.75), 19, oneDay())
            );            
            fill(0,913.75);
            AreEqual(712.5,symbolSystem.lastTradePNL);                                                
            processBar(921.25,921.75,920.5,921,O.date("2009-06-19 09:00:00"));                
            AreEqual(1,symbolSystem.scaleWins[0]);
            AreEqual(0.5,symbolSystem.scaleWins[1]);
            processBar(921.5,922.5,920.75,921.25,O.date("2009-06-19 10:00:00"));
            hasOrders(buy("Entry L", limit(921.75), 29, oneDay()));            
            fill(0,921.25);            
            processBar(915.5,916.5,914.5,917.5,O.date("2009-06-19 14:00:00"));                
            processBar(914.75,916,914.75,916,O.date("2009-06-19 16:00:00"));    
            hasOrders(
                sell("Stop L", protectiveStop(917.08),29, oneDay()),
                sell("Exit EOD L", limit(911), 29, oneDay())
            );
            fill(0,916);            
            processBar(906.25,906.75,906,906.25,O.date("2009-06-22 09:00:00"));    
            AreEqual(0,symbolSystem.scaleWins[0]);
            AreEqual(1,symbolSystem.scaleWins[1]);
            AreEqual(0.5,symbolSystem.scaleWins[2]);
            AreEqual(0.5,symbolSystem.scaleWins[9]);
            noOrders();
            noPositions();
        }     

        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {                               
                {"atrLength", 5},                
                {"nATRStop", 1},
                {"nATRTrigger", 1.6},
                {"sizeScaleSwitch", 1},
                {"risk", 10000},
                {"useDailyATR",0},
                {"timeStampClose",16},
                {"timeStampMark",10}
            });
        }
    }

    [TestFixture]
    public class TestTrendIntradayGapInTrades : TestITrend {
        [Test]
        public void testCase3() {         
            processBar(908,909,907.5,908.25,O.date("2009-06-17 09:00:00"));                        
            processBar(904.25,905.5,902.75,903.25,O.date("2009-06-17 10:00:00"));            
            processBar(913,913.5,911.75,912.25,O.date("2009-06-17 14:00:00"));            
            processBar(905.5,905.75,905,905,O.date("2009-06-17 16:00:00"));            
            processBar(908.5,909,907.5,908.5,O.date("2009-06-18 09:00:00"));    
            processBar(907.25,913.5,906.25,913,O.date("2009-06-18 10:00:00"));            
            fill(0,913);            
            processBar(916.25,916.75,914.75,915,O.date("2009-06-18 14:00:00"));    
            processBar(912.25,913.75,911.5,913.75,O.date("2009-06-18 16:00:00"));     
            fill(0,913.75);
            processBar(921.25,921.75,920.5,921,O.date("2009-06-19 09:00:00"));                
            processBar(921.5,922.5,920.75,921.25,O.date("2009-06-19 10:00:00"));
            fill(0,921.25);
            processBar(915.5,916.5,914.5,917.5,O.date("2009-06-19 14:00:00"));    
            processBar(914.75,916,914.75,916,O.date("2009-06-19 16:00:00"));    
            fill(0,916);
            processBar(906.25,906.75,906,906.25,O.date("2009-06-22 09:00:00"));    
            AreEqual(0,symbolSystem.scaleWins[0]);
            AreEqual(1,symbolSystem.scaleWins[1]);
            AreEqual(0.5,symbolSystem.scaleWins[2]);
            AreEqual(0.5,symbolSystem.scaleWins[9]);
            O.zeroTo(27, i => processBar(914.75,916,914.75,916,date("2009-06-22 09:00:00").AddDays(i+1)));
            AreEqual(0,symbolSystem.scaleWins[0]);
            AreEqual(1,symbolSystem.scaleWins[1]);            
            processBar(906.25,906.75,906,906.25,date("2009-06-22 09:00:00").AddDays(29));
            AreEqual(0.5,symbolSystem.scaleWins[0]);
            AreEqual(0.5,symbolSystem.scaleWins[1]);            
            processBar(908,909,907.5,908.25,O.date("2009-07-21 09:00:00"));                        
            processBar(904.25,905.5,902.75,903.25,O.date("2009-07-21 10:00:00"));            
            processBar(913,913.5,911.75,912.25,O.date("2009-07-21 14:00:00"));            
            processBar(905.5,905.75,905,905,O.date("2009-07-21 16:00:00"));            
            processBar(908.5,909,907.5,908.5,O.date("2009-07-22 09:00:00"));    
            processBar(907.25,913.5,906.25,913,O.date("2009-07-22 10:00:00"));            
            hasOrders(buy("Entry L", limit(913.5), 43, oneDay()));            
            fill(0,913);            
            processBar(916.25,916.75,914.75,915,O.date("2009-07-22 14:00:00"));    
            processBar(912.25,913.75,911.5,913.75,O.date("2009-07-22 16:00:00"));     
            hasOrders(
                sell("Stop L", protectiveStop(908.35),43, oneDay()),
                sell("Exit EOD L", limit(908.75), 43, oneDay())
            );            
            fill(0,913.75);
            AreEqual(1612.5,symbolSystem.lastTradePNL);
            processBar(921.25,921.75,920.5,921,O.date("2009-06-23 09:00:00"));       
            AreEqual(1,symbolSystem.scaleWins[0]);
            AreEqual(0.5,symbolSystem.scaleWins[1]);
        }
    }
}