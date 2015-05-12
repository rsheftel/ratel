using System;
using Q.Spuds.Core;
using Q.Trading;

namespace Q.Spuds.Indicators {
    public class TrueRange : Spud<double> {
        readonly Spud<Bar> bars;

        public TrueRange(Spud<Bar> bars) : base(bars.manager) {
            this.bars = dependsOn(bars);
        }

        protected override double calculate() {
            if(bars.count() == 1)
                return bars[0].range();
            var prevClose = bars[1].close;
            var max = Math.Max(bars[0].high, prevClose);
            var min = Math.Min(bars[0].low, prevClose);
            return max - min;
        }

    }
}