using NUnit.Framework;
using O=Q.Util.Objects;

namespace Q.Spuds.Indicators {
    [TestFixture]
    public class TestRSI : SpudTestCase<double, double> {
        [Test]
        public void testRSI() {
            indicator = new RSI(values, 5);
            Bombs(() => O.info(indicator[0] + ""), "uninitialized");
            addPoint(100);
            addPoint(101, 99.999900000099998);
            addPoint(104, 99.999900000099998);
            addPoint(102, 80.890730966157037);
            addPoint(105,  85.62414928479653);
            addPoint(100, 58.080739717036316);
            addPoint(103, 65.687948919418972);
            addPoint(108, 74.535176039717101);
            addPoint(110, 77.232548433453928);
            addPoint(103, 54.165313650320272);
        }

    }
}