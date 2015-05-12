using NUnit.Framework;
using Q.Messaging;
using Q.Util;

namespace Q.Trading {
    [TestFixture]
    public class TestSystemHeartbeat : DbTestCase {
        SystemHeartbeat heart;

        public override void setUp() {
            base.setUp();
            var ty1C = new Symbol("RE.TEST.TY.1C");
            heart = new SystemHeartbeat(LIVE_SYSTEM, ty1C, "something");
        }

        [Test]
        public void testHeartBeats() {
            var manager = Objects.timerManager();
            var counter = new PublishCounter("something.S.V.I.PV.RE.TEST.TY.1C.heartbeat");
            Objects.freezeNow("2009/08/08 08:08:08");
            manager.isInterceptingTimersForTest = true;
            manager.intercept("2009/08/08 08:08:08", "first beat");
            heart.goLive();
            manager.intercept("2009/08/08 08:08:09", "second beat");
            manager.runTimers("2009/08/08 08:08:08");
            counter.requireCount(1);
        }
    }
}
