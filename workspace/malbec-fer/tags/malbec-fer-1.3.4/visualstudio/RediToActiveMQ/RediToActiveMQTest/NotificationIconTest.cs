using System;
using NUnit.Framework;
using RediToActiveMQ;

namespace RediToActiveMQTest
{
    [TestFixture]
    public class NotificationIconTest
    {

        [Test]
        public void TestWithinRuntime() 
        {
            var now = DateTime.Now;

            var before = new DateTime(now.Year, now.Month, now.Day, 1, 0, 0);
            var after = new DateTime(now.Year, now.Month, now.Day, 23, 0, 0);
            var middle = new DateTime(now.Year, now.Month, now.Day, 10, 0, 0);

            var ni = new NotificationIcon();

            Assert.IsFalse(ni.WithinRuntime(before), "Time is not within limit");
            Assert.IsFalse(ni.WithinRuntime(after), "Time is not within limit");
            Assert.IsTrue(ni.WithinRuntime(middle), "Time is within limit");
        }

        [Test]
        public void TestCreateShutdownTimer()
        {
            var ni = new NotificationIcon();
            var delegateFired = false;
            long millisToWait;

            var shutdownTimer = ni.CreateShutdownTime(DateTime.Now, delegate { delegateFired = true;}, out millisToWait);

            Assert.IsNotNull(shutdownTimer, "Timer not created");
            Assert.IsTrue(millisToWait > 0 && !delegateFired, "Time to wait is less than 1 second");
        }

        [Test]
        public void TestCreateTime()
        {
            var now = DateTime.Now;

            var newTime = NotificationIcon.CreateDate(now, "22:00:00");

            Assert.AreEqual(newTime, new DateTime(now.Year, now.Month, now.Day, 22, 0, 0), "Failed to create date time");
        }

        [Test]
        public void TestGetCurrentUserId() {
            var userId = NotificationIcon.GetCurrentUserId("TEST");

            Assert.AreEqual("mfranz", userId);
            

        }
    }
}
