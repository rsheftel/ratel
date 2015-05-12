using System;
using Q.Spuds.Core;

namespace Q.Spuds.Indicators {
    internal class RSI : Spud<double> {
        readonly Spud<double> values;
        readonly EWMA upAverage;
        readonly EWMA dnAverage;
        readonly RootSpud<double> upChanges;
        readonly RootSpud<double> dnChanges;

        public RSI(Spud<double> values, int halfLife) : base(values.manager) {
            this.values = dependsOn(values);
            upChanges = new RootSpud<double>(manager);
            dnChanges = new RootSpud<double>(manager);
            upAverage = new EWMA(upChanges, halfLife);
            dnAverage = new EWMA(dnChanges, halfLife);
        }

        protected override double calculate() {
            if(values.count() == 1) return double.NaN;
            var changes = values[0] - values[1];
            upChanges.set(Math.Max(0.0, changes));
            dnChanges.set(-Math.Min(0.0, changes));
            double rs;
            if(dnAverage == 0) rs = 1000000.0;
            else rs = upAverage / dnAverage;

            return 100.0 - (100.0 / (1.0 + rs));
        }
    }
}