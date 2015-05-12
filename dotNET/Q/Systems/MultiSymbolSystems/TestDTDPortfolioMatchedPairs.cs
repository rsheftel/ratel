using System;
using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;
using Q.Util;
using systemdb.metadata;

namespace Q.Systems.MultiSymbolSystems {
    [TestFixture]
    public class TestDTDPortfolioMatchedPairs : OneSystemTest<DTDPortfolioMatchedPairs> {
        
        static readonly Symbol[] symbols = Objects.produce(() => {
            var result = new Symbol[5];
            Objects.zeroTo(5, i => {
                result[i] = new Symbol("CDS.TEST.CAH.5Y.ACB20" + i, 10000);
                MarketTable.MARKET.insert(result[i].name, new java.lang.Double (1.0));
                MarketHistoryTable.MARKET_HISTORY.insert(
                    new systemdb.metadata.Market(result[i].name), Objects.jDate("2001-01-01"), null);
            });
            return result;
        });

        DateTime currentDate;

        protected override SystemArguments arguments() {
            return new SystemArguments(symbols, parameters());
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
                {"maxPairs", 1},
                {"requiredSlippageMultiplier", 2}
            });
        }

        public override void setUp() {
            base.setUp();

            currentDate = date("2009-01-01");
            Objects.each(symbols, symbol => {
                var subSymbolSystem = symbolSystem(symbol);
                subSymbolSystem.richCheap.enterTestMode();
                subSymbolSystem.dv01.enterTestMode();
                subSymbolSystem.spread.enterTestMode();
                subSymbolSystem.dtd.enterTestMode();
            });

            Objects.zeroTo(leadBars(),i => processBar(
                bar(0, 1, 1, 0, 0.01, 1),
                bar(1, 1, 1, 0, 0.02, 1),
                bar(2, 1, 1, 0, 0.03, 1),
                bar(3, 1, 1, 0, 0.04, 1),
                bar(4, 1, 1, 0, 0.05, 1)));
        }
        
        static Dictionary<Symbol, Bar> collectBars(params Bar[] bars) {
            return Objects.dictionary(Objects.convert(Objects.seq(5), i => symbols[i]), bars);
        }

        void processClose(params Bar[] bars) {
            processClose(collectBars(bars));
        }
        
        void processBar(params Bar[] bars) {
            processBar(collectBars(bars));
            currentDate = currentDate.AddDays(1);
        }

        Bar[] bar11() {
            var bars = new Bar[5];
            bars[0] = bar(0, 1, 1, 0.0015, 0.01, 1);
            bars[1] = bar(1, 1, 1, -0.0015, 0.02, 1);
            bars[2] = bar(2, 1, 1, 0.0013, 0.03, 1);
            bars[3] = bar(3, 1, 1, 0, 0.04, 1);
            bars[4] = bar(4, 1, 1, 0, 0.05, 1);
            return bars;
        }

        Bar[] bar12() {
            var bars = new Bar[5];
            bars[0] = bar(0, 1, 1, 0.0010, 0.01, 1);
            bars[1] = bar(1, 1, 1, -0.0013, 0.02, 1);
            bars[2] = bar(2, 1, 1, 0.0011, 0.03, 1);
            bars[3] = bar(3, 1, 1, 0, 0.04, 1);
            bars[4] = bar(4, 1, 1, -0.0025, 0.05, 1);
            return bars;
        }
    
        [Test]
        public void testDTDPortfolioMatchedPairsBasic () {
            noOrders();
            processClose(bar11());
            hasOrders(
                symbols[0].sell("SE", market(), 10, oneBar()),
                symbols[1].buy("LE", market(), 10, oneBar()));
            Objects.zeroTo(2, i => fill(symbols[i],0,1.0));
            noOrders();
            processBar(bar11());
            noOrders();
            processClose(bar12());
            hasOrders(symbols[1].sell("LX Max Pairs", market(), 10, oneBar()),
                symbols[4].buy("LE", market(), 10, oneBar()));
            fill(symbols[1], 0, 1.0); fill(symbols[4], 0, 1.0);
            noOrders();
        }

        protected override int leadBars() {
            return 10;
        }
    }
}