using O=Q.Util.Objects;
using System;
using NUnit.Framework;

namespace Q.Spuds.Indicators {
    [TestFixture]
    public class TestStandardDeviation : SpudTestCase<double, double> {
        [Test]
        public void testStandardDeviation() {
            indicator = new StdDeviationOfSample(values);
            Bombs(() => O.info(indicator[0] + ""), "uninitialized");
            addPoint(3, 0.0);
            addPoint(7, Math.Sqrt(8));
            addPoint(7, Math.Sqrt(16.0/3.0));
            addPoint(19, Math.Sqrt(48));
        }


        [Test]
        public void testPopulationStandardDeviation() {
            indicator = new StdDeviationOfPopulation(values);
            Bombs(() => O.info(indicator[0] + ""), "uninitialized");
            addPoint(3, 0.0);
            addPoint(7, 2.0);
            addPoint(7);
            AlmostEqual(Math.Sqrt(32.0/9.0), indicator[0], 1e-6);
            addPoint(19, 6.0);
        }
    }
}