using NUnit.Framework;
using O=Q.Util.Objects;

namespace Q.Spuds.Indicators {

    [TestFixture]
    public class TestEWMA : SpudTestCase<double, double> {
        [Test]
        public void testEWMA() {
            indicator = new EWMA(values, 1);
            Bombs(() => O.info(indicator[0] + ""), "uninitialized");
            addPoint(0, 0);
            addPoint(1, 0.5);
            addPoint(0, 0.25);
            addPoint(1, 0.625);
        }

        [Test]
        public void testEWMAJeff() {
            indicator = new EWMA(values, 3);
            Bombs(() => O.info(indicator[0] + ""), "uninitialized");
            addPoint(1, 1);
            addPoint(0, 0.79370052598409979);
            addPoint(0, 0.62996052494743671);
            addPoint(0, 0.50000000000000011);
        }

        [Test]
        public void testCalcTwice() {
            indicator = new EWMA(values, 1);
            addPoint(0, 0);
            values.set(0);
            AreEqual(0, indicator[0]);
        }
    }

    [TestFixture]
    public class TestEWSD : SpudTestCase<double, double> {
        [Test]
        public void testEWSD1() {
            indicator = new EWSD(values, 1);
            Bombs(() => O.info(indicator[0] + ""), "uninitialized");
            checkPoint(121.79, 0);
            checkPoint(122.35,0.197990);
            checkPoint(120.15,0.693109);
            checkPoint(120.78,0.503798);
        }
        [Test]
        public void testEWSD5() {
            indicator = new EWSD(values, 5);
            Bombs(() => O.info(indicator[0] + ""), "uninitialized");
            checkPoint(121.79, 0);
            checkPoint(122.35,0.175401);
            checkPoint(120.15,0.560791);
            checkPoint(120.78,0.588618);
        }

        protected void checkPoint(double val,double target) {
            addPoint(val);
            AlmostEqual(indicator, target,1e-6);
        }
    }

    public class TestEWZScore : TestEWSD {
        [Test]
        public void testZScore5() {
            indicator = new EWZScore(values, 5);
            Bombs(() => O.info(indicator[0] + ""), "uninitialized");
            checkPoint(121.79,0);
            checkPoint(122.35,2.7793933);
            checkPoint(120.15,-2.658407);
            checkPoint(120.78,-1.273116);
        }
        [Test]
        public void testZScore1() {
            indicator = new EWZScore(values,1);
            Bombs(() => O.info(indicator[0] + ""), "uninitialized");
            checkPoint(121.79, 0);
            checkPoint(122.35,1.414214);
            checkPoint(120.15,-1.385064);
            checkPoint(120.78,-0.327512);
        }
    }
}