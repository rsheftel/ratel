using Q.Spuds.Core;

namespace Q.Trading {
    public class ObjectiveExit : DynamicExit {
        protected readonly Spud<double> close;
        protected readonly double target;

        public ObjectiveExit(Position position, Spud<double> close, double target, string name) : base(position, name, LIMIT, close.manager) {
            this.close = dependsOn(close);
            this.target = target;
        }

        protected override double exitLevel() {
            return target;
        }

        protected override void cleanup() {
            close.removeChild(this);
        }
    }
}