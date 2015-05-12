using System;
using System.Collections.Generic;
using file;
using java.util;
using NUnit.Framework;
using Q.Util;
using sto;
using util;
using O=Q.Util.Objects;
using JDate=java.util.Date;   

namespace Q.Trading.Results {
    [TestFixture]
    public class TestMetrics : DbTestCase {
        const string CURVE_FILE = "C:/svn/R/src/QFPortfolio/inst/testdata/FuncTestCurves/curve_NDayBreak_1_daily_AllFutures.csv";
        const string ANOTHER_FILE = "C:/svn/R/src/QFPortfolio/inst/testdata/PortfolioEquityCurves/NDayBreak_1.0_daily_BFBD30_FV.1C.csv";     
        const string INTRADAY_FILE = "C:/SVN/R/src/STO/inst/testdata/IntradaySTO/CurvesBin/CVE_10_Daily_CET10.AEP5M/run_1.bin";     

        [Test]
        public void testRMetrics() {
            var csvFile = Systematic.mainDir().file("R/scripts/STO/rMetricsTieOut.csv");
            var rScript = csvFile.path().Replace(".csv", ".r");
            O.runProcess(@"T:\R\R-2.5.0\bin\Rscript.exe", rScript);
            var csv = new Csv(csvFile, true);
            var cMetrics = new Metrics();
            O.zeroTo(csv.count(), i => {
                var values = csv.record(i);
                var metricName = csv.value("metricName", values);
                var calculator = Bomb.missing(cMetrics.calculators, qMetricName(metricName));
                checkMetric(metricName, calculator, values, csv);
            });
        }

        static string qMetricName(string rName) {
            rName = rName
                .Replace("DrawDown", "Drawdown")
                .Replace("DownSide", "Downside")
                .Replace("Daily", "")
                .Replace("WeeklyStandardDeviation", "StandardDeviationWeekly")
                .Replace("MonthlyStandardDeviation", "StandardDeviationMonthly")
                .Replace("WeeklySharpeRatio", "SharpeRatioWeekly")
                .Replace("MonthlySharpeRatio", "SharpeRatioMonthly")
                ;

            return "Q" + rName;
        }

        static void checkMetric(string rMetricName, Metrics.MetricCalculator metric, List values, Csv csv) {
            LogC.info("checking metric " + rMetricName);
            checkOne(metric, double.Parse(csv.value("tol1", values)), CURVE_FILE, double.Parse(csv.value("value1", values)));
            checkOne(metric, double.Parse(csv.value("tol2", values)), ANOTHER_FILE, double.Parse(csv.value("value2", values)));
            checkOne(metric, double.Parse(csv.value("tol3", values)), INTRADAY_FILE, double.Parse(csv.value("value3", values)));
        }

       

        static void checkOne(Metrics.MetricCalculator metric, double tol, string file, double rValue) {
            var statisticsCollector = collector(file);
            statisticsCollector.cacheAllDrawdowns(Metrics.allDrawdowns(statisticsCollector));
            var ours = metric(statisticsCollector);            
            AlmostEqual(rValue, ours, tol);
        }

        static StatisticsCollector collector(string fileName) {
            var curve = new Curve(fileName);
            var jPnl = curve.pnlObservations();
            var jPosition = curve.positionObservations();
            var dates = new List<DateTime>();
            var pnl = new List<double>();
            var position = new List<double>();
            foreach(JDate d in jPnl) {
                dates.Add(O.date(d));
                pnl.Add(jPnl.value(d));
                position.Add(jPosition.value(d));
            }
            
            return new StatisticsCollector(dates, pnl, position);
        }
    }
}