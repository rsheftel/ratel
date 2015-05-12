using System;
using System.Collections.Generic;
using System.Threading;
using NUnit.Framework;

namespace Q.Util {
    public class TimerManager : Objects {
        internal bool isInterceptingTimersForTest;
        readonly LazyDictionary<DateTime, List<Action>> timers  = new LazyDictionary<DateTime, List<Action>>(time => new List<Action>());
        DateTime lastTestTimeRun;
        readonly Dictionary<DateTime, string> allowedTimes = new Dictionary<DateTime, string>();

        static void withErrorsLogged(Action action) {
            try {
                action();
            } catch(Exception e) {
                LogC.err("exception caught from timer", e);
            }
        }

        public void atTime(DateTime time, Action action, out Timer timer) {
            if (isInterceptingTimersForTest) {
                addTestTimer(time, action);
                timer = neverRun();
                return;
            }
            timer = new Timer(o => withErrorsLogged(action), null, timeTilDue(time), new TimeSpan(-1));
        }

        public void everyMillis(long millis, Action action, out Timer timer) {
            if (isInterceptingTimersForTest) {
                addTestTimer(now(), () => runAndAddNext(millis, action));
                timer = neverRun();
                return;
            }
            timer = new Timer(o => withErrorsLogged(action), null, 0, millis);
        }

        public void everyMillisStartingAt(DateTime time, long millis, Action action, out Timer timer) {
            if (isInterceptingTimersForTest) doNothing();
            timer = new Timer(o => withErrorsLogged(action), null, (long) timeTilDue(time).TotalMilliseconds, millis);
        }

        public void inMillis(long millis, Action action, out Timer timer) {
            if (isInterceptingTimersForTest) doNothing();
            timer = new Timer(o => withErrorsLogged(action), null, millis, 0);
        }
        
        public void intercept(string time, string name) {
            intercept(date(time), name);
        }

        public void intercept(DateTime time, string name) {
            allowedTimes.Add(time, name);
        }

        void runAndAddNext(long millis, Action action) {
            action();
            addTestTimer(now().AddMilliseconds(millis), () => runAndAddNext(millis, action));
        }

        static TimeSpan timeTilDue(DateTime time) {
            var dueTime = time.Subtract(now());
            if(!dueTime.Equals(dueTime.Duration())) dueTime = new TimeSpan(0);
            return dueTime;
        }

        void addTestTimer(DateTime time, Action action) {
            Bomb.unless(allowedTimes.ContainsKey(time), () => "can't add timer for " + ymdHuman(time) + " call intercept if this time is expected.");
            timers.get(time).Add(action);
        }

        static Timer neverRun() {
            return new Timer(o => { }, null, new TimeSpan(7, 0, 0, 0), new TimeSpan(0));
        }

        public void runTimers(DateTime time) {
            Bomb.unless(time.CompareTo(lastTestTimeRun) > 0, () => 
                "attempting to run timers backwards or rerun the same time, \nlastTestTimeRun is " + ymdHuman(lastTestTimeRun) + "\nthis time: " + ymdHuman(time));
            freezeNow(time);
            LogC.info("run " + Bomb.missing(allowedTimes, time) + " timers " + ymdHuman(time));
            var actions = copy(timers.get(time));
            timers.remove(time);
            allowedTimes.Remove(time);
            Bomb.when(isEmpty(actions), () => "no timers expecting to run on " + ymdHuman(time));
            each(actions, action => action());
            lastTestTimeRun = time;
        }

        public void runTimers(string time) {
            runTimers(date(time));
        }
        
        internal void exitTimerTestMode() {
            isInterceptingTimersForTest = false;
            timers.clear();
            lastTestTimeRun = default(DateTime);
        }

        public void requireEmpty() {
            Assert.IsTrue(timers.size() == 0);
        }

    }
}