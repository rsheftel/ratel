using Q.Spuds.Core;

namespace Q.Trading {
    public class PriceTrailingStop : DynamicExit {
        readonly double stopPrice;
        readonly SpudBase close;

        public PriceTrailingStop(Position position, SpudBase close, double stopPrice, string name) 
            : base(position, name, STOP, close.manager) 
        {
            this.stopPrice = stopPrice;
            this.close = dependsOn(close);
        }

        protected override double exitLevel() {
            return stopPrice;
        }

        protected override void cleanup() {
            close.removeChild(this);
        }
    }
}