using System;
using System.Diagnostics;
using jms;
using tsdb;
using JChannel=jms.Channel;
using mail;
using NUnit.Framework;
using Q.Messaging;
using Q.Recon;
using Q.Trading;
using systemdb.metadata;
using util;
using Attribute=tsdb.Attribute;
using O=Q.Util.Objects;
using TestAttribute=NUnit.Framework.TestAttribute;

namespace Q.Util {
    [TestFixture] public class DbTestCase : QAsserts {
        protected MockEmailer emailer;
        protected static readonly LiveSystem LIVE_SYSTEM = new LiveSystem(new Siv("S", "I", "V"), new Pv("PV"));

        [SetUp] public void setUpWithTeardown() {
            try {
                LogC.consoleOut("setUpWithTeardown");
                LogC.setOut("setup " + this, null, false);
                LogC.setVerboseLoggingForever(true);
                LogC.info("Starting " + this);
                Log.info("Starting " + this);
                setUp();
            }  catch {
                try {
                    tearDown();
                } catch (Exception e) {
                    LogC.err("failed in teardown after failing in setup! ", e);
                }
                throw;
            }
         }

        public virtual void setUp() {
            db.Db.beInNoCommitTestMode();
            Topic.clearCache();
            JMSTestCase.useTestBroker();
            QTopic.useRetroactiveConsumer = false;
            acquireTestLock();
            emailer = new MockEmailer();
            var id = LIVE_SYSTEM.populateDetailsIfNeeded(false);
            Environment.SetEnvironmentVariable("RE_TEST_MODE", "FALSE");
            FerretControl.setUpForTest();
        }

        public static string testName() {
            var frames = new StackTrace(false).GetFrames();
            if (frames == null) throw Bomb.toss("no frames on stack?");
            foreach (var frame in frames) {
                var mb = frame.GetMethod();
                var attributes = mb.GetCustomAttributes(typeof(TestAttribute), true);
                if (O.isEmpty(attributes)) continue;
                return ((TestAttribute) O.the(attributes)).Description;
            }
            throw Bomb.toss("no test attribute found on current test!");
        }

        void acquireTestLock() {
            while(O.trueDat()) {
                var otherLock = db.TestLocksTable.TEST_LOCK.tryAcquireLockOnce("test", GetType().FullName);
                if(otherLock.Equals(db.TestLocksTable.NONE)) return;
                LogC.info("waiting for test lock " + otherLock);
                O.sleep(350);
            }
        }

        [TearDown] public virtual void tearDown() {
            try {
                OrderTable.prefix = OrderTable.DEFAULT_PREFIX;
                db.Db.reallyRollback();
                emailer.reset();
                Dates.thawNow();
                SystemHeartbeat.waitTimeMillis = SystemHeartbeat.defaultWaitMillis;
                QTopic.useRetroactiveConsumer = true;
                JChannel.closeResources();
                O.timerManager().exitTimerTestMode();
                Symbol.clearCache();
                LIVE_SYSTEM.clearDetailsCache();
                LiveWatcher.setPositionsBrokerForTest(LiveWatcher.DEFAULT_POSITIONS_BROKER);
            } finally {
                db.TestLocksTable.TEST_LOCK.releaseLock("test", false);
                Log.doNotDebugSqlForever();
            }
        }

        public static void waitMatches<T>(T expected, Producer<T> actual) {
            var actual_ = default(T);
            try { O.wait(() => (actual_ = actual()).Equals(expected)); }
            catch (Exception e) {
                if (actual_.ToString().Equals(expected.ToString())) 
                    Bomb.toss(
                        "actual != but string matched\n" + expected + "\n" + 
                        "actual Type:"  + actual_.GetType().FullName + "\n" + 
                        "expected type:" + expected.GetType().FullName, e);
                Bomb.toss(actual_ + "\n    Did not match\n" + expected, e);
            }
        }

        static int emptySeriesId = 1;

        protected static TimeSeries createEmptyTestSeries() {
            emptySeriesId++;
            return TimeSeries.create("empty test series " + emptySeriesId, AttributeValues.values(new[] {
                Attribute.QUOTE_SIDE.value(new[] {"test"}),
                Attribute.INDEX_SERIES.value(new[] {"" + emptySeriesId})
            }));
        }

        protected static void insertSymbol(string name, TimeSeries series) {            
            SystemTimeSeriesTable.SYSTEM_TS.insert(name, "TSDB", "ActiveMQ", "some topic name");
            SystemTSDBTable.SYSTEM_SERIES_DATA.insert(name, new DataSource("test"), series.name());            
        }

        protected static void insertSymbol(string name) {            
            insertSymbol(name, createEmptyTestSeries());
        }

        protected static void insertMarket(string name, double slippage) {
            insertSymbol(name, createEmptyTestSeries());
            MarketTable.MARKET.insert(name, new java.lang.Double(slippage));
        }
    }
}