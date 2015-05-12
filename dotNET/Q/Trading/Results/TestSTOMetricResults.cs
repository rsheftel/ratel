using file;
using NUnit.Framework;
using Q.Util;
using O=Q.Util.Objects;

namespace Q.Trading.Results {
    [TestFixture]
    public class TestSTOMetricResults  : DbTestCase {
        [Test]
        public void testBasicFunctionality() {
            var results = hamster();
            HasCount(49999, results.runs());
            Contains("QAnnualizedNetProfit", O.list(results.metricNames()));
            var metrics = (RunMetrics) results[1];
            AreEqual(2, metrics.run());
            IsTrue(double.IsNaN(metrics.metric("QAnnualizedNetProfit")));
            results.populateValues(metrics);
            AreEqual(1669377.62360075, metrics.metric("QAnnualizedNetProfit"));
            metrics = (RunMetrics) results[1];
            AreEqual(1669377.62360075, metrics.metric("QAnnualizedNetProfit"));
            results.clearCache(metrics);
            metrics = (RunMetrics) results[1];
            IsTrue(double.IsNaN(metrics.metric("QAnnualizedNetProfit")));
            LogC.info("done");
        }

        static STOMetricResults hamster() {
            return new STOMetricResults(new QFile(@"..\..\testdata\hamster\Benchmark_1.0_daily_ALL.ham"), new QFile(@"..\..\testdata\hamster\Benchmark_1.0_daily.ham"));
        }

        [Test]
        public void testSortedRunNumbersByDownsideDeviation() {
            var minDeviationRuns = O.list(9727, 19727, 29727, 39727, 49727);
            var results = hamster();
            var actual = results.runsByMetric("QDownsideDeviation").GetRange(125, 5);
            LogC.info(Objects.toShortString(actual));
            AreEqual(minDeviationRuns, O.sort(actual));
        }

        [Test]
        public void testSortedRunNumbersByParameter() {
            var minLengthDnRuns = O.list(O.convert(O.seq(2500), i => 1 + 20 * i));
            var results = hamster();
            var actual = results.runsByParameter("LengthDn").GetRange(0, 2500);
            LogC.info(Objects.toShortString(actual));
            AreEqual(minLengthDnRuns, O.sort(actual));
        }

        [Test]
        public void testCollectionView() {
            var results = hamster();
            IsFalse(results.CanGroup);
            IsTrue(results.CanSort);
            results.MoveCurrentToPosition(126);
            AreEqual(126, results.CurrentPosition);
            AreEqual(126, ((RunMetrics) results.CurrentItem).run());
            results.sortBy("QDownsideDeviation");
            AreEqual(9727, ((RunMetrics) results.CurrentItem).run() % 10000);
        }

    }
}
