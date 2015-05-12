using O=Q.Util.Objects;
using NUnit.Framework;

namespace Q.Spuds.Indicators {
    [TestFixture]
    public class TestSimpleAverage : SpudTestCase<double, double> {
        [Test]
        public void testAverage() {
            indicator = new Average(values);
            Bombs(() => O.info(indicator[0] + ""), "uninitialized");
            addPoint(1, 1);
            addPoint(2, 1.5);
            AreEqual(1.0, indicator[1]);
            addPoint(3, 2);
            addPoint(4, 2.5);
        }

        [Test]
        public void testMovingAverage() {
            indicator = new Average(values, 2);
            addPoint(1, 1);
            addPoint(2, 1.5);
            addPoint(3, 2.5);
            addPoint(4, 3.5);           
        }
    }
}