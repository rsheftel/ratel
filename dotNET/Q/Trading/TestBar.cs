using NUnit.Framework;
using Q.Util;
using O = Q.Util.Objects;

namespace Q.Trading {
    [TestFixture]
    public class TestBar : DbTestCase {
        [Test]
        public void testBarCannotBeInstantiatedWithInvalidOHLC() {
            new Bar(1, 1, 1, 1);
            Bombs(delegate {
                      new Bar(1, .9, 1, 1);
                  }, @"high \(0.9\) must be above low \(1\)");
        }

        [Test]
        public void testBarCanBeUpdatedFromATick() {
            O.freezeNow();
            var b = new Bar(1, 1, 1, 1);
            b = b.update(tick(2));
            AreEqual(new Bar(1, 2, 1, 2, O.now()), b);
            b = b.update(tick(0));
            AreEqual(new Bar(1, 2, 0, 0, O.now()), b);
        }

        [Test]
        public void testOrderedHighLow() {
            assertTickOrder(new Bar(0, 1, -1, 0), -1, 1); // equally far from open, chooses low
            assertTickOrder(new Bar(100, 110, 90, 100), 90, 110); // equally far from open, chooses low
            assertTickOrder(new Bar(0, 1.1, -1, 0), -1, 1.1); // -1 closer to open
            assertTickOrder(new Bar(0, 1, -1.1, 0), 1, -1.1); // 1 closer to open
        }

        static void assertTickOrder(Bar b, double first, double second) {
            AreEqual(O.list(first, second), b.orderedHighLow());
        }

        static Tick tick(int price) {
            return new Tick(price, 1, O.now());
        }
    }
}