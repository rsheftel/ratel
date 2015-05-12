using System;
using Q.Spuds.Core;
using Q.Util;

namespace Q.Trading {
    public class ParabolicStop : DynamicExit {
        readonly RootSpud<double> af; // acceleration factor
        readonly double initialStopPrice;
        readonly double afStep;
        readonly double afMax;
        readonly ComparableSpud<double> extreme;
        readonly Spud<double> maxExtreme;
        readonly Spud<double> recentOppositeExtreme;
        readonly Spud<int> numBars;
        readonly BarSpud bars;
        readonly ComparableSpud<double> opposite;

        public ParabolicStop(
            Position position, BarSpud bars, double initialStopPrice, double afStep, 
            double afMax, int lookbackBars, string name
        ) : base(position, name, PROTECTIVE_STOP, bars.manager) {
            this.initialStopPrice = initialStopPrice;
            this.afStep = afStep;
            this.afMax = afMax;
            this.bars = bars;
            af = dependsOn(new RootSpud<double>(manager));
            af.set(afStep);
            
            extreme = dependsOn(position.longShort(bars.high, bars.low));
            var extremeSide = position.longShort(MinMax<double>.MAX, MinMax<double>.MIN);
            maxExtreme = dependsOn(extreme.minMax(extremeSide));
            opposite = position.direction().isLong() ? bars.low : bars.high;
            recentOppositeExtreme = dependsOn(position.direction().isLong() ? opposite.lowest(lookbackBars) : opposite.highest(lookbackBars));
            numBars = dependsOn(new BarCounter(bars));
        }

        protected override double exitLevel() {
            Bomb.when(position.isClosed(), () => "position closed without cleaning up parabolic stop!");
            if (numBars == 0) return initialStopPrice;
            if (maxExtreme.changed()) 
                af.set(Math.Min(af[1] + afStep, afMax));
            return capped(this[1] + af * (extreme - this[1])); 
        }

        protected override void cleanup() {
            extreme.removeChild(maxExtreme);
            bars.removeChild(numBars);
            af.removeChild(this);
            extreme.removeChild(this);
            maxExtreme.removeChild(this);
            recentOppositeExtreme.removeChild(this);
            opposite.removeChild(recentOppositeExtreme);
            
            numBars.removeChild(this);
            manager.remove(af);
            manager.remove(maxExtreme);
            manager.remove(recentOppositeExtreme);
            manager.remove(numBars);
        }

        delegate double Chooser(double a, double b);

        double capped(double stopPrice) {
            return position.longShort<Chooser>(Math.Min, Math.Max)(stopPrice, recentOppositeExtreme);
        }
    }
}