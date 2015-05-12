using System;
using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;
using O=Q.Util.Objects;

namespace Q.Systems {
    [TestFixture]
    public class TestCouponSwap : OneSymbolSystemTest<CouponSwap> {
        DateTime lastTime = O.now();
        Bar lastBar;

        [Test]
        public void testLongEntries() {
            
            //Simple entry
            close(1, 1.0/32, 2.0/32, 0);
            noOrders();

            close(2, 7.0/32, 2.5/32, 1);
            hasOrders(buy("CS Buy", market(), 25, oneBar()));

            //Test the entry throttle on EWMA longMA
            close(3, 3.125, 1, 1);
            close(4, 1, 1, 1);
            close(5, 0.25, 0.0625, 1);
            noOrders();
            close(5.5, 1, 1, 1);
            close(6, 1, 0.5, 1);
            hasOrders(buy("CS Buy", market(), 25, oneBar()));

            //Test the roll cut off
            close(6.5, 0, 0, 0);
            close(6.75, 1, 0.5, -1);
            noOrders();
            close(7, 0, 0, 1);
            close(8, 1, 0.5, 0);
            hasOrders(buy("CS Buy", market(), 25, oneBar()));
        }

        [Test]
        public void testShortEntries() {
            
            //Simple entry
            close(1, -1.0/32, -2.0/32, 0);
            noOrders();

            close(2, -7.0/32, -2.5/32, -1);
            hasOrders(sell("CS Sell", market(), 25, oneBar()));

            //Test the entry throttle on EWMA longMA
            close(3, -3.125, -1, -1);
            close(4, -1, -1, -1);
            close(5, -0.25, -0.0625, -1);
            noOrders();
            close(5.5, -1, -1, -1);
            close(6, -1, -0.5, -1);
            hasOrders(sell("CS Sell", market(), 25, oneBar()));

            //Test the roll cut off
            close(6.5, 0, 0, 0);
            close(6.75, -1, -0.5, 1);
            noOrders();
            close(7, 0, 0, -1);
            close(8, -1, -0.5, 0);
            hasOrders(sell("CS Sell", market(), 25, oneBar()));
        }
        
        [Test]
        public void testPyramid() {
            close(1, 0.3125, 0.0625, 1);
            hasOrders(buy("CS Buy", market(), 25, oneBar()));
            fill(0, 1);
            close(2, 0, 0, 0);
            close(3, 0.3125, 0.0625, 1);
            hasOrders(buy("CS Buy", market(), 25, oneBar()));
            fill(0, 2);
            HasCount(2, symbolSystem.positions());
            close(2, 0, 0, 0);
            close(3, 0.3125, 0.0625, 1);
            noOrders();
        }

        [Test]
        public void testObjectiveExitLong() {
            close(1, 0.3125, 0.0625, 1);
            hasOrders(buy("CS Buy", market(), 25, oneBar()));
            fill(0, 1);
            close(2, 1.2, 1.2, 1);
            hasOrders(position().exitLong("CS ExitL_0", market(), oneBar()));
            
            //Now add scaled up order
            close(2, 0, 0, 0);
            close(3, 0.3125, 0.0625, 1);
            hasOrders(buy("CS Buy", market(), 25, oneBar()));
            fill(0, 1);
            close(2, 1.2, 1.2, 1);
            //This should exit both orders
            hasOrders(positions()[0].exitLong("CS ExitL_0", market(), oneBar()),
                      positions()[1].exitLong("CS ExitL_1", market(), oneBar()));  
        }

        [Test]
        public void testObjectiveExitShort()
        {
            close(1, -0.3125, -0.0625, -1);
            hasOrders(sell("CS Sell", market(), 25, oneBar()));
            fill(0, 1);
            close(2, -1.2, -1.2, -1);
            hasOrders(position().exitShort("CS ExitS_0", market(), oneBar()));

            //Now add scaled up order
            close(2, 0, 0, 0);
            close(3, -0.3125, -0.0625, -1);
            hasOrders(sell("CS Sell", market(), 25, oneBar()));
            fill(0, 1);
            close(2, -1.2, -1.2, -1);
            //This should exit both orders
            hasOrders(positions()[0].exitShort("CS ExitS_0", market(), oneBar()),
                      positions()[1].exitShort("CS ExitS_1", market(), oneBar()));
        }

		[Test]
		public void testRollExitLong()
		{
            close(1, 0.3125, 0.0625, 1.0/32);
            hasOrders(buy("CS Buy", market(), 25, oneBar()));
            fill(0, 1);
            close(2, 0.3125, 0.0625, -1.0/32);
            noOrders();
            close(3, 0.3125, 0.0625, -5.0/32);
            hasOrders(position().exitLong("Roll ExitL_0", market(), oneBar()));
		}

		[Test]
		public void testRollExitShort()
		{
            close(1, -0.3125, -0.0625, -1.0/32);
            hasOrders(sell("CS Sell", market(), 25, oneBar()));
            fill(0, 1);
            close(2, -0.3125, -0.0625, 1.0/32);
            noOrders();
            close(3, -0.3125, -0.0625, 5.0/32);
            hasOrders(position().exitShort("Roll ExitS_0", market(), oneBar()));
		}
		
		[Test]
		public void testRollReEntryLong()
		{
			//Enter on Zscore
            close(1, 0.3125, 0.0625, 1.0/32);
            hasOrders(buy("CS Buy", market(), 25, oneBar()));
            fill(0, 1);
            //Stop out on Roll
            close(2, 0.3125, 0.0625, -5.0/32);
            hasOrders(position().exitLong("Roll ExitL_0", market(), oneBar()));
			fill(0, 1);
			//Re enter on roll coming back in line
			close(3, 0.3125, 0.0625, -0.4/32);
            hasOrders(buy("Roll XOver Buy", market(), 25, oneBar()));
		}

		[Test]
		public void testRollReEntryShort()
		{
			//Enter on Zscore
            close(1, -0.3125, -0.0625, -1.0/32);
            hasOrders(sell("CS Sell", market(), 25, oneBar()));
            fill(0, 1);
            //Stop out on Roll
            close(2, -0.3125, -0.0625, 5.0/32);
            hasOrders(position().exitShort("Roll ExitS_0", market(), oneBar()));
			fill(0, 1);
			//Re enter on roll coming back in line
			close(3, -0.3125, -0.0625, 0.4/32);
            hasOrders(sell("Roll XOver Sell", market(), 25, oneBar()));		
		}

		[Test]
		public void testStopLossLong()
		{
			//Enter on Zscore
            close(1, 0.3125, 0.0625, 1);
            hasOrders(buy("CS Buy", market(), 25, oneBar()));
            fill(0, 1);
            //Scale up, and stop out on the higher price
		    close(10, 0, 0.0625, 1);
            close(10, 0.3125, 0.0625, 1);
            hasOrders(buy("CS Buy", market(), 25, oneBar()));
            fill(0, 10);
            //Stop out on money management stop loss
            close(5, 0.3125, 0.0625, 1);
            hasOrders(positions()[0].exitLong("Stop Loss ExitL_0", market(), oneBar()),
                      positions()[1].exitLong("Stop Loss ExitL_1", market(), oneBar()));
		}

		[Test]
		public void testStopLossShort()
		{
			//Enter on Zscore
            close(10, -0.3125, -0.0625, -1);
            hasOrders(sell("CS Sell", market(), 25, oneBar()));
            fill(0, 10);
            //Scale up, and stop out on the higher price
            close(1, 0, 0.0625, -1);
            close(1, -0.3125, -0.0625, -1);
            hasOrders(sell("CS Sell", market(), 25, oneBar()));
            fill(0, 1);
            //Stop out on money management stop loss
            close(6, -0.3125, -0.0625, -1);
            hasOrders(positions()[0].exitShort("Stop Loss ExitS_0", market(), oneBar()),
                      positions()[1].exitShort("Stop Loss ExitS_1", market(), oneBar()));
        }
        
        [Test]
        public void testOnlyOneStopOutLong()
        {
            //Enter on Zscore
            close(1, 0.3125, 0.0625, 1);
            hasOrders(buy("CS Buy", market(), 25, oneBar()));
            fill(0, 1);
            //Scale up, and stop out on the higher price
            close(10, 0, 0.0625, 1);
            close(10, 0.3125, 0.0625, 1);
            hasOrders(buy("CS Buy", market(), 25, oneBar()));
            fill(0, 10);
            //Stop out on roll, and money management stop loss not active its redundent
            close(5, 0.3125, 0.0625, -1);
            hasOrders(positions()[0].exitLong("Roll ExitL_0", market(), oneBar()),
                      positions()[1].exitLong("Roll ExitL_1", market(), oneBar()));
        }
        
        [Test]
        public void testOnlyOneStopOutShort()
        {
            //Enter on Zscore
            close(10, -0.3125, -0.0625, -1);
            hasOrders(sell("CS Sell", market(), 25, oneBar()));
            fill(0, 10);
            //Scale up, and stop out on the higher price
            close(1, 0, 0.0625, -1);
            close(1, -0.3125, -0.0625, -1);
            hasOrders(sell("CS Sell", market(), 25, oneBar()));
            fill(0, 1);
            //Stop out on money management stop loss
            close(6, -0.3125, -0.0625, 1);
            hasOrders(positions()[0].exitShort("Roll ExitS_0", market(), oneBar()),
                      positions()[1].exitShort("Roll ExitS_1", market(), oneBar()));
        }
		        
        public override void setUp() {
            base.setUp();
            symbolSystem.modelPrice.enterTestMode();
            symbolSystem.actualPrice.enterTestMode();
            symbolSystem.rollDecimal.enterTestMode();
            lastBar = null;
            O.zeroTo(leadBars(), i => close(1, 0, 0, 0));
        }

        protected override Symbol initializeSymbol() {
            var emptyTestSeries = createEmptyTestSeries();
            insertSymbol("F1510MDL", emptyTestSeries);
            insertSymbol("F1510ACT", emptyTestSeries);
            insertSymbol("F1510WRL", emptyTestSeries);
            return new Symbol("FNCL.1.5.1.0.TEST", 1000);
        }


        void close(double tri, double modelPrice, double actualPrice, double rollDecimal){
            if(lastBar != null)
                processBar(lastBar);
            lastTime = lastTime.AddDays(1);
            symbolSystem.modelPrice.add(lastTime, modelPrice);
            symbolSystem.actualPrice.add(lastTime, actualPrice);
            symbolSystem.rollDecimal.add(lastTime, rollDecimal);
            lastBar = new Bar(tri, tri, tri, tri, lastTime);
            processClose(lastBar);
        }

        protected override int leadBars() {
            return 3;
        }

        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                {"EntryTicks", 3.5},
                {"ExitTicks", 1},
                {"RollCutOff", 2.5},
                {"RollCutOff.Margin", 2},
                {"HalfLife", 5},
                {"TradeSize", 25},
                {"MaxPyramid", 2},
                {"StopTicks", 128}
            });
        }
    }
}
