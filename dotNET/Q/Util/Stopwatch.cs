using System;
using System.Collections.Generic;

namespace Q.Util {
    public class Stopwatch : Objects {
        TimeSpan elapsed;
        int iterations;
        DateTime recentStart;
        TimeSpan thisElapsed; // local to the end method really, but we don't want a new object to be allocated in that method. 

        static readonly Dictionary<string, Stopwatch> timers = new Dictionary<string, Stopwatch>();
        static bool memory;
        long recentMemoryStart;
        long totalMemoryUsed;
        bool started;

        public static void start(string a) {
            get(a).start();
        }

        public static void stop(string a) {
            get(a).stop();
        }

        static Stopwatch get(string a) {
            if (!timers.ContainsKey(a))
                timers.Add(a, new Stopwatch());
            return timers[a];
        }

        public void add(Action run) {
            start();
            run();
            stop();
        }

        void stop() {
            if (!started) return;
            thisElapsed = reallyNow().Subtract(recentStart);
            elapsed = elapsed.Add(thisElapsed);
            iterations++;
            if (memory) totalMemoryUsed += GC.GetTotalMemory(true) - recentMemoryStart;
            started = false;
        }

        void start() {
            if (memory) recentMemoryStart = GC.GetTotalMemory(true);
            started = true;
            recentStart = reallyNow();
        }

        public double millis() {
            return elapsed.TotalMilliseconds;
        }

        public double seconds() {
            return millis() / 1000.0;
        }

        public double millisPer() {
            return millis() / iterations;
        }

        public static string report(string description) {
            if (get(description).iterations == 0) return "";
            var result = description + " - total millis: " + toShortString(millis(description)) + ", " 
                + get(description).iterations + "x" + toShortString(millisPer(description));
            if (memory) result += " bytesUsed: " + toShortString(bytesUsed(description)) + ", x" + toShortString(bytesUsedPer(description));
            LogC.info(result);
            return result;
        }

        public void add(int n, Action run) {
            while (n-- > 0) add(run);
        }

        public static double millis(string a) {
            return get(a).millis();
        }

        public static double seconds(string a) {
            return get(a).seconds();
        }

        public static double millisPer(string a) {
            return get(a).millisPer();
        }

        public static void doMemoryProfiling() {
            memory = true;
        }

        public static long bytesUsed(string s) {
            Bomb.unless(memory, () => "can't ask for bytes used, not recording memory data.");
            return get(s).bytesUsed();
        }

        long bytesUsed() {
            Bomb.when(started, () => "eek! in the middle of an operation");
            return totalMemoryUsed;
        }

        public static double bytesUsedPer(string s) {
            return (bytesUsed(s) + 0.0) / get(s).iterations;
        }

        public static void add(string s, int i, Action list) {
            get(s).add(i, list);
        }
    }
}