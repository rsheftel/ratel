using System;
using System.Collections.Generic;
using org.apache.commons.math.stat.regression;
using Q.Util;

namespace Q.Trading.Results {
    public class Metrics : Objects {
        public delegate double MetricCalculator(StatisticsCollector collector);
        internal readonly Dictionary<string, MetricCalculator> calculators;

        public static readonly MetricCalculator NET_PROFIT = collector => sum(collector.pnl());
        public static readonly MetricCalculator MAX_DRAWDOWN = maxDrawdown;
        public static readonly MetricCalculator KRATIO = kRatio;
        public static readonly MetricCalculator ANNUALIZED_NET_PROFIT = annualizedNetProfit;
        public static readonly MetricCalculator CALMAR_RATIO = collector => safeDivide(0, ANNUALIZED_NET_PROFIT(collector), -MAX_DRAWDOWN(collector));
        public static readonly MetricCalculator CONDITIONAL_TEN_PERCENTILE_CALMAR_RATIO =
            collector => safeDivide(0, ANNUALIZED_NET_PROFIT(collector), -percentileDrawdown(collector, .1, true));
        public static readonly MetricCalculator CONDITIONAL_TWENTY_PERCENTILE_CALMAR_RATIO =
            collector => safeDivide(0, ANNUALIZED_NET_PROFIT(collector), -percentileDrawdown(collector, .2, true));
        public static readonly MetricCalculator AVERAGE_DRAWDOWN = averageDrawdown;
        public static readonly MetricCalculator AVERAGE_DRAWDOWN_TIME = averageDrawdownTime;
        public static readonly MetricCalculator AVERAGE_DRAWDOWN_RECOVERY_TIME = averageDrawdownRecoveryTime;
        public static readonly MetricCalculator TEN_PERCENTILE_DRAWDOWN = collector => percentileDrawdown(collector, 0.1, false);
        public static readonly MetricCalculator CONDITIONAL_TEN_PERCENTILE_DRAWDOWN = collector => percentileDrawdown(collector, 0.1, true);
        public static readonly MetricCalculator TWENTY_PERCENTILE_DRAWDOWN = collector => percentileDrawdown(collector, 0.2, false);
        public static readonly MetricCalculator CONDITIONAL_TWENTY_PERCENTILE_DRAWDOWN = collector => percentileDrawdown(collector, 0.2, true);
        public static readonly MetricCalculator SORTINO_RATIO = collector => kappaRatio(collector, 2) * annualizationFactor(collector);
        public static readonly MetricCalculator OMEGA_RATIO = collector => kappaRatio(collector, 1) + 1;
        public static readonly MetricCalculator UPSIDE_POTENTIAL_RATIO = upsidePotentialRatio;
        public static readonly MetricCalculator EXPECTANCY = expectancy;
        public static readonly MetricCalculator TRADES_PER_BAR = collector => TOTAL_TRADES(collector) / collector.dates().Count;
        public static readonly MetricCalculator EXPECTANCY_SCORE = collector => EXPECTANCY(collector) * TRADES_PER_BAR(collector);
        public static readonly MetricCalculator DOWNSIDE_DEVIATION = collector => semiDeviation(collector.pnl(), 2, false);
        public static readonly MetricCalculator STANDARD_DEVIATION_DAILY = collector => standardDeviation(collapsedPnl(collector, daily));
        public static readonly MetricCalculator STANDARD_DEVIATION_WEEKLY = collector => standardDeviation(collapsedPnl(collector, weekly));
        public static readonly MetricCalculator STANDARD_DEVIATION_MONTHLY = collector => standardDeviation(collapsedPnl(collector, monthly));
        public static readonly MetricCalculator SHARPE_RATIO = collector => safeDivide(0, ANNUALIZED_NET_PROFIT(collector), STANDARD_DEVIATION_DAILY(collector)) / Math.Sqrt(252);
        public static readonly MetricCalculator SHARPE_RATIO_WEEKLY = collector => safeDivide(0, ANNUALIZED_NET_PROFIT(collector), STANDARD_DEVIATION_WEEKLY(collector)) / Math.Sqrt(52);
        public static readonly MetricCalculator SHARPE_RATIO_MONTHLY = collector => safeDivide(0, ANNUALIZED_NET_PROFIT(collector), STANDARD_DEVIATION_MONTHLY(collector)) / Math.Sqrt(12);
        public static readonly MetricCalculator TOTAL_SLIPPAGE = collector => collector.all().totalSlippage();
        public static readonly MetricCalculator AVERAGE_SLIPPAGE_WINNING_TRADES = collector => collector.winning().averageSlippage();
        public static readonly MetricCalculator AVERAGE_SLIPPAGE_LOSING_TRADES = collector => collector.losing().averageSlippage();

        public static readonly MetricCalculator LARGEST_WINNING_TRADE = collector => collector.winning().maxPnl();
        public static readonly MetricCalculator LARGEST_LOSING_TRADE = collector => collector.losing().minPnl();
        public static readonly MetricCalculator AVERAGE_LOSS = collector => collector.losing().averagePnl();
        public static readonly MetricCalculator AVERAGE_WIN = collector => collector.winning().averagePnl();
        public static readonly MetricCalculator REALIZED_NET_PROFIT = collector => collector.all().sumPnl();
        public static readonly MetricCalculator REALIZED_GROSS_LOSS = collector => collector.losing().sumPnl();
        public static readonly MetricCalculator REALIZED_GROSS_PROFIT = collector => collector.winning().sumPnl();
        public static readonly MetricCalculator AVERAGE_PROFIT = collector => collector.all().averagePnl();
        public static readonly MetricCalculator UNREALIZED_NET_PROFIT = collector => NET_PROFIT(collector) - REALIZED_NET_PROFIT(collector);
        public static readonly MetricCalculator WINNING_TRADES_PERCENT = winningTradesPercent;
        public static readonly MetricCalculator LOSING_TRADES_PERCENT = losingTradesPercent;
        public static readonly MetricCalculator NEUTRAL_TRADES = collector => collector.neutral().count();
        public static readonly MetricCalculator WINNING_TRADES = collector => collector.winning().count();
        public static readonly MetricCalculator LOSING_TRADES = collector => collector.losing().count();
        public static readonly MetricCalculator LONG_WINNING_TRADES = collector => collector.longWinning().count();
        public static readonly MetricCalculator LONG_LOSING_TRADES = collector => collector.longLosing().count();
        public static readonly MetricCalculator SHORT_WINNING_TRADES = collector => collector.shortWinning().count();
        public static readonly MetricCalculator SHORT_LOSING_TRADES = collector => collector.shortLosing().count();
        public static readonly MetricCalculator MAX_CONSECUTIVE_WINNING_TRADES = collector => collector.winning().maxConsecutive();
        public static readonly MetricCalculator MAX_CONSECUTIVE_LOSING_TRADES = collector => collector.losing().maxConsecutive();
        public static readonly MetricCalculator TOTAL_FINISHED_TRADES = collector => collector.all().count();
        public static readonly MetricCalculator TOTAL_TRADES = collector => TOTAL_FINISHED_TRADES(collector) + collector.openTrades();
        public static readonly MetricCalculator AVERAGE_LOSING_BARS_HELD = collector => collector.losing().averageBarsHeld();
        public static readonly MetricCalculator AVERAGE_WINNING_BARS_HELD = collector => collector.winning().averageBarsHeld();
        public static readonly MetricCalculator AVERAGE_BARS_HELD = collector => collector.all().averageBarsHeld();
        public static readonly MetricCalculator WINNING_BARS_HELD = collector => collector.winning().totalBarsHeld();
        public static readonly MetricCalculator LOSING_BARS_HELD = collector => collector.losing().totalBarsHeld();
        public static readonly MetricCalculator TOTAL_BARS_HELD = collector => collector.all().totalBarsHeld();
        public static readonly MetricCalculator WIN_LOSS_RATIO = collector => safeDivide(0, AVERAGE_WIN(collector), -AVERAGE_LOSS(collector));
        public static readonly MetricCalculator AVERAGE_TRADE = collector => safeDivide(0, NET_PROFIT(collector), TOTAL_TRADES(collector));
        public static readonly MetricCalculator NET_PROFIT_PER_MAX_DRAWDOWN = collector => safeDivide(0, NET_PROFIT(collector), -MAX_DRAWDOWN(collector));
        public static readonly MetricCalculator LARGEST_LOSS_PER_AVERAGE_LOSS = collector => safeDivide(0, LARGEST_LOSING_TRADE(collector), AVERAGE_LOSS(collector));
        public static readonly MetricCalculator LARGEST_LOSS_PER_GROSS_LOSS = collector => safeDivide(0, LARGEST_LOSING_TRADE(collector), REALIZED_GROSS_LOSS(collector));
        public static readonly MetricCalculator LARGEST_WIN_PER_AVERAGE_WIN = collector => safeDivide(0, LARGEST_WINNING_TRADE(collector), AVERAGE_WIN(collector));
        public static readonly MetricCalculator LARGEST_WIN_PER_GROSS_PROFIT = collector => safeDivide(0, LARGEST_WINNING_TRADE(collector), REALIZED_GROSS_PROFIT(collector));
        public static readonly MetricCalculator LARGEST_WIN_PER_NET_PROFIT = collector => safeDivide(0, LARGEST_WINNING_TRADE(collector), NET_PROFIT(collector));
        
        public Metrics() {
            calculators = new Dictionary<string, MetricCalculator> {
                {"QDownsideDeviation", DOWNSIDE_DEVIATION},
                {"QStandardDeviation", STANDARD_DEVIATION_DAILY},
                {"QStandardDeviationWeekly", STANDARD_DEVIATION_WEEKLY},
                {"QStandardDeviationMonthly", STANDARD_DEVIATION_MONTHLY},
                {"QSharpeRatio", SHARPE_RATIO},
                {"QSharpeRatioWeekly", SHARPE_RATIO_WEEKLY},
                {"QSharpeRatioMonthly", SHARPE_RATIO_MONTHLY},
                {"QNetProfitPerMaxDrawdown", NET_PROFIT_PER_MAX_DRAWDOWN},
                {"QLargestLossPerAverageLoss", LARGEST_LOSS_PER_AVERAGE_LOSS},
                {"QLargestLossPerGrossLoss", LARGEST_LOSS_PER_GROSS_LOSS},
                {"QLargestWinPerAverageWin", LARGEST_WIN_PER_AVERAGE_WIN},
                {"QLargestWinPerGrossProfit", LARGEST_WIN_PER_GROSS_PROFIT},
                {"QLargestWinPerNetProfit", LARGEST_WIN_PER_NET_PROFIT},
                {"QWinLossRatio", WIN_LOSS_RATIO},
                {"QAverageTrade", AVERAGE_TRADE},
                {"QExpectancy", EXPECTANCY},
                {"QExpectancyScore", EXPECTANCY_SCORE},
                {"QTradesPerBar", TRADES_PER_BAR},
                {"QLargestWinningTrade", LARGEST_WINNING_TRADE},
                {"QLargestLosingTrade", LARGEST_LOSING_TRADE},
                {"QAverageLoss", AVERAGE_LOSS},
                {"QAverageWin", AVERAGE_WIN},
                {"QRealizedNetProfit", REALIZED_NET_PROFIT},
                {"QRealizedGrossLoss", REALIZED_GROSS_LOSS},
                {"QRealizedGrossProfit", REALIZED_GROSS_PROFIT},
                {"QAverageProfit", AVERAGE_PROFIT},
                {"QNetProfit", NET_PROFIT},
                {"QUnrealizedNetProfit", UNREALIZED_NET_PROFIT},
                {"QWinningTradesPct", WINNING_TRADES_PERCENT},
                {"QLosingTradesPct", LOSING_TRADES_PERCENT},
                {"QNeutralTrades", NEUTRAL_TRADES},
                {"QWinningTrades", WINNING_TRADES},
                {"QLosingTrades", LOSING_TRADES},
                {"QLongWinningTrades", LONG_WINNING_TRADES},
                {"QLongLosingTrades", LONG_LOSING_TRADES},
                {"QShortWinningTrades", SHORT_WINNING_TRADES},
                {"QShortLosingTrades", SHORT_LOSING_TRADES},
                {"QMaxConsecutiveWinningTrades", MAX_CONSECUTIVE_WINNING_TRADES},
                {"QMaxConsecutiveLosingTrades", MAX_CONSECUTIVE_LOSING_TRADES},
                {"QTotalFinishedTrades", TOTAL_FINISHED_TRADES},
                {"QTotalTrades", TOTAL_TRADES},
                {"QAverageLosingBarsHeld", AVERAGE_LOSING_BARS_HELD},
                {"QAverageWinningBarsHeld", AVERAGE_WINNING_BARS_HELD},
                {"QAverageBarsHeld", AVERAGE_BARS_HELD},
                {"QWinningBarsHeld", WINNING_BARS_HELD},
                {"QLosingBarsHeld", LOSING_BARS_HELD},
                {"QTotalBarsHeld", TOTAL_BARS_HELD},
                {"QMaxDrawdown", MAX_DRAWDOWN},
                {"QKRatio", KRATIO},
                {"QAnnualizedNetProfit", ANNUALIZED_NET_PROFIT},
                {"QCalmarRatio", CALMAR_RATIO},
                {"QConditionalTenPercentileCalmarRatio", CONDITIONAL_TEN_PERCENTILE_CALMAR_RATIO},
                {"QConditionalTwentyPercentileCalmarRatio", CONDITIONAL_TWENTY_PERCENTILE_CALMAR_RATIO},
                {"QAverageDrawdown", AVERAGE_DRAWDOWN},
                {"QAverageDrawdownTime", AVERAGE_DRAWDOWN_TIME},
                {"QAverageDrawdownRecoveryTime", AVERAGE_DRAWDOWN_RECOVERY_TIME},
                {"QTenPercentileDrawdown", TEN_PERCENTILE_DRAWDOWN},
                {"QConditionalTenPercentileDrawdown", CONDITIONAL_TEN_PERCENTILE_DRAWDOWN},
                {"QTwentyPercentileDrawdown", TWENTY_PERCENTILE_DRAWDOWN},
                {"QConditionalTwentyPercentileDrawdown", CONDITIONAL_TWENTY_PERCENTILE_DRAWDOWN},
                {"QSortinoRatio", SORTINO_RATIO},
                {"QOmegaRatio", OMEGA_RATIO},
                {"QUpsidePotentialRatio", UPSIDE_POTENTIAL_RATIO},
                {"QTotalSlippage", TOTAL_SLIPPAGE},
                {"QAverageSlippagePerWinningTrade", AVERAGE_SLIPPAGE_WINNING_TRADES},
                {"QAverageSlippagePerLosingTrade", AVERAGE_SLIPPAGE_LOSING_TRADES},
            };
        }

        public Dictionary<string, double> values(StatisticsCollector collector) {
            Bomb.when(collector.pnl().Count == 0, () => "must have at least one observation");
            collector.cacheAllDrawdowns(allDrawdowns(collector));
            var result = dictionary(calculators.Keys, name => calculators[name](collector));
            collector.cacheAllDrawdowns(null);
            return result;
        }
        
        static double maxDrawdown(StatisticsCollector collector) {
            var pnl = collector.pnl();
            if(isEmpty(pnl)) return 0;
            var equity = cumulativeSum(pnl);
            var highWaterMark = cumulativeMax(equity);
            return min(subtract(equity, highWaterMark));
        }

        static double kRatio(StatisticsCollector collector) {
            var pnl = collector.pnl();
            if(all(pnl, p => p == 0)) return 0;

            var equity = cumulativeSum(pnl);
            var regression = new SimpleRegression();
            eachIt(equity, (i, p) => regression.addData(i + 1, p));
            var b1 = regression.getSlope();
            var stdError = regression.getSlopeStdErr();
            return b1 / (stdError * Math.Sqrt(pnl.Count));
        }

        static double kappaRatio(StatisticsCollector collector, int moment) {
            var pnl = collector.pnl();
            if(all(pnl, p => p == 0)) return 0;
            return average(pnl) / Math.Pow(lowerPartialMoment(pnl, moment), 1.0/moment);
        }

        static double lowerPartialMoment(IEnumerable<double> pnl, int moment) {
            return average(convert(pnl, x => Math.Pow(Math.Max(-x, 0), moment)));
        }

        static double upsidePotentialRatio(StatisticsCollector collector) {
            var pnl = collector.pnl();
            return semiDeviation(pnl, 1, true) / semiDeviation(pnl, 2, false);
        }

        static double semiDeviation(IEnumerable<double> pnl, int moment, bool isUpside) {
            var subset = accept(pnl, x => isUpside ? x > 0 : x < 0);
            return Math.Pow(average(convert(subset, x => Math.Pow(x, moment))), 1.0 / moment);
        }

        static double annualizationFactor(StatisticsCollector collector) {
            return Math.Sqrt(count(collector.pnl())/yearsInCollector(collector));            
        }

        public static double yearsInCollector(StatisticsCollector collector) {
            var firstDate = first(collector.dates());
            var lastDate = last(collector.dates());
            return lastDate.Subtract(firstDate).TotalDays / 365.25;          
        }            
        
        static IEnumerable<double> collapsedPnl(StatisticsCollector collector, Converter<DateTime, DateTime> toKey) {
            var bucketed = new Dictionary<DateTime, double>();
            each(collector.dates(), collector.pnl(), (barDate, pnl) => {
                var key = toKey(barDate);
                double total;
                bucketed.TryGetValue(key, out total);
                bucketed[key] = total + pnl;
            });
            return convert(sort(bucketed.Keys), key => bucketed[key]);
        }

        static DateTime monthly(DateTime bar) {
            return new DateTime(bar.Year, bar.Month, 1);
        }

        static DateTime weekly(DateTime bar) {
            var sunday = bar.AddDays(DayOfWeek.Sunday - bar.DayOfWeek);
            return new DateTime(sunday.Year, sunday.Month, sunday.Day);
        }

        static DateTime daily(DateTime bar) {
            return new DateTime(bar.Year, bar.Month, bar.Day);
        }

        static double expectancy(StatisticsCollector collector) {
            var expectedWin = AVERAGE_WIN(collector) * WINNING_TRADES_PERCENT(collector);
            var expectedLoss = AVERAGE_LOSS(collector) * (1 - WINNING_TRADES_PERCENT(collector));
            return (expectedLoss + expectedWin) / Math.Abs(AVERAGE_LOSS(collector));
        }

        static double annualizedNetProfit(StatisticsCollector collector) {        
            return NET_PROFIT(collector) / yearsInCollector(collector);
        }

        static double averageDrawdownTime(StatisticsCollector collector) {
            var drawdowns = closedDrawdowns(collector);
            if(drawdowns.Count == 0) return 0;
            return drawdowns.Count == 0 ? 0 : average(convert(drawdowns, d => (double) d.length()));
        }

        static double averageDrawdownRecoveryTime(StatisticsCollector collector) {
            var drawdowns = closedDrawdowns(collector);
            if(drawdowns.Count == 0) return 0;
            return drawdowns.Count == 0 ? 0 : average(convert(drawdowns, d => (double) d.recoveryTime()));
        }

        static double averageDrawdown(StatisticsCollector collector) {
            var drawdowns = collector.allDrawdowns();
            if(drawdowns.Count == 0) return 0;
            return -average(convert(drawdowns, d => d.size()));
        }

        // conditional: average one to n.alpha instead of n.alppha alone
        static double percentileDrawdown(StatisticsCollector collector, double percentile, bool isConditional) {
            var drawdowns = collector.allDrawdowns();
            var notEnough = percentile == 0.1 ? drawdowns.Count < 10 : drawdowns.Count < 5;            
            if (notEnough) return 0;
            var nAlpha = (int) Math.Floor(drawdowns.Count * percentile) - 1;
            if(isConditional) return -average(convert(sort(drawdowns).GetRange(0, nAlpha + 1), dd => dd.size()));
            return -sort(drawdowns)[nAlpha].size();
        }

        static List<Drawdown> closedDrawdowns(StatisticsCollector collector) {
            var drawdowns = copy(collector.allDrawdowns());
            if (isEmpty(drawdowns)) return drawdowns;
            if(last(drawdowns).isOpen()) drawdowns.RemoveAt(drawdowns.Count - 1);
            return drawdowns;
        }

        internal static List<Drawdown> allDrawdowns(StatisticsCollector collector) {
            var equities = cumulativeSum(collector.pnl());
            var highWaterMarks = cumulativeMax(equities);
            Drawdown current = null;
            var drawdowns = new List<Drawdown>();
            each(equities, highWaterMarks, (equity, highWaterMark) => {
                if(equity == highWaterMark) {
                    if(current != null) current.closed();
                    current = null;
                    return;
                }
                if(current == null) {
                    current = new Drawdown();
                    drawdowns.Add(current);
                }
                current.add(equity, highWaterMark);
            });
            return drawdowns;
        }

        public class Drawdown : IComparable<Drawdown> {
            int length_;
            double size_;
            bool closed_;
            int worstPoint;

            public void add(double equity, double highWaterMark) {
                length_++;
                var current = highWaterMark - equity;
                if (current <= size_) return;
                size_ = current;
                worstPoint = length_;
            }

            public double size() {
                return size_;
            }

            public int length() {
                return length_ + 1;
            }

            public void closed() {
                closed_ = true;
            }

            public bool isOpen() {
                return !closed_;
            }

            public int recoveryTime() {
                return length() - worstPoint;
            }

            public int CompareTo(Drawdown other) {
                return other.size_.CompareTo(size_);
            }
        }

        static double winningTradesPercent(StatisticsCollector collector) {
            return safeDivide(0, WINNING_TRADES(collector), TOTAL_FINISHED_TRADES(collector));
        }

        static double losingTradesPercent(StatisticsCollector collector) {
            return safeDivide(0, LOSING_TRADES(collector), TOTAL_FINISHED_TRADES(collector));
        }
    }
}