using System.Collections.Generic;
using amazon;
using db;
using NUnit.Framework;
using Q.Util;
using sto;
using systemdb.metadata;
using DbTestCase=Q.Util.DbTestCase;

namespace Q.Trading.Results {
    [TestFixture]
    public class TestMetricFiles : DbTestCase {
        [Test]
        public void testCanWriteToS3() {
            var id = 5203;
            new MetaBucket("quantys-5203").create();
            var metrics = new MetricResults();
            var marketName = "RE.TEST.TY.1C";
            var runNumber = 1;
            var expected = new Dictionary<string, double> {
                { "value1", 1.0 },
                { "value2", 5.005 },
                { "value3", 0.005 }
            };
            metrics.Add(marketName, expected);
            metrics.Add(marketName + ".2", expected);
            MetricFiles.writeToS3(id, runNumber, metrics);
            var actual = MetricFiles.readFromS3(id, runNumber);
            IsTrue(Objects.dictionaryEquals(expected, actual[marketName]));
            IsTrue(Objects.dictionaryEquals(expected, actual[marketName + ".2"]));
        }

        [Test]
        public void testCanWriteMetricFile() {
            var id = 5203;
            MetricFiles.writeOne(id, "RE.TEST.TY.1C", 1, new Dictionary<string, double> {
                { "value1", 1.0 },
                { "value2", 5.005 },
                { "value3", 0.005 }
            });
            var details = SystemDetailsTable.DETAILS.details(id);
            var sto = new STO(details);
            checkMetrics(sto, 1.0, 5.005, 0.005, 1);
            IsTrue(Db.explicitlyCommitted());

            MetricFiles.writeOne(id, "RE.TEST.TY.1C", 10, new Dictionary<string, double> {
                { "value1", 2.0 },
                { "value2", 6.005 },
                { "value3", 0.006 }
            });
            checkMetrics(sto, 2.0, 6.005, 0.006, 10);
            IsTrue(Db.explicitlyCommitted());
        }

        static void checkMetrics(STO sto, double value1, double value2, double value3, int run) {
            AreEqual(value1, sto.getMetric("value1", "RE.TEST.TY.1C", run));
            AreEqual(value2, sto.getMetric("value2", "RE.TEST.TY.1C", run));
            AreEqual(value3, sto.getMetric("value3", "RE.TEST.TY.1C", run));
        }
    }
}