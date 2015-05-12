using file;
using NUnit.Framework;
using Q.Messaging;
using Q.Trading.Results;
using Q.Util;
using systemdb.metadata;
using O=Q.Util.Objects;

namespace Q.Local {
    [TestFixture]
    public class TestSTOServer : DbTestCase {
        [Test]
        public void testStoWorks() {
            LogC.setOut("testStoWorks", @"C:\logs\localCloud.log", false);
            LogC.useJavaLog = true;
            const int systemId = 178114;

            var curveFile = new QFile(@"V:\Market Systems\General Market Systems\Benchmark\20080923\CurvesBin\Benchmark_1.0_daily_RE.TEST.TY.1C/run_678.bin");
            curveFile.deleteIfExists();
            var testQueue = new Queue("TEST.LocalCloud");
            O.timerManager().isInterceptingTimersForTest = true;
            O.freezeNow("2009/06/22 03:00:00");
            O.timerManager().intercept("2009/06/22 03:00:00", "heartbeat");
            O.timerManager().intercept("2009/06/22 03:00:03", "second heartbeat");
            var server = new STOServer(systemId, 2);
            var fired = false;
            server.heart.subscribe(fields => {
                fired = true;
                AreEqual(fields.get("Hostname"), O.hostname());
                AreEqual(fields.get("ServerIndex"), 2);
            });
            server.subscribe(testQueue);
            server.heart.initiate();
            IsFalse(fired);
            O.timerManager().runTimers("2009/06/22 03:00:00");
            O.wait(() => fired);
            MetricResults metrics = null;
            O.timerManager().intercept("2009/06/22 03:00:00", "client heartbeat");
            new STOClient(SystemDetailsTable.DETAILS.details(systemId), testQueue).metrics(678, results => metrics = results);
            O.wait(100, 1000, () => metrics != null);
            AreEqual(-1953125.00, metrics["RE.TEST.TY.1C"]["QNetProfit"]);
            curveFile.requireExists();
        }
    }
}
