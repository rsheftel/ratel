using NUnit.Framework;
using Q.Util;

namespace Q.Spuds.Indicators {
    [TestFixture]
    public class TestBollingerBand : SpudTestCase<double, double> {
        [Test]
        public void testBollingerBand() {
            indicator = new BollingerBand(values, 20, 2);
            var points = new[] {
                59.75, 60.5313, 61.5, 60.5, 58.4063, 58, 58.0313, 56.875, 58.25, 55.5313, 
                54.25, 55.5, 55.875, 57.625, 58.625, 57.625, 59.125, 60.4063, 60, 60.25
            };
            Objects.each(points, addPoint);
            AlmostEqual(62.2048, indicator[0], 0.0001);
        }

        [Test]
        public void testSimple() {
            indicator = new BollingerBand(values, 2, -2);
            addPoint(3);
            addPoint(3, 3);
            addPoint(5, 2);
            addPoint(9, 3);
        }
    }
}