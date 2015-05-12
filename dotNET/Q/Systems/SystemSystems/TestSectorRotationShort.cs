using System;
using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;
using Q.Trading.Results;
using systemdb.metadata;
using Test=NUnit.Framework.TestAttribute;
using tsdb;
using util;
using O=Q.Util.Objects;

namespace Q.Systems.SystemSystems {    
    [TestFixture]
    public class TestSectorRotationShort : OneSystemTest< SectorRotationShort> {
        internal List<Symbol> symbols;
        DateTime time = O.date("2006-07-26");
        Dictionary<Symbol, Bar> lastBars;
        
        protected override void initializeSymbols() {
            var names = new List<string>();
            O.zeroTo(5, i => names.Add("sym" + (i + 1)));
            symbols = O.list(O.convert(names, name => new Symbol(name)));
            var series = createEmptyTestSeries();
            O.each(symbols, symbol => setupSymbol(symbol, series));            
        }

        static void setupSymbol(Collectible symbol, TimeSeries series) {            
            insertSymbol(symbol.name, series);
            MarketTable.MARKET.insert(symbol.name, new java.lang.Double(0.0));
        }

        protected override int leadBars() {
            return 23;
        }

        protected override SystemArguments arguments() {
            return new SystemArguments(symbols, parameters());
        }        

        public void addThreeDays() {
            close(47, 80,30, 70, 16);
            close(48, 79, 31, 71, 14);
            close(49, 82, 26, 75, 13);
        }

        internal void close(double tri0, double tri1, double tri2, double tri3, double tri4) {
            if(lastBars != null)
                processBar(lastBars);
            var tris = O.array(tri0, tri1, tri2, tri3, tri4);
            lastBars = new Dictionary<Symbol, Bar>();
            O.zeroTo(5, i => lastBars.Add(symbols[i], bar(tris[i])));
            processClose(lastBars);              
            var jTime = O.jDate(time);
            time = O.date(Dates.businessDaysAhead(1, jTime, "nyb"));
        }

        Bar bar(double tri) {
            return new Bar(tri,tri,tri,tri,time);
        }

        public void testNullBarDate() {
             addThreeDays();          
            O.zeroTo(7, i => addThreeDays());            
            close(47, 80,30, 70, 16);            
            AreEqual(null,system().barDate);         
        }
        
        public void addFirstMonth() {
            addThreeDays();          
            O.zeroTo(7, i => addThreeDays());            
            close(47, 80,30, 70, 16);            
            AreEqual(O.jDate("2006-08-29"),system().barDate);
            AreEqual(O.jDate("2006-07-31"),system().refDate);
            AreEqual(O.jDate("2006-08-31"),system().evaluationDate);                       
            close(48, 79, 31, 71, 14);
            AreEqual(O.jDate("2006-08-30"),system().barDate);
            AreEqual(O.jDate("2006-07-31"),system().refDate);
            AreEqual(O.jDate("2006-08-31"),system().evaluationDate);                        
            AreEqual(5,system().basketSize);
        }

    
    }
    [TestFixture]
    public class TestSectorRotationShortFirstSpace : TestSectorRotationShort {
         protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                    {"Risk", 100000},
                    {"DaysBuffer", 0},
                    {"NBest", 2},    
                    {"MinBasketSize", 5},    
                    {"MaxNBestBasketSizeRatio",1},                    
                    {"Cumulative", 1},    
                    {"LeadBars", 23},    
                    {"DaysInTrade", 15}
                });
        }

        [Test]
        public void testSystem() {            
            addFirstMonth();                                     
            noOrders();
            close(49,82, 26,75,13); // Close of 8/31/2006
            hasOrders(symbols[0], symbols[0].buy("enter long", market(),408,oneBar()));
            hasOrders(symbols[1], symbols[1].buy("enter long", market(),244,oneBar()));
            hasOrders(symbols[2], symbols[2].sell("enter short", market(),1154,oneBar()));
            hasOrders(symbols[3], symbols[3].buy("enter long", market(),267,oneBar()));
            hasOrders(symbols[4], symbols[4].sell("enter short", market(),2308,oneBar()));
            AreEqual(O.jDate("2006-08-31"),system().barDate);
            AreEqual(O.jDate("2006-07-31"),system().refDate);
            AreEqual(O.jDate("2006-08-31"),system().evaluationDate);
            AreEqual(O.jDate("2006-08-31"),system().rebalancingDate);
            AreEqual(O.jDate("2006-09-22"), system().tradeExitDate);
            O.zeroTo(5, i => fill(symbols[i], 0, 1));
            close(47, 80,30, 70, 16); // Close of 9/01/2006
            AreEqual(O.jDate("2006-09-01"),system().barDate);
            AreEqual(O.jDate("2006-07-31"),system().refDate);
            AreEqual(O.jDate("2006-08-31"),system().evaluationDate);
            AreEqual(O.jDate("2006-08-31"),system().rebalancingDate);
            AreEqual(O.jDate("2006-09-22"), system().tradeExitDate);
            hasPosition(symbols[0],408);
            hasPosition(symbols[1],244);
            hasPosition(symbols[2],-1154);
            hasPosition(symbols[3],267);
            hasPosition(symbols[4],-2308);
            close(48, 79, 31, 71, 14);
            close(49,82, 26,75,13);
            addThreeDays();
            addThreeDays();
            addThreeDays();
            close(47, 80,30, 70, 16);
            close(48, 79, 31, 71, 14);
            hasPosition(symbols[0],408);
            hasPosition(symbols[1],244);
            hasPosition(symbols[2],-1154);
            hasPosition(symbols[3],267);
            hasPosition(symbols[4],-2308);
            AreEqual(O.jDate("2006-09-21"),system().barDate);
            close(49,82, 26,75,13);                     
            AreEqual(O.jDate("2006-09-22"),system().barDate);
            hasOrders(symbols[0], symbols[0].sell("exit on days in trade", market(),408,oneBar()));           
            hasOrders(symbols[1], symbols[1].sell("exit on days in trade", market(),244,oneBar()));
            hasOrders(symbols[2], symbols[2].buy("exit on days in trade", market(),1154,oneBar()));
            hasOrders(symbols[3], symbols[3].sell("exit on days in trade", market(),267,oneBar()));
            hasOrders(symbols[4], symbols[4].buy("exit on days in trade", market(),2308,oneBar()));       
            O.zeroTo(5, i => fill(symbols[i], 0, 1));
            close(47, 80,30, 70, 16);
            AreEqual(O.jDate("2006-09-25"),system().barDate);
            AreEqual(O.jDate("2006-08-31"),system().refDate);
            AreEqual(O.jDate("2006-09-29"),system().evaluationDate);
            AreEqual(O.jDate("2006-09-29"),system().rebalancingDate);
            //AreEqual(O.jDate("2006-10-23"), system().tradeExitDate);
            noOrders();
            close(48, 79, 31, 71, 14);
            close(49,82, 26,75,13);
            close(47, 80,30, 70, 16);
            close(48, 79, 31, 71, 14);
            hasOrders(symbols[0], symbols[0].buy("enter long", market(),417,oneBar()));
            hasOrders(symbols[1], symbols[1].sell("enter short", market(),380,oneBar()));
            hasOrders(symbols[2], symbols[2].buy("enter long", market(),645,oneBar()));
            hasOrders(symbols[3], symbols[3].sell("enter short", market(),423,oneBar()));
            hasOrders(symbols[4], symbols[4].buy("enter long", market(),1429,oneBar()));
        }
    }

    [TestFixture]
    public class TestSectorRotationShortSecondSpace : TestSectorRotationShort {
         protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                    {"Risk", 100000},
                    {"DaysBuffer", 2},
                    {"NBest", 2},    
                    {"MinBasketSize", 5},    
                    {"MaxNBestBasketSizeRatio",1},                    
                    {"Cumulative", 1},    
                    {"LeadBars", 23},    
                    {"DaysInTrade", 5}
                });
        }

        [Test]
        public void testSystem() {            
            addFirstMonth();                                     
            noOrders();
            close(49,82, 26,75,13); // Close of 8/31/2006
            noOrders();
            close(47, 80,30, 70, 16);
            close(48, 79, 31, 71, 14); // Close of 9/05/2006
            hasOrders(symbols[0], symbols[0].buy("enter long", market(),417,oneBar()));
            hasOrders(symbols[1], symbols[1].buy("enter long", market(),253,oneBar()));
            hasOrders(symbols[2], symbols[2].sell("enter short", market(),968,oneBar()));
            hasOrders(symbols[3], symbols[3].buy("enter long", market(),282,oneBar()));
            hasOrders(symbols[4], symbols[4].sell("enter short", market(),2143,oneBar()));            
            O.zeroTo(5, i => fill(symbols[i], 0, 1));
            close(49,82, 26,75,13); // Close of 9/6/2006
            addThreeDays();
            hasPosition(symbols[0],417);
            close(47, 80,30, 70, 16); // Close of 9/12/2006
            hasOrders(symbols[0], symbols[0].sell("exit on days in trade", market(),417,oneBar()));           
            hasOrders(symbols[1], symbols[1].sell("exit on days in trade", market(),253,oneBar()));
            hasOrders(symbols[2], symbols[2].buy("exit on days in trade", market(),968,oneBar()));
            hasOrders(symbols[3], symbols[3].sell("exit on days in trade", market(),282,oneBar()));
            hasOrders(symbols[4], symbols[4].buy("exit on days in trade", market(),2143,oneBar()));  
            O.zeroTo(5, i => fill(symbols[i], 0, 1));
            close(48, 79, 31, 71, 14); 
            noOrders();
        }
    }
    [TestFixture]
    public class TestSectorRotationShortThirdSpace : TestSectorRotationShort {
         protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                    {"Risk", 100000},
                    {"DaysBuffer", 2},
                    {"NBest", 4},     
                    {"Cumulative", 1},    
                    {"MinBasketSize", 4},    
                    {"MaxNBestBasketSizeRatio",0.81},                    
                    {"LeadBars", 23},    
                    {"DaysInTrade", 5}
                });
        }

        [Test]
        public void testSystem() {            
            addFirstMonth();                                     
            noOrders();
            close(49,82, 26,75,13); // Close of 8/31/2006
            noOrders();
            close(47, 80,30, 70, 16);
            close(48, 79, 31, 71, 14); // Close of 9/05/2006
            hasOrders(symbols[0], symbols[0].sell("enter short", market(),104,oneBar()));
            hasOrders(symbols[1], symbols[1].sell("enter short", market(),63,oneBar()));
            hasOrders(symbols[2], symbols[2].sell("enter short", market(),161,oneBar()));
            hasOrders(symbols[3], symbols[3].buy("enter long", market(),282,oneBar()));
            hasOrders(symbols[4], symbols[4].sell("enter short", market(),357,oneBar()));            
            O.zeroTo(5, i => fill(symbols[i], 0, 1));
            close(49,82, 26,75,13); // Close of 9/6/2006
            addThreeDays();
            hasPosition(symbols[0],-104);
            close(47, 80,30, 70, 16); // Close of 9/12/2006
            hasOrders(symbols[0], symbols[0].buy("exit on days in trade", market(),104,oneBar()));           
            hasOrders(symbols[1], symbols[1].buy("exit on days in trade", market(),63,oneBar()));
            hasOrders(symbols[2], symbols[2].buy("exit on days in trade", market(),161,oneBar()));
            hasOrders(symbols[3], symbols[3].sell("exit on days in trade", market(),282,oneBar()));
            hasOrders(symbols[4], symbols[4].buy("exit on days in trade", market(),357,oneBar()));  
            O.zeroTo(5, i => fill(symbols[i], 0, 1));
            close(48, 79, 31, 71, 14); 
            noOrders();
        }
    }

    [TestFixture]
    public class TestSectorRotationShortNBestRatioLimit : TestSectorRotationShort {
         protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                    {"Risk", 100000},
                    {"DaysBuffer", 2},
                    {"NBest", 4},     
                    {"Cumulative", 1},    
                    {"MinBasketSize", 4},    
                    {"MaxNBestBasketSizeRatio",0.79},                    
                    {"LeadBars", 23},    
                    {"DaysInTrade", 5}
                });
        }

        [Test]
        public void testSystem() {
            testNullBarDate();
        }
    }

    [TestFixture]
    public class TestSectorRotationShortBasketSizeLimit : TestSectorRotationShort {
         protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                    {"Risk", 100000},
                    {"DaysBuffer", 2},
                    {"NBest", 4},     
                    {"Cumulative", 1},    
                    {"MinBasketSize", 6},    
                    {"MaxNBestBasketSizeRatio",1},                    
                    {"LeadBars", 23},    
                    {"DaysInTrade", 5}
                });
        }

        [Test]
        public void testSystem() {
            testNullBarDate();              
        }
    }

    [TestFixture]
    public class TestSectorRotationShortNonCumulative : TestSectorRotationShort {
         protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                    {"Risk", 100000},
                    {"DaysBuffer", 0},
                    {"NBest", 2},    
                    {"MinBasketSize", 5},    
                    {"MaxNBestBasketSizeRatio",1},                    
                    {"Cumulative", 0},    
                    {"LeadBars", 23},    
                    {"DaysInTrade", 15}
                });
        }

        [Test]
        public void testSystem() {            
            addFirstMonth();                                     
            noOrders();
            close(49,82, 26,75,13); // Close of 8/31/2006
            hasOrders(symbols[0], symbols[0].buy("enter long", market(),408,oneBar()));
            hasOrders(symbols[1], symbols[1].buy("enter long", market(),244,oneBar()));
            hasOrders(symbols[2], symbols[2].sell("enter short", market(),3077,oneBar()));
            hasOrders(symbols[3], symbols[3].buy("enter long", market(),267,oneBar()));
            hasOrders(symbols[4], symbols[4].buy("enter long", market(),1538,oneBar()));           
            O.zeroTo(5, i => fill(symbols[i], 0, 1));
            close(47, 80,30, 70, 16); // Close of 9/01/2006            
            close(48, 79, 31, 71, 14);
            close(49,82, 26,75,13);
            addThreeDays();
            addThreeDays();
            addThreeDays();
            close(47, 80,30, 70, 16);
            close(48, 79, 31, 71, 14);
            hasPosition(symbols[0],408);
            hasPosition(symbols[1],244);
            hasPosition(symbols[2],-3077);
            hasPosition(symbols[3],267);
            hasPosition(symbols[4],1538);            
            close(49,82, 26,75,13);                                 
            hasOrders(symbols[0], symbols[0].sell("exit on days in trade", market(),408,oneBar()));           
            hasOrders(symbols[1], symbols[1].sell("exit on days in trade", market(),244,oneBar()));
            hasOrders(symbols[2], symbols[2].buy("exit on days in trade", market(),3077,oneBar()));
            hasOrders(symbols[3], symbols[3].sell("exit on days in trade", market(),267,oneBar()));
            hasOrders(symbols[4], symbols[4].sell("exit on days in trade", market(),1538,oneBar()));       
            O.zeroTo(5, i => fill(symbols[i], 0, 1));            
        }        
    }

    [TestFixture]
    public class TestSectorRotationShortTestRun : TestSectorRotationShort {
         protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                    {"Risk", 100000},
                    {"DaysBuffer", 0},
                    {"NBest", 1},    
                    {"MinBasketSize", 5},    
                    {"MaxNBestBasketSizeRatio",0.5},                    
                    {"Cumulative", 0},    
                    {"LeadBars", 25},    
                    {"DaysInTrade", 1}
                });
        }

        [Test]
        public void testSystem() {            
            addThreeDays();          
            O.zeroTo(7, i => addThreeDays());            
            close(47, 80,30, 70, 16);                        
            close(48, 79, 31, 71, 14);            
            noOrders();
            close(49,82, 26,75,13); // Close of 8/31/2006           
            close(47, 80,30, 70, 16); // Close of 9/01/2006            
            close(48, 79, 31, 71, 14);
            close(49,82, 26,75,13);
            addThreeDays();
            addThreeDays();
            addThreeDays();
            close(47, 80,30, 70, 16);
            close(48, 79, 31, 71, 14);            
            close(49,82, 26,75,13);   
            O.zeroTo(25, i => addThreeDays());                               
        }        
    }
}