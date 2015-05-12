using Q.Spuds.Core;
using Q.Trading;

namespace Q.Spuds.Indicators {
    public class TDSetup : Spud<double> {
        readonly Spud<Bar> bars;
   

        public TDSetup(Spud<Bar> bars) : base(bars.manager) {
            this.bars = dependsOn(bars);
        }

        protected override double calculate() {
            if(bars.count() < 6) return 0;
            
            if(this[1] < 0) {
                if(bars[0].close < bars[4].close) return (this[1] - 1);
            }
            if (this[1] > 0) {
                if(bars[0].close > bars[4].close) return (this[1] + 1);
            }
            if(bullishPriceFlip()) return 1;
            if(bearishPriceFlip()) return -1;
            return 0;            
        }

        bool bullishPriceFlip() {
            return((bars[0].close > bars[4].close) && (bars[1].close < bars[5].close));
        }

        bool bearishPriceFlip() {
            return ((bars[0].close < bars[4].close) && (bars[1].close > bars[5].close));
        }
    }
}