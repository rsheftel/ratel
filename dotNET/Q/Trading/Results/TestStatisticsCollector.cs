using System;
using System.Collections.Generic;
using NUnit.Framework;
using Q.Systems;
using Q.Systems.Examples;
using Q.Util;
using O=Q.Util.Objects;

namespace Q.Trading.Results {
    [TestFixture]
    public class TestStatisticsCollector : OneSymbolSystemTest<ExampleSymbolSystem> {
        DateTime currentDate = O.date("2009/01/06");

        [Test]
        public void testMetricCalcs() {
            var collector = new StatisticsCollector(arguments());
            addClosedPosition(collector, 100.0, 1);
            addClosedPosition(collector, 90.0, 2);
            addClosedPosition(collector, -90.25, 3);
            addClosedPosition(collector, 0, 4);
            addClosedPosition(collector, -98.25, 5);
            
            var position = addOpenPosition(collector, 95.0, 10);
            addBar(collector, position, 96, 1);
            AreEqual(11.50 * symbol().bigPointValue, collector.netProfit());

            addBar(collector, position, 97, 1);
            AreEqual(21.50 * symbol().bigPointValue, collector.netProfit());
            addBar(collector, position, 92, 1);
            addBar(collector, position, 97, 1);
            
            var expectedMetrics = new Dictionary<string, double> {
                {"QDownsideDeviation", 50000},
                {"QStandardDeviation", 41298.4563876182},
                {"QStandardDeviationWeekly", 0},
                {"QStandardDeviationMonthly", 0},
                {"QSharpeRatio", 3.99276123210182},
                {"QSharpeRatioWeekly", 0},
                {"QSharpeRatioMonthly", 0},
                {"QNetProfitPerMaxDrawdown", 21500 / 50000.0},
                {"QLargestLossPerAverageLoss", 98250 / 94250.0},
                {"QLargestLossPerGrossLoss", 98250 / 188500.0},
                {"QLargestWinPerAverageWin", 100000 / 95000.0},
                {"QLargestWinPerGrossProfit", 100000 / 190000.0},
                {"QLargestWinPerNetProfit", 100000 / 21500.0},
                {"QWinLossRatio", 1.00795755968170},
                {"QAverageTrade", 21500 / 6.0},
                {"QExpectancy", -0.196816976127321},
                {"QTradesPerBar", 1.5},
                {"QExpectancyScore", -0.295225464190981},
                {"QLargestWinningTrade", 100000},
                {"QLargestLosingTrade", -98250},
                {"QAverageLoss", -94250},
                {"QAverageWin", 95000},
                {"QRealizedNetProfit", 1500},
                {"QRealizedGrossLoss", -188500},
                {"QRealizedGrossProfit", 190000},
                {"QAverageProfit", 300},
                {"QNetProfit", 21500},
                {"QUnrealizedNetProfit", 20000},
                {"QWinningTradesPct", 0.40},
                {"QLosingTradesPct", 0.40},
                {"QNeutralTrades", 1},
                {"QWinningTrades", 2},
                {"QLosingTrades", 2},
                {"QLongWinningTrades", 2},
                {"QLongLosingTrades", 2},
                {"QShortWinningTrades", 0},
                {"QShortLosingTrades", 0},
                {"QMaxConsecutiveWinningTrades", 2},
                {"QMaxConsecutiveLosingTrades", 1},
                {"QTotalFinishedTrades", 5},
                {"QTotalTrades", 6},
                {"QAverageLosingBarsHeld", 4},
                {"QAverageWinningBarsHeld", 1.5},
                {"QAverageBarsHeld", 3},
                {"QWinningBarsHeld", 3},
                {"QLosingBarsHeld", 8},
                {"QTotalBarsHeld", 15},
                {"QMaxDrawdown", -50000},
                {"QKRatio", -0.077151674981046},
                {"QAnnualizedNetProfit", 2617625},
                {"QCalmarRatio", 52.3525},
                {"QConditionalTenPercentileCalmarRatio",0},
                {"QConditionalTwentyPercentileCalmarRatio",0},
                {"QAverageDrawdown", -50000},
                {"QAverageDrawdownTime", 2},
                {"QAverageDrawdownRecoveryTime", 1},
                {"QTenPercentileDrawdown", 0},
                {"QConditionalTenPercentileDrawdown", 0},
                {"QTwentyPercentileDrawdown", 0},
                {"QConditionalTwentyPercentileDrawdown", 0},
                {"QSortinoRatio", 4.74463644550349},
                {"QOmegaRatio", 1.43},
                {"QUpsidePotentialRatio", 0.476666666666667},
                {"QTotalSlippage", 156.25},
                {"QAverageSlippagePerWinningTrade", 31.25},
                {"QAverageSlippagePerLosingTrade", 31.25}
            };
            
            var metrics = collector.metrics();
            O.each(expectedMetrics, (name, value) => AlmostEqual(value, Bomb.missing(metrics, name), 1e-10));
            AreEqual(expectedMetrics.Count, metrics.Count);
        }        
        
        
        [Test]
        public void testForeignExchangeFXConversion() {
            symbol().setBigPointValue(5000);

            var collector = new StatisticsCollector(arguments());
            doSomeTrades(collector);

            O.each(O.list(-90000.0, 369000, -253500, -907000, 304500, 91000), collector.pnl(), (a, b) => AlmostEqual(a, b, 1e-8));
            var metrics = collector.metrics();
            AreEqual(1, metrics["QWinningTrades"]);
            AlmostEqual(91500, metrics["QAverageWin"], 1e-8);
            AlmostEqual(-577500, metrics["QAverageLoss"], 1e-8);
            AlmostEqual(-486000, metrics["QNetProfit"], 1e-8);
        }
                
        [Test]
        public void testForeignExchangeFXConversionCanBeTurnedOff() {
            symbol().setBigPointValue(5000);

            var args = arguments();
            args.runInNativeCurrency = true;
            var collector = new StatisticsCollector(args);
            doSomeTrades(collector);

            O.each(O.list(-15000.0, 60000, -90000, 130000, -35000, 70000), collector.pnl(), (a, b) => AlmostEqual(a, b, 1e-8));
            var metrics = collector.metrics();
            AreEqual(2, metrics["QWinningTrades"]);
            AlmostEqual(60000, metrics["QAverageWin"], 1e-8);
            AlmostEqual(0, metrics["QAverageLoss"], 1e-8);
            AlmostEqual(120000, metrics["QNetProfit"], 1e-8);
        }

        void doSomeTrades(StatisticsCollector collector) {
            var first = addTrade(symbol().buy("buy", market(), 3, oneBar()).placed(), collector, 100, 0, 1.05);
            addBar(collector, O.list(first), 99, 1);
            addBar(collector, O.list(first), 103, 1.2);
            addBar(collector, O.list(first), 97, 1.1);
            addTrade(first.exit("out", market(), oneBar()).placed(), collector, 101, 0, 1.1);
            var second = addTrade(symbol().sell("sell", market(), 7, oneBar()).placed(), collector, 102, 0, 1.1);
            addBar(collector, O.list(second), 100, 1.4);
            addBar(collector, O.list(second), 101, 1.3);
            addTrade(second.exit("out", market(), oneBar()).placed(), collector, 99, 0, 1.3);
            addBar(collector, 99, 0.9);
        }

        [Test]
        public void testMaxDrawdown() {
            var collector = new StatisticsCollector(arguments());
            var position = addOpenPosition(collector, 0.0, 1);
            oneBar(collector, position, 10, 10000, 0);
            oneBar(collector, position, 8, 8000, -2000);
            oneBar(collector, position, 11, 11000, -2000);
            oneBar(collector, position, 8, 8000, -3000);
            oneBar(collector, position, 6, 6000, -5000);
            oneBar(collector, position, 8, 8000, -5000);
            oneBar(collector, position, 12, 12000, -5000);
        }

        void oneBar(StatisticsCollector collector, Position position, double close, double expectedProfit, double expectedDrawdown) {
            addBar(collector, position, close, 1);
            AreEqual(expectedProfit, collector.netProfit());
            AreEqual(expectedDrawdown, Metrics.MAX_DRAWDOWN(collector));
        }

        void addBar(StatisticsCollector collector, Position position, double close, double fxRate) {
            addBar(collector, O.list(position), close, fxRate);
        }

        void addBar(StatisticsCollector collector, IEnumerable<Position> positions, double close, double fxRate) {
            var weightedPositions = O.convert(positions, p => new WeightedPosition(p));
            collector.addBar(weightedPositions, O.dictionaryOne(symbol(), new Bar(close, close, close, close, currentDate)), O.dictionaryOne(symbol(), fxRate));
            currentDate = currentDate.AddDays(1);
        }

        void addBar(StatisticsCollector collector, double close, double fxRate) {
            addBar(collector, new List<Position>(), close, fxRate);
        }

        Position addOpenPosition(StatisticsCollector collector, double price, int size) {
            var order = symbol().buy("buy one", market(), size, oneBar()).placed();
            return addTrade(order, collector, price - 0.015625, 0.015625, 1);
        }

        [Test]
        public void testUsesPriorCloseWhenBarMissingOnOpenTrade() {
            var c = collector();
            c.addBar(new List<WeightedPosition>(), Objects.dictionaryOne(symbol(), new Bar(5, 5, 5 ,5)), Objects.dictionaryOne(symbol(), 1.0));
            addOpenPosition(c, 5, 1);
            c.addBar(new List<WeightedPosition>(), Objects.dictionaryOne(new Symbol("FOO"), new Bar(990, 99, 99, 99)), Objects.dictionaryOne(new Symbol("FOO"), 1.0));
            AreEqual(0, c.netProfit());
        }

        [Test]
        public void testNoTrades() {
            var c = collector();
            addBar(c, 0, 1);
            addBar(c, 0, 1);
            var metrics = c.metrics();
            isDefaulted(metrics, "QOmegaRatio", 1);
            isDefaulted(metrics, "QUpsidePotentialRatio", Double.NaN);
            isDefaulted(metrics, "QExpectancy", Double.NaN);
            isDefaulted(metrics, "QExpectancyScore", Double.NaN);
            isDefaulted(metrics, "QDownsideDeviation", Double.NaN);
            isDefaulted(metrics, "QAverageSlippagePerWinningTrade", Double.NaN);
            isDefaulted(metrics, "QAverageSlippagePerLosingTrade", Double.NaN);
            IsEmpty(O.accept(metrics, (name, value) => value != 0.0));
        }

        static void isDefaulted(IDictionary<string, double> metrics, string name, double value) {
            AreEqual(value, metrics[name]);
            metrics.Remove(name); 
        }

        StatisticsCollector collector() {
            return new StatisticsCollector(arguments());
        }

        void addClosedPosition(StatisticsCollector collector, double pnl, int barsHeld) {
            var order = symbol().buy("buy one", market(), 1, oneBar()).placed();
            var position = addTrade(order, collector, -0.015625, 0.015625, 1);
            O.zeroTo(barsHeld, i => position.newBar());
            var exit = position.exit("get out", market(), oneBar()).placed();
            addTrade(exit, collector, pnl + 0.015625, 0.015625, 1);
        }

        static Position addTrade(Order order, StatisticsCollector collector, double fillPrice, double slippage, double fxRate) {
            var trade = new Trade(order, fillPrice, order.size, slippage, fxRate);
            var position = order.fill(trade);
            collector.addOrder(new WeightedPosition(position), new WeightedTrade(trade));
            return position;
        }

        protected override int leadBars() {
            return 0;
        }

        protected override Parameters parameters() {
            var parameters = base.parameters();
            parameters.overwrite("lookback", "2");
            return parameters;
        }
    }
}