using System;
using System.Collections.Generic;
using java.lang;
using NUnit.Framework;
using Q.Trading;
using Q.Trading.Slippage;
using Q.Util;
using systemdb.metadata;
using O=Q.Util.Objects;

namespace Q.Systems.MultiSymbolSystems {
    [TestFixture]
    public class TestDTDPortfolioPositionLimits : OneSystemTest<DTDPortfolioPositionLimits> {

        static Symbol[] symbols;

        DateTime currentDate;

        protected override SystemArguments arguments() {
           return new SystemArguments(symbols, parameters());
        }

        protected override void initializeSymbols() {
            symbols = new Symbol[5];
            O.zeroTo(5, i => {
                symbols[i] = new Symbol("CDS.TEST.CAH.5Y.ACB20" + i, 10000);
                SystemTimeSeriesTable.SYSTEM_TS.insert(symbols[i].name, "TSDB", "ActiveMQ", "TEST." + symbols[i]);
                MarketTable.MARKET.insert(symbols[i].name, "TEST", new java.lang.Double (1.0), "12:35:00", new Integer(4));
                MarketHistoryTable.MARKET_HISTORY.insert(new systemdb.metadata.Market(symbols[i].name), Objects.jDate("2001-01-01"), null);
            });
        }


        Bar bar(int symbolIndex, double tri, double dtd, double rc, double spread, double dv01) {
            var symbol = symbols[symbolIndex];
            var subSystem = symbolSystem(symbol);
            subSystem.dtd.overwrite(currentDate, dtd);
            subSystem.richCheap.overwrite(currentDate, rc);
            subSystem.spread.overwrite(currentDate, spread);
            subSystem.dv01.overwrite(currentDate, dv01);
            return new Bar(tri, tri, tri, tri, currentDate);
        }        
        
        void tick(int symbolIndex, double tri, double dtd, double rc, double spread, double dv01, DateTime time) {
            var symbol = symbols[symbolIndex];
            var subSystem = symbolSystem(symbol);
            subSystem.dtd.addTick(time, dtd);
            subSystem.richCheap.addTick(time, rc);
            subSystem.spread.addTick(time, spread);
            subSystem.dv01.addTick(time, dv01);
            processTick(symbol, tri, time);
        }

        DTDRichCheapV2 symbolSystem(Symbol symbol) {
            return system().systems_[symbol];
        }

        protected override Parameters parameters() {
            return base.parameters().overwrite(new Dictionary<string, double> {
                {"exitLongLevel", 1},
                {"exitShortLevel", -1},
                {"lengthZScore", leadBars()},
                {"longSize", 10},
                {"lossStopLevel", 1000000},
                {"shortSize", 10},
                {"triggerLong", -2.5},
                {"triggerShort", 2},
                {"timeStopBars", 10},
                {"maxSpread", 0.12},
                {"profitObjectiveMultiple", 0},
                {"trailingStopFlag", 0},
                {"maxPositions", 3},
                {"requiredSlippageMultiplier", 2}
            });
        }

        public override void setUp() {
            base.setUp();

            currentDate = date("2009-01-01");
            O.each(symbols, symbol => {
                var subSymbolSystem = symbolSystem(symbol);
                subSymbolSystem.richCheap.enterTestMode();
                subSymbolSystem.dv01.enterTestMode();
                subSymbolSystem.spread.enterTestMode();
                subSymbolSystem.dtd.enterTestMode();
            });

            O.zeroTo(leadBars(),i => processBar(new[] {
                bar(0, 1, 1, 0, 0.01, 1),
                bar(1, 1, 1, 0, 0.02, 1),
                bar(2, 1, 1, 0, 0.03, 1),
                bar(3, 1, 1, 0, 0.04, 1),
                bar(4, 1, 1, 0, 0.05, 1)}));
        }
        
        static Dictionary<Symbol, Bar> collectBars(IEnumerable<Bar> bars) {
            return O.dictionary(O.convert(O.seq(5), i => symbols[i]), bars);
        }

        void processClose(IEnumerable<Bar> bars) {
             processClose(collectBars(bars));
        }
        
        void processBar(IEnumerable<Bar> bars) {
            var symbolBars = collectBars(bars);
            processBar(symbolBars);
        }

        protected override void processBar(Dictionary<Symbol, Bar> symbolBars) {
            base.processBar(symbolBars);
            currentDate = currentDate.AddDays(1);
        }

        Bar[] bar11() {
            var bars = new Bar[5];
            bars[0] = bar(0, 1, 1, 0.0015, 0.01, 1);
            bars[1] = bar(1, 1, 1, -0.0015, 0.02, 1);
            bars[2] = bar(2, 1, 1, 0.0015, 0.03, 1);
            bars[3] = bar(3, 1, 1, 0, 0.04, 1);
            bars[4] = bar(4, 1, 1, 0, 0.05, 1);
            return bars;
        }

        Bar[] bar12() {
            var bars = new Bar[5];
            bars[0] = bar(0, 1, 1, 0.0010, 0.01, 1);
            bars[1] = bar(1, 1, 1, -0.0013, 0.02, 1);
            bars[2] = bar(2, 1, 1, 0.0011, 0.03, 1);
            bars[3] = bar(3, 1, 1, 0.0014, 0.04, 1);
            bars[4] = bar(4, 1, 1, -0.0015, 0.05, 1);
            return bars;
        }

        Dictionary<Symbol, Bar> bar12MissingZero() {
            var result = collectBars(bar12());
            result.Remove(symbols[0]);
            return result;
        }

        Bar[] bar11V2() {
            var bars = new Bar[5];
            bars[0] = bar(0, 1, 1, 0, 0.01, 1);
            bars[1] = bar(1, 1, 1, 0, 0.02, 1);
            bars[2] = bar(2, 1, 1, 0, 0.03, 1);
            bars[3] = bar(3, 1, 1, 0.0020, 0.04, 1);
            bars[4] = bar(4, 1, 1, 0, 0.05, 1);
            return bars;
        }

        Bar[] bar12V2() {
            var bars = new Bar[5];
            bars[0] = bar(0, 1, 1, 0.0010, 0.01, 1);
            bars[1] = bar(1, 1, 1, -0.0013, 0.02, 1);
            bars[2] = bar(2, 1, 1, 0.0011, 0.03, 1);
            bars[3] = bar(3, 1, 1, 0, 0.04, 1);
            bars[4] = bar(4, 1, 1, -0.0015, 0.05, 1);
            return bars;
        }

        Bar[] bar11V3() {
            var bars = new Bar[5];
            bars[0] = bar(0, 1, 1, 0, 0.01, 1);
            bars[1] = bar(1, 1, 1, 0, 0.02, 1);
            bars[2] = bar(2, 1, 1, 0, 0.03, 1);
            bars[3] = bar(3, 1, 1, 0.0050, 0.04, 1);
            bars[4] = bar(4, 1, 1, 0, 0.05, 1);
            return bars;
        }

        Bar[] bar12V3() {
            var bars = new Bar[5];
            bars[0] = bar(0, 1, 1, 0.0010, 0.01, 1);
            bars[1] = bar(1, 1, 1, -0.0013, 0.02, 1);
            bars[2] = bar(2, 1, 1, 0.0011, 0.03, 1);
            bars[3] = bar(3, 1, 1, 0.0050, 0.04, 1);
            bars[4] = bar(4, 1, 1, -0.0015, 0.05, 1);
            return bars;
        }
    
        [Test]
        public void testDTDPortfolioPositionLimitBasic () {
            noOrders();
            processClose(bar11());
            hasOrders(symbols[0].sell("SE",market(),10,oneBar()),
                      symbols[1].buy("LE",market(),10,oneBar()),
                      symbols[2].sell("SE", market(), 10, oneBar()));
            O.zeroTo(3, i => fill(symbols[i],0,1.0));
            noOrders();
            processBar(bar11());
            noOrders();
            processClose(bar12());
            hasOrders(symbols[0].buy("SX Max Positions", market(), 10, oneBar()),
                      symbols[4].buy("LE", market(), 10, oneBar()));
            fill(symbols[0], 0, 1.0); fill(symbols[4], 0, 1.0);
            noOrders();
        }

        [Test]
        public void testDTDPortfolioPositionLimitMissingData () {
            noOrders();
            processClose(bar11());
            hasOrders(symbols[0].sell("SE",market(),10,oneBar()),
                      symbols[1].buy("LE",market(),10,oneBar()),
                      symbols[2].sell("SE", market(), 10, oneBar()));
            O.zeroTo(3, i => fill(symbols[i],0,1.0));
            noOrders();
            processBar(bar11());
            noOrders();
            system().systems_[symbols[0]].setExpectedProfitCalculationDisallowedForTest(true);
            processClose(bar12MissingZero());
            hasOrders(symbols[2].buy("SX Max Positions", market(), 10, oneBar()),
                      symbols[4].buy("LE", market(), 10, oneBar()));
            fill(symbols[2], 0, 1.0); fill(symbols[4], 0, 1.0);
            noOrders();
        }

        [Test]
        public void testDTDPortfolioPositionLimitNoMoreCurrentPositions() {
            noOrders();
            processClose(bar11V2());
            hasOrders(symbols[3].sell("SE", market(), 10, oneBar()));
            fill(symbols[3], 0, 1.0);
            noOrders();
            processBar(bar11V2());
            noOrders();
            processClose(bar12V2());
            hasOrders(symbols[1].buy("LE", market(), 10, oneBar()),
                      symbols[2].sell("SE", market(), 10, oneBar()),
                      symbols[3].buy("SX Max Positions", market(), 10, oneBar()),
                      symbols[4].buy("LE", market(), 10, oneBar()));
            O.zeroTo(4, i => fill(symbols[i+1], 0, 1.0));
            noOrders();
        }

        [Test]
        public void testDTDPortfolioPositionLimitCurrentPositionsBetter() {
            noOrders();
            processClose(bar11V3());
            hasOrders(symbols[3].sell("SE", market(), 10, oneBar()));
            fill(symbols[3], 0, 1.0);
            noOrders();
            processBar(bar11V3());
            noOrders();
            processClose(bar12V3());
            hasOrders(symbols[1].buy("LE", market(), 10, oneBar()),
                      symbols[4].buy("LE", market(), 10, oneBar()));
            fill(symbols[1], 0, 1.0); fill(symbols[4], 0, 1.0);
            noOrders();
        }

        [Test]
        public void testLive() {
            O.timerManager().isInterceptingTimersForTest = true;
            noOrders();
            processClose(bar11());
            processBar(bar11());
            var goLiveTime = O.date(O.ymdHuman(currentDate) + " 09:00:00");
            O.freezeNow(goLiveTime);
            var closeTime = date(O.ymdHuman(currentDate) + " 12:34:56");
            Objects.timerManager().intercept(closeTime, "subsystem close");
            Objects.timerManager().intercept(goLiveTime, "multi system heartbeat");
            Objects.timerManager().intercept(goLiveTime.AddDays(10), "multi system close (never run)");
            tick(0, 0.01, 1, 1, 1, 1, O.now());
            O.timerManager().runTimers(closeTime);
        }

        protected override int leadBars() {
            return 10;
        }
    }

    public class SlippageBombs : SlippageCalculator {
        public SlippageBombs(Symbol symbol, BarSpud bars) : base(symbol, bars) {}
        public override double slippage() {
            throw Bomb.toss("no slippage at this point!");
        }
    }
}