using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;

namespace Q.Systems {    
    [TestFixture]
    public class TestSwingMA: OneSymbolSystemTest<SwingMA> {
        public override void setUp() {
            base.setUp();
            //Lead bars
            processBar(100,105,100,105);
            processBar(105,105,95,95);
            processBar(95,98,95,98);
            processBar(98,100,98,100);
            processBar(100,100,99,99);
            noOrders();
        }
        
        protected override int leadBars() {
            return 5;
        }

        protected override Parameters parameters() {
            return base.parameters().overwrite(
                new Dictionary<string, double> {
                    {"MAType", 1}, 
                    {"MASlow", 5},
                    {"MAFast", 2},
                    {"ATRLen", 3},
                    {"EntryATRMargin", 0.25},
                    {"StopATRs", 5},
                    {"RiskDollars", 1000000},
                });
        }

        [Test]
        public void LongEntryAndExits() {
            processBar(99,99,99,99);
            noOrders();
            processBar(99,120,99,120);
            noOrders();
            processBar(120,130,120,130);
            noOrders();
            processBar(130,130,112,112);
            hasOrders(buy("Swing Enter L",protectiveStop(112 + 0.25*symbolSystem.atr),21,oneBar()));
            fill(0,114.41);
            hasOrders(position().exitLong("Money Stop L", stop(66.19341321848670), oneBar()));
            processBar(112,135,112,135);
            hasOrders(  position().exitLong("Money Stop L", stop(66.19341321848670), oneBar()),
                        position().exitLong("Price Exit L", protectiveStop(128.80060301985300),oneBar()));  
            processBar(135,135,130,130);
            hasOrders(position().exitLong("Money Stop L", stop(66.19341321848670), oneBar()));
            processBar(130,130,125,125);
            hasOrders(  position().exitLong("Money Stop L", stop(66.19341321848670), oneBar()),
                        position().exitLong("Price Exit L", protectiveStop(120.16952593639700),oneBar()));  

        }

        [Test]
        public void ShortEntryAndExits() {
            processBar(99,99,97,97);
            noOrders();
            processBar(97,97.5,97,97.5);
            hasOrders(sell("Swing Enter S", protectiveStop(96.77192440891580), 69, oneBar()));
            fill(0,96.77);
            hasOrders(position().exitShort("Money Stop S", stop(111.33151182168400), oneBar()));
            processBar(97.5,97.5,97,97);
            hasOrders(  position().exitShort("Money Stop S", stop(111.33151182168400), oneBar()),
                        position().exitShort("Price Exit S", protectiveStop(98.20732282770340),oneBar()));
            processBar(97,98,97,98);
            hasOrders(  position().exitShort("Money Stop S", stop(111.33151182168400), oneBar()),
                        position().exitShort("Price Exit S", protectiveStop(99.06140250038880),oneBar()));
        }
    }
}
