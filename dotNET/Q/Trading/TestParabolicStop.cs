using System;
using NUnit.Framework;
using Q.Systems;
using Q.Systems.Examples;

namespace Q.Trading {
    [TestFixture]
    public class TestParabolicStop : OneSymbolSystemTest<TestParabolicStop.StopSystem>{
        public class StopSystem : EmptySystem {
            public Direction testTradeDirection;
            public double initialStopPrice;
            public double afStep;
            public double afMax;
            public int lookbackBars;

            public StopSystem(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {
                bars.low.prepare();
                bars.high.prepare();
            }

            protected override void onNewBar() {
                if (hasPosition()) return;
                if(testTradeDirection==Direction.LONG)
                    placeOrder(symbol.buy("Enter Long",market(),100,oneBar()));
                else {
                    placeOrder(symbol.sell("Enter Short",market(),100,oneBar()));
                }
            }

            protected override void onFilled(Position position, Trade trade) {
                if(position.isEntry(trade))
                    addDynamicExit(new ParabolicStop(position,bars,initialStopPrice,afStep,afMax,lookbackBars,"Parabolic Stop"), false);
            }
        }

        [Test]
        public void testStopOutLong() {
            //Tell the system to make a long buy on first bar
            symbolSystem.testTradeDirection = Direction.LONG;
            symbolSystem.afStep = 0.02;
            symbolSystem.afMax = 0.20;
            symbolSystem.lookbackBars = 2;
            symbolSystem.initialStopPrice = 95;

            processBar(100,110,100,105,new DateTime(2000,01,01));
            hasOrders(buy("Enter Long", market(), 100, oneBar()));
            fill(0, 105);
            
            //This should then create the initial stop
            hasOrders(position().exitLong("Parabolic Stop", protectiveStop(95), oneBar()));

            //Then each bar will update the stop
            processBar(105,105,100,105,new DateTime(2000,01,02));
            hasOrders(position().exitLong("Parabolic Stop", protectiveStop(95.2), oneBar()));
            processBar(97,100,96,97,new DateTime(2000,01,03));
            hasOrders(position().exitLong("Parabolic Stop", protectiveStop(95.296), oneBar()));
            processBar(97,101,95,99,new DateTime(2000,01,04));
            //That should have filled the order....
            fill(0, 0);
            processBar(97,101,95,99,new DateTime(2000,01,05));
            processBar(97,101,95,99,new DateTime(2000,01,06));
        }

        [Test]
        public void testStopLongWithAfIncrease() {
            //Tell the system to make a long buy on first bar
            symbolSystem.testTradeDirection = Direction.LONG;
            symbolSystem.afStep = 0.02;
            symbolSystem.afMax = 0.20;
            symbolSystem.lookbackBars = 2;
            symbolSystem.initialStopPrice = 95;

            processBar(100,110,100,105);
            hasOrders(buy("Enter Long", market(), 100, oneBar()));
            fill(0, 105);
            
            //This should then create the initial stop
            hasOrders(position().exitLong("Parabolic Stop", protectiveStop(95), oneBar()));

            //Then each bar will update the stop
            processBar(105,105,100,105);
            hasOrders(position().exitLong("Parabolic Stop", protectiveStop(95.2), oneBar()));
            //Bars with a new higher high
            processBar(97,115,97,97);
            hasOrders(position().exitLong("Parabolic Stop", protectiveStop(95.992), oneBar()));
            processBar(100,120,100,99);
            //The stop is capped at 97 because of the low 2 bars ago
            hasOrders(position().exitLong("Parabolic Stop", protectiveStop(97.0), oneBar()));
            processBar(99,105,99,103);
            //The 97 cap is now > 2 bars back, so not relevent
            hasOrders(position().exitLong("Parabolic Stop", protectiveStop(97.48), oneBar()));
        }

        [Test]
        public void testStopShortWithAfIncrease() {
            //Tell the system to make a short sell on first bar
            symbolSystem.testTradeDirection = Direction.SHORT;
            symbolSystem.afStep = 0.02;
            symbolSystem.afMax = 0.20;
            symbolSystem.lookbackBars = 2;
            symbolSystem.initialStopPrice = 110;

            processBar(100,108,100,105);
            hasOrders(sell("Enter Short", market(), 100, oneBar()));
            fill(0, 105);
            
            //This should then create the initial stop
            hasOrders(position().exitShort("Parabolic Stop", protectiveStop(110), oneBar()));

            //Then each bar will update the stop
            processBar(105,105,100,100);
            hasOrders(position().exitShort("Parabolic Stop", protectiveStop(109.8), oneBar()));
            //Bars with a new lower low
            processBar(97,109,97,97);
            hasOrders(position().exitShort("Parabolic Stop", protectiveStop(109.288), oneBar()));
            processBar(100,109,95,99);
            //The stop is capped at 109 because of the low 2 bars ago
            hasOrders(position().exitShort("Parabolic Stop", protectiveStop(109), oneBar()));
            processBar(99,105,99,103);
            hasOrders(position().exitShort("Parabolic Stop", protectiveStop(109), oneBar()));
            processBar(98,100,97,99);
            hasOrders(position().exitShort("Parabolic Stop", protectiveStop(108.28), oneBar()));
        }

        [Test]
        public void testLookbackBars() {
            //Tell the system to make a long buy on first bar
            symbolSystem.testTradeDirection = Direction.LONG;
            symbolSystem.afStep = 0.02;
            symbolSystem.afMax = 0.20;
            symbolSystem.lookbackBars = 4;
            symbolSystem.initialStopPrice = 95;

            processBar(100,110,100,105);
            hasOrders(buy("Enter Long", market(), 100, oneBar()));
            fill(0, 105);
            
            //This should then create the initial stop
            hasOrders(position().exitLong("Parabolic Stop", protectiveStop(95), oneBar()));

            //Then each bar will update the stop
            processBar(105,105,100,105);
            hasOrders(position().exitLong("Parabolic Stop", protectiveStop(95.2), oneBar()));
            //Bars with a new higher high
            processBar(97,115,97,97);
            hasOrders(position().exitLong("Parabolic Stop", protectiveStop(95.992), oneBar()));
            processBar(100,120,100,99);
            //The stop is capped at 97 because of the low ** 4 ** bars ago
            hasOrders(position().exitLong("Parabolic Stop", protectiveStop(97.0), oneBar()));
            processBar(99,105,99,103);
            hasOrders(position().exitLong("Parabolic Stop", protectiveStop(97.0), oneBar()));
            processBar(103,125,100,105);
            hasOrders(position().exitLong("Parabolic Stop", protectiveStop(97.0), oneBar()));
            //Now out of the lookback
            processBar(106,120,105,105);
            hasOrders(position().exitLong("Parabolic Stop", protectiveStop(98.84), oneBar()));
        }


        protected override int leadBars() {
            return 0;
        }
    }
}