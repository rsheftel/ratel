using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;

namespace Q.Systems {    
    public abstract class TestNBarFade: OneSymbolSystemTest<NBarFade> {
        public void startupBarsLong() {
            //Lead bars
            processBar(100,110,100,105);
            processBar(105,105,90,95);
            processBar(95,103,95,98);
            processBar(98,110,95,100);
            processBar(100,100,98,99);
            processBar(99,105,100,99);

            //Live Bars
            processBar(99,100,95,95);
            noOrders();
            processBar(95,100,92,100);
            AreEqual(null, symbolSystem.tradeSetup());
        }

        public void startupBarsShort() {
            //Lead bars
            processBar(100, 100, 90, 105);
            processBar(105, 110, 95, 95);
            processBar(95, 105, 97, 98);
            processBar(98, 105, 90, 100);
            processBar(100, 102, 100, 99);
            processBar(99, 100, 95, 99);

            //Live Bars
            processBar(99, 105, 100, 95);
            noOrders();
            processBar(95, 108, 100, 100);
            AreEqual(null, symbolSystem.tradeSetup());
        }
    }
    
    [TestFixture] 
    public class TestNBarFadeBasic : TestNBarFade {
        

        [Test] 
        public void simpleSetupLong() {
            startupBarsLong();

            processBar(95,100,91,100);  //this one triggers because the window has rolled
            AreEqual(Direction.LONG, symbolSystem.tradeSetup());
            hasOrders(buy("Enter Long", protectiveStop(92 + symbolSystem.atr*1.5), 77, oneBar()));

            processBar(99,99,90,95);  //a new low, but the old trade is still out there
            AreEqual(Direction.LONG, symbolSystem.tradeSetup());
            hasOrders(buy("Enter Long", protectiveStop(92 + symbolSystem.atr[1]*1.5), 77, oneBar()));

            processBar(95,100,95,100); //last bar with the outstanding order, not filled, now pulled
            noOrders();
        }

        [Test] 
        public void simpleSetupShort() {
            startupBarsShort();

            processBar(100,109,100,100);  //this one triggers because the window has rolled
            AreEqual(Direction.SHORT, symbolSystem.tradeSetup());
            hasOrders(sell("Enter Short", protectiveStop(108 - symbolSystem.atr*1.5), 71, oneBar()));

            processBar(95,110,95,95);  //a new high, but the old trade is still out there
            AreEqual(Direction.SHORT, symbolSystem.tradeSetup());
            hasOrders(sell("Enter Short", protectiveStop(108 - symbolSystem.atr[1]*1.5), 71, oneBar()));

            processBar(100,105,100,100); //last bar with the outstanding order, not filled, now pulled
            noOrders();
        }

        [Test]
        public void longEntryInitiateStop() {
            startupBarsLong();

            processBar(95,100,91,100);  //this one triggers because the window has rolled
            AreEqual(Direction.LONG, symbolSystem.tradeSetup());
            hasOrders(buy("Enter Long", protectiveStop(92 + symbolSystem.atr*1.5), 77, oneBar()));
    
            processBar(99,106,90,95); // This bars fills it, only parabolic put out there
            fill(0, 92 + symbolSystem.atr[1]*1.5);
            hasOrders(position().exitLong("Parabolic Stop", protectiveStop(92), oneBar()));

            processBar(95,100,95,100); //Next bar updates the parabolic, and puts out objective
            AreEqual(null, symbolSystem.tradeSetup());
            hasOrders(  position().exitLong("Parabolic Stop", protectiveStop(92.16), oneBar()),
                        position().exitLong("Objective Exit L", limit(106),oneBar()));
        }
        
        [Test]
        public void shortEntryInitiateStop() {
            startupBarsShort();

            processBar(100,110,90,95);  //this one triggers because the window has rolled
            AreEqual(Direction.SHORT, symbolSystem.tradeSetup());
            hasOrders(sell("Enter Short", protectiveStop(108 - symbolSystem.atr*1.5), 62, oneBar()));
    
            processBar(100,110,90,95); // This bars fills it, only parabolic put out there
            fill(0,108 - symbolSystem.atr[1]*1.5);
            hasOrders(position().exitShort("Parabolic Stop", protectiveStop(108), oneBar()));

            processBar(95,105,92,105); //Next bar updates the parabolic, and puts out objective
            AreEqual(null, symbolSystem.tradeSetup());
            hasOrders(  position().exitShort("Parabolic Stop", protectiveStop(107.68), oneBar()),
                        position().exitShort("Objective Exit S", limit(90),oneBar()));
        }
          
        [Test]
        public void notClosedAboveNoEntryLong() {
            startupBarsLong();
            processBar(95,100,91,91.5);   //Close is not above the 92 level, so no setup fails
            AreEqual(null, symbolSystem.tradeSetup());
            noOrders();

            processBar(91,92,90,91.5);  //Now the close is above the lowest low
            AreEqual(Direction.LONG, symbolSystem.tradeSetup());
            hasOrders(buy("Enter Long", protectiveStop(91 + symbolSystem.atr*1.5), 85, oneBar()));
        }

        [Test]
        public void notClosedAboveNoEntryShort() {
            startupBarsShort();
            processBar(95,109,100,108);   //Close is not below the 108 level, so no setup fails
            AreEqual(null, symbolSystem.tradeSetup());
            noOrders();

            processBar(109,110,105,108);  //Now the close is below the highest high
            AreEqual(Direction.SHORT, symbolSystem.tradeSetup());
            hasOrders(sell("Enter Short", protectiveStop(109 - symbolSystem.atr*1.5), 76, oneBar()));
        }

        [Test]
        public void skinnyEntryLong() {
            //Lead bars
            processBar(100,101,100,101);
            processBar(100,101,100,101);
            processBar(100,101,100,101);
            processBar(100,101,100,101);
            processBar(100,101,100,101);
            processBar(100,101,100,101);
  
            //Live Bars
            processBar(100,101,99,101);
            AreEqual(Direction.LONG, symbolSystem.tradeSetup());
            hasOrders(buy("Enter Long", protectiveStop(100 + symbolSystem.atr*1.5), 590, oneBar()));
            //This bar triggers so fill it, then process the onNewBar
            fill(0,100 + symbolSystem.atr*1.5);
            //Only the stop parabolic is put out for this bar
            hasOrders(position().exitLong("Parabolic Stop", protectiveStop(100), oneBar()));
            processBar(101,102,101,102);
            //Now that the onNewBar has been called the stop and objective are out 
            hasOrders(  position().exitLong("Parabolic Stop", protectiveStop(100.08), oneBar()),
                        position().exitLong("Objective Exit L", limit(102),oneBar()));
        }
        [Test]
        public void skinnyEntryShort() {
            //Lead bars
            processBar(100,101,100,101);
            processBar(100,101,100,101);
            processBar(100,101,100,101);
            processBar(100,101,100,101);
            processBar(100,101,100,101);
            processBar(100,101,100,101);
  
            //Live Bars
            processBar(100,102,100,100);
            AreEqual(Direction.SHORT, symbolSystem.tradeSetup());
            hasOrders(sell("Enter Short", protectiveStop(101 - symbolSystem.atr*1.5), 590, oneBar()));
            //This bar triggers so fill it, then process the onNewBar
            fill(0,101 - symbolSystem.atr*1.5);
            //Only the stop parabolic is put out for this bar
            hasOrders(position().exitShort("Parabolic Stop", protectiveStop(101), oneBar()));
            processBar(100,100.5,99,99);
            //Now that the onNewBar has been called the stop and objective are out 
            hasOrders(  position().exitShort("Parabolic Stop", protectiveStop(100.92), oneBar()),
                        position().exitShort("Objective Exit S", limit(99),oneBar()));
        }

        [Test]
        public void objectiveExitLong() {
            processBar(100, 110, 100, 105);
            processBar(105, 109, 90, 95);
            processBar(95, 108, 95, 98);
            processBar(98, 107, 95, 100);
            processBar(100, 106, 98, 99);
            processBar(99, 105, 100, 99);
            processBar(99, 104, 90, 95);
            processBar(95, 103, 88, 100);

            hasOrders(buy("Enter Long", protectiveStop(90 + symbolSystem.atr * 1.5), 59, oneBar()));
            fill(0,90 + symbolSystem.atr * 1.5);
            //Only the stop parabolic is put out for this bar
            hasOrders(position().exitLong("Parabolic Stop", protectiveStop(90), oneBar()));
            processBar(100,102,100,102);
            //Now that the onNewBar has been called the stop and objective are out 
            hasOrders(  position().exitLong("Parabolic Stop", protectiveStop(90.24), oneBar()),
                        position().exitLong("Objective Exit L", limit(104),oneBar()));

            processBar(100,102,100,102);
            hasOrders(  position().exitLong("Parabolic Stop", protectiveStop(90.4752), oneBar()),
                        position().exitLong("Objective Exit L", limit(103),oneBar()));
            processBar(100,105,100,102);
            hasOrders(  position().exitLong("Parabolic Stop", protectiveStop(91.056192), oneBar()),
                        position().exitLong("Objective Exit L", limit(105),oneBar()));
        }

        [Test]
        public void objectiveExitShort() {
            processBar(100, 100, 91, 100);
            processBar(100, 110, 92, 95);
            processBar(95, 105, 92, 98);
            processBar(98, 105, 93, 100);
            processBar(100, 102, 94, 99);
            processBar(99, 100, 95, 99);
            processBar(99, 110, 96, 96);
            processBar(97, 112, 97, 100);

            hasOrders(sell("Enter Short", protectiveStop(110 - symbolSystem.atr * 1.5), 61, oneBar()));
            fill(0,110 - symbolSystem.atr * 1.5);
            //Only the stop parabolic is put out for this bar
            hasOrders(position().exitShort("Parabolic Stop", protectiveStop(110), oneBar()));
            processBar(100,100,98,102);
            //Now that the onNewBar has been called the stop and objective are out 
            hasOrders(  position().exitShort("Parabolic Stop", protectiveStop(109.76), oneBar()),
                        position().exitShort("Objective Exit S", limit(96),oneBar()));

            processBar(100,100,98,102);
            hasOrders(  position().exitShort("Parabolic Stop", protectiveStop(109.5248), oneBar()),
                        position().exitShort("Objective Exit S", limit(97),oneBar()));
            processBar(100,100,95,102);
            hasOrders(  position().exitShort("Parabolic Stop", protectiveStop(108.943808), oneBar()),
                        position().exitShort("Objective Exit S", limit(95),oneBar()));
        }

        protected override int leadBars() {
            return 6;
        }

        protected override Parameters parameters() {
            return base.parameters().overwrite(
                new Dictionary<string, double> {
                    {"ATRLen", 5},
                    {"nDays", 6},
                    {"nATRentry", 1.5},
                    {"exitATRmultiple", 1},
                    {"stopAfStep", 0.02},
                    {"stopAfMax", 0.2},
                    {"entryBarWindow", 2},
                    {"closeBetter", 1},
                    {"riskDollars",1000000},
                });
        }
    }
        
    [TestFixture] 
    public class TestNBarFadeDiffParams : TestNBarFade {
       //Above but remove the close above requirement
        //exitATR multiple

        [Test]
        public void notClosedAboveYesEntryLong() {
            startupBarsLong();
            processBar(95,100,91,91.5);   //Close is not above the 92 level, this time setup good
            AreEqual(Direction.LONG, symbolSystem.tradeSetup());
            hasOrders(buy("Enter Long", protectiveStop(92 + symbolSystem.atr*1.5), 38, oneBar()));
        }

        [Test]
        public void notClosedAboveYesEntryShort() {
            startupBarsShort();
            processBar(100,120,100,120);   //Close is not below the 108 level, this time ok
            AreEqual(Direction.SHORT, symbolSystem.tradeSetup());
            hasOrders(sell("Enter Short", protectiveStop(108 - symbolSystem.atr*1.5), 31, oneBar()));
        }
        
        protected override int leadBars() {
            return 6;
        }

        protected override Parameters parameters() {
            return base.parameters().overwrite(
                new Dictionary<string, double> {
                    {"ATRLen", 5},
                    {"nDays", 6},
                    {"nATRentry", 1.5},
                    {"exitATRmultiple", 2},     //This is different
                    {"stopAfStep", 0.02},
                    {"stopAfMax", 0.2},
                    {"entryBarWindow", 2},
                    {"closeBetter", 0},         //This is different
                    {"riskDollars",1000000},
                });
        }
    }
}
