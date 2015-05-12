using NUnit.Framework;
using Q.Spuds.Core;

namespace Q.Spuds.Indicators {
    [TestFixture]
    public class TestEWAcf : SpudTestCase<double, double> {
        [Test]
        public void testEWAcf() {
            // this is a little weird because we used data from TS as the test, and the implementation there is a little odd
            addPoint(0);
            addPoint(0);
            addPoint(0);
            addPoint(-0.219444265);
            indicator = new EWAcf(values, 10, 3);
            addPoint(-0.516886204, 0);
            addPoint(0.046609311, 0);
            addPoint(1.039525646, -12.01053709);
            addPoint(1.311982378, -25.50471467);
            addPoint(0.329293064, -24.22784361);
            addPoint(-0.502758285, -37.8175819);
            addPoint(-1.049661947, -60.31325276);
            addPoint(-0.133007786, -61.141188);
            addPoint(0.201625133, -63.16854652);
            addPoint(-0.255056821, -54.5576458);
            addPoint(0.002237499, -54.56645802);
            addPoint(-0.235107337, -55.10909658);
        }

        protected override void addPoint(double newValue, double newMean) {
            addPoint(newValue);
            AlmostEqual(newMean, indicator[0], 1e-7);
        }

    }

    public class EWAcf : Spud<double> {
        readonly EWMA variance;
        readonly EWMA covariance;

        public EWAcf(Spud<double> values, double halfLife, int lag) : base(values.manager) {
            variance = dependsOn(new EWMA(new Product(values, values), halfLife));
            covariance = dependsOn(new EWMA(new Product(values, values.lagged(lag)), halfLife));
        }

        protected override double calculate() {
            return 100 * covariance / variance;
        }

        public class Product : Spud<double> {
            readonly Spud<double> multiplier;
            readonly Spud<double> multiplicand;

            public Product(Spud<double> multiplier, Spud<double> multiplicand) : base(multiplier.manager) {
                this.multiplier = dependsOn(multiplier);
                this.multiplicand = dependsOn(multiplicand);
            }

            protected override double calculate() {
                return multiplier * multiplicand;
            }
        }
    }
}