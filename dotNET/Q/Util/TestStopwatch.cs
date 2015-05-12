using System.Collections.Generic;
using O = Q.Util.Objects;

namespace Q.Util {
    public class TestStopwatch : DbTestCase {
        public void testAccumulatingSomeRunTime() {
            const string A = "a";
            const string B = "b";
            O.zeroTo(10, i => {
                Stopwatch.start(A);
                Objects.sleep(10);
                Stopwatch.stop(A);
            });
            Stopwatch.start(B);
            O.zeroTo(100, i => O.sleep(10));
            Stopwatch.stop(B);
            IsTrue(Stopwatch.millis(A) > 100, "elapsed outside allowed range: " + Stopwatch.millis(A) + " " + Stopwatch.seconds(A));
            IsTrue(Stopwatch.seconds(A) < 0.3, "elapsed outside allowed range: " + Stopwatch.millis(A) + " " + Stopwatch.seconds(A));
            IsTrue(Stopwatch.millis(B) > 1000 && Stopwatch.seconds(B) < 4, "elapsed outside allowed range: " + Stopwatch.seconds(B));
            IsTrue(Stopwatch.millisPer(A) >= 10 && Stopwatch.millisPer(A) < 30);
            Matches("a - total", Stopwatch.report(A));
        }

        public void testMemoryCalculator() {
            var l = new List<double>();
            Stopwatch.doMemoryProfiling();
            Stopwatch.start("foo");
            O.zeroTo(10, i => {
                Stopwatch.start("foo");
                l.Add(1.0);
                Stopwatch.stop("foo");
            });
            IsTrue(Stopwatch.bytesUsed("foo") >= 80 && Stopwatch.bytesUsed("foo") < 160, "used " + Stopwatch.bytesUsed("foo"));
            IsTrue(Stopwatch.bytesUsedPer("foo") >= 8 && Stopwatch.bytesUsedPer("foo") < 16);
            Matches("bytes", Stopwatch.report("foo"));
        }

        public void testStopStart() {
            Stopwatch.doMemoryProfiling();
            Stopwatch.report("asdf");
            Stopwatch.stop("asdf");
            Stopwatch.report("asdf");
            Stopwatch.start("asdf");
        }

    }
}
