using System;
using System.Collections.Generic;
using NUnit.Framework;
using O = Q.Util.Objects;

namespace Q.Util {
    [TestFixture] public class TestEach : DbTestCase {
        static readonly List<string> LETTERS = Objects.list("a", "b", "c");
        static readonly List<int> START = Objects.list(5, 6, 7);

        [Test] public void testEach() {
            var result = new List<string>();

            O.each(LETTERS, result.Add);
            AreEqual(LETTERS, result);
            result.Clear();
            O.eachIt(LETTERS, (i, s) => result.Add(i + " " + s));
            AreEqual(O.list("0 a", "1 b", "2 c"), result);
            int[] sum = {0};
            O.eachIt(START, delegate(int i, int val) { sum[0] += val + i; });
            AreEqual(sum[0], 21);
            sum[0] = 0;
            O.eachIt(O.list(7, 8, 9), delegate(int i, int val) { sum[0] += val * i; });
            AreEqual(sum[0], 26);
        }

        [Test] public void testParallelEach() {
            string[] content = {""};
            O.each(
                LETTERS,
                START,
                delegate(int i, string s, int val) {
                    content[0] += i + ":" + s + "(" + val + ")";
                    O.info(i + ":" + s + "(" + val + ")");
                });
            AreEqual("0:a(5)1:b(6)2:c(7)", content[0]);
            content[0] = "";
            O.each(LETTERS, START, delegate(string s, int val) { content[0] += s + "(" + val + ")"; });
            AreEqual("a(5)b(6)c(7)", content[0]);
            Bombs(() => O.each(O.list(1, 2, 3, 4), O.list(2, 3, 4), delegate(int i1, int i2) { }),
                "failed",
                "mismatched");
            Bombs(() => O.each(O.list(1, 2, 3), O.list(1, 2, 3, 4), delegate(int i1, int i2) { }), "mismatched");

            Bombs(() => O.each(O.list(4, 5, 6), val => Bomb.when(val == 5, () => "VALUE IS 5!")),
             "failed@1, processing: 5 in \\[4, 5, 6\\]", "VALUE IS 5");
        }

        public void perftestConvertPerformance() {
            var dic = new Dictionary<int, int> {{1, 2}};
            Stopwatch.add("direct", 10000000, delegate { new Dictionary<int, int> {{1, dic[1]}}; });
            Stopwatch.report("direct");

            Converter<int, int> nop = i => i;
            Stopwatch.add("withConvert", 10000000, () => O.convert(dic, nop, nop));
            Stopwatch.report("withConvert");
        }

        public void perftestListPerformance() {
            var l = new List<int>();
            Action appendToList = () => l.Add(0);
            for(var i = 1000; i < 20000; i+= 1000) {
                l.Clear();
                Stopwatch.add("" + i, i, appendToList);
                Stopwatch.report("" + i);
            }
        }

    }
}