using System;
using NUnit.Framework;
using JMarket=systemdb.metadata.Market;
using O = Q.Util.Objects;

namespace Q.Util {
    
    [TestFixture]
    public class TestObjects : QAsserts {
        [Test]
        public void TestStuff() {
            AreEqual(1, 1);
        }

        [Test]
        public static void Main(string[] args) {
            new JMarket("TY.1C");
        }

        [Test]
        public void testJDateCanHandleDefaultCDate() {
            var defalt = new DateTime();
            AreEqual("1753/01/01", O.ymdHuman(new DateTime(1753, 1, 1)));
            AreEqual("0001/01/03", O.ymdHuman(defalt));
            AreEqual("0001/01/03", O.ymdHuman(O.jDate(defalt)));
            AreEqual("0001/01/03", O.ymdHuman(O.date(O.ymdHuman(defalt))));
        }

        [Test]
        public void testShortString() {
            AreEqual("[a, b, c]", O.toShortString(O.list("a", "b", "c")));
            AreEqual("2.34K", O.toShortString(2341));
            AreEqual("2.34u", O.toShortString(.000002341));
            AreEqual("1.00G", O.toShortString(1000002341));
            AreEqual("1.00M", O.toShortString(1002341));
        }

        [Test]
        public void testConvert() {
            var letters = O.list("a", "b", "c");
            var sevens = O.convert(letters, l => 7);
            AreEqual(O.list(7, 7, 7), O.list(sevens));
        }

        [Test]
        public void testFilter() {
            var numbers = O.list(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            var odds = O.list(1, 3, 5, 7, 9);
            AreEqual(odds, O.accept(numbers, i => i % 2 == 1));
        }

        [Test]
        public void testRest() {
            var numbers = O.seq(10);
            AreEqual(Objects.second(numbers), O.first(O.rest(numbers)));
            AreEqual(Objects.nth(numbers, 3), O.second(O.rest(numbers)));
        }

        [Test]
        public void testReverse() {
            AreEqual(O.list(4,3,2,1), O.list(O.reverse(Objects.list(1, 2, 3, 4))));
            AreEqual(O.list(O.reverse(O.reverse(Objects.seq(15)))), O.list(O.seq(15)));
        }
    }
}