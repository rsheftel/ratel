using jms;
using NUnit.Framework;
using systemdb.data;

namespace Q.Util {
    [TestFixture]
    public class TestHeartbeat : DbTestCase {

        [Test]
        public void testBasicFunctionality() {
            Objects.freezeNow("2009/06/25 12:00:00");
            var timers = Objects.timerManager();
            timers.isInterceptingTimersForTest = true;
            var heart = new Heartbeat(JMSTestCase.TEST_BROKER, "test.heartbeat", 2000);
            var fieldsResult = new Fields[] {null};
            heart.subscribe(fields => {
                Bomb.unlessNull(fieldsResult[0], () => "have " + fieldsResult[0] + " but received " + fields);
                fieldsResult[0] = fields;
            });
            timers.intercept("2009/06/25 12:00:00", "first beat");
            heart.initiate();
            timers.intercept("2009/06/25 12:00:02", "second beat");
            timers.runTimers("2009/06/25 12:00:00");
            Objects.wait(() => fieldsResult[0] != null);
            AreEqual(Objects.hostname(), fieldsResult[0].get("Hostname"));
            fieldsResult[0] = null; 

            timers.intercept("2009/06/25 12:00:04", "third beat");
            timers.runTimers("2009/06/25 12:00:02");
            Objects.wait(() => fieldsResult[0] != null);

        }
    }
}