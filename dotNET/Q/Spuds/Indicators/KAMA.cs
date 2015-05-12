using System;
using NUnit.Framework;
using Q.Spuds.Core;

namespace Q.Spuds.Indicators {
    public class KAMA : Spud<double> {
        readonly Spud<double> values;
        readonly double decayFast;
        readonly double decaySlow;
        readonly int calcBars;
        readonly RootSpud<double> noise;
        readonly AggregatorSpud<double> noiseSum;

        public KAMA(Spud<double> values, double maFastLength, double maSlowLength, int calcBars) : base(values.manager) {
            this.values = dependsOn(values);
            decayFast = 2/(maFastLength+1);
            decaySlow = 2/(maSlowLength+1);
            this.calcBars = calcBars;
            noise = new RootSpud<double>(values.manager);
            noiseSum = new AggregatorSpud<double>(noise, sum, calcBars);
        }

        protected override double calculate() {
            if (values.count() > 1) noise.set(Math.Abs(values[0] - values[1]));
            if (values.count() <= calcBars) return values[0];

            var signal = Math.Abs(values[0] - values[calcBars]);
            var efficiencyRatio = signal / noiseSum;
            var smoothingCoefficient = Math.Pow(efficiencyRatio * (decayFast - decaySlow) + decaySlow, 2);
            return this[1] + smoothingCoefficient * (values[0] - this[1]);
        }

        public void requireSettings(double fast, double slow, int barsBack) {
            Assert.AreEqual(fast, decayFast);
            Assert.AreEqual(slow, decaySlow);
            Assert.AreEqual(barsBack, calcBars);
        }
    }
}
