using NUnit.Framework;
using Q.Trading;

namespace Q.Spuds.Indicators {
    [TestFixture]
    public class TestTrueRange : SpudTestCase<Bar, double> {
        [Test]
        public void testTrueRange() {
            indicator = new TrueRange(values);
            addPoint(new Bar(2, 4, 1, 3), 3);
            addPoint(new Bar(2, 4, 1, 3), 3);
            addPoint(new Bar(2, 2.5, 1, 2), 2);
            addPoint(new Bar(3, 5, 2.5, 4), 3);
        }
        [Test]
        public void testTrueRangeWithoutLookingAtTheValues() {
            indicator = new TrueRange(values);
            addPoint(new Bar(2, 4, 1, 3));
            addPoint(new Bar(2, 4, 1, 3));
            addPoint(new Bar(2, 2.5, 1, 2));
            addPoint(new Bar(3, 5, 2.5, 4), 3);
            AreEqual(2, indicator[1]);
        }

        [Test]
        public void testCalcTwice() {
            indicator = new TrueRange(values);
            addPoint(new Bar(2, 4, 1, 3), 3);
            values.set(new Bar(2, 4, 1, 3));
            AreEqual(indicator[0], 3);
        }
    }
}