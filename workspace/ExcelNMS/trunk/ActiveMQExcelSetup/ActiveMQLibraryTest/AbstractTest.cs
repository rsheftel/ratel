using System;
using System.Threading;

namespace ActiveMQLibraryTest {
    /// <summary>
    /// Base class for unit tests.
    /// </summary>
    public abstract class AbstractTest
    {
        #region Wait For Delegates
        protected delegate bool WaitForHandler(object source);
        protected delegate bool WaitForValueHandler(object source, object value);
        #endregion

        #region Wait For Logic
        protected static void WaitFor(WaitForHandler d, object source, long waitPeriod)
        {
            var timedOut = DateTime.Now.AddMilliseconds(waitPeriod);
            while (DateTimeOffset.Now < timedOut && !d(source)) {
                Thread.Sleep(10); // give the CPU a rest
            }
        }

        protected static void WaitForValue(WaitForValueHandler d, object source, long waitPeriod, object value)
        {
            var timedOut = DateTime.Now.AddMilliseconds(waitPeriod);
            while (DateTimeOffset.Now < timedOut && !d(source, value)) {
                Thread.Sleep(1);
            }
        }

        #endregion
    }
}