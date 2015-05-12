using Q.Spuds.Core;

namespace Q.Trading {
    public class MaxBarsExit : DynamicExit {
        readonly Spud<double> close;
        readonly int maxBars;
        readonly double limitOffset;

        public MaxBarsExit(Position position, Spud<double> close, int maxBars, double limitOffset, string description) : base(position, description, LIMIT, close.manager) {
            this.close = dependsOn(close);
            this.maxBars = maxBars;
            this.limitOffset = limitOffset;
        }

        protected override double exitLevel() {
            return close - position.direction() * limitOffset;
        }

        protected override void cleanup() {
            close.removeChild(this);
        }

        public override void placeOrder(System system) {
            if(position.barsHeld() >= maxBars)
                base.placeOrder(system);
        }
    }
}