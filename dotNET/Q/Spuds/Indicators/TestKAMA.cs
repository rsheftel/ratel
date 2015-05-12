using O=Q.Util.Objects;
using NUnit.Framework;

namespace Q.Spuds.Indicators {
    [TestFixture]
    public class TestKAMA : SpudTestCase<double, double> {
        [Test]
        public void testKAMA() {
            indicator = new KAMA(values, 2, 30, 8);
            Bombs(() => O.info(indicator[0] + ""), "uninitialized");

            //The values below were originally calced from the ExampleKAMA.xls sheet
            addPoint(38.5, 38.5);
			addPoint(37.2, 37.2);
			addPoint(37.7, 37.7);
			addPoint(38, 38);
			addPoint(38.5, 38.5);
			addPoint(38.4, 38.4);
			addPoint(38.25, 38.25);
			addPoint(39, 39);
			//First point after the initial calc period
            addPointRound(40, 39.0681);
            addPointRound(39, 39.0612);
			addPointRound(38, 39.0502);
            addPointRound(37.5, 39.0260);
            addPointRound(40.3, 39.0838);
            addPointRound(10, 31.2031);
        }

        void addPointRound(double newPoint, double expected) {
            addPoint(newPoint);
            AlmostEqual(expected, indicator[0], 1e-4);
        }

        [Test]
        public void testCalcTwice() {
            indicator = new KAMA(values, 2, 30, 8);
            addPoint(0, 0);
            values.set(0);
            AreEqual(0, indicator[0]);
        }
    }
}