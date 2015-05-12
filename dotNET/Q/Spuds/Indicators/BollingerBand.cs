using NUnit.Framework;
using Q.Spuds.Core;

namespace Q.Spuds.Indicators {
    public class BollingerBand : Spud<double> {
        readonly int barsBack;
        readonly double deviations;
        readonly Average mean;
        readonly StdDeviationOfPopulation sd;

        public BollingerBand(Spud<double> values, int barsBack, double deviations) : base(values.manager) {
            this.barsBack = barsBack;
            this.deviations = deviations;
            mean = dependsOn(new Average(values, barsBack));
            sd = dependsOn(new StdDeviationOfPopulation(values, barsBack));
        }

        protected override double calculate() {
            return mean + deviations * sd;
        }

        public void requireSettings(int expectedBarsBack, double expectedDeviations) {
            Assert.AreEqual(expectedDeviations, deviations);
            Assert.AreEqual(expectedBarsBack, barsBack);
        }
    }
}
