using O=Q.Util.Objects;
using NUnit.Framework;
using Q.Trading;

namespace Q.Spuds.Indicators {
    [TestFixture]
    public class TestAverageTrueRangeEW : SpudTestCase<Bar, double> {
        [Test]
        public void testATR() {
            indicator = new AverageTrueRangeEW(values, 1);
            Bombs(() => O.info(indicator[0] + ""), "uninitialized");
            addPoint(new Bar(1, 1, 1, 1), 0);
            addPoint(new Bar(2, 2, 2, 2), 0.5);
            AreEqual(0.5, indicator[0]);
            manager.newTick();
            AreEqual(0.25, indicator[0]);
            manager.newTick();
            AreEqual(0.25, indicator[0]);
            manager.newBar();
            AreEqual(0.25, indicator[0]);
        }

        [Test]
        public void testCalcTwice() {
            indicator = new AverageTrueRangeEW(values, 1);
            var bar = new Bar(1, 1, 1, 1);
            addPoint(bar, 0);
            values.set(bar);
            AreEqual(0, indicator[0]);
        }
    }
}