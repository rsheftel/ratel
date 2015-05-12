namespace Q.Spuds.Core {
    internal class BarCounter : Spud<int> {
        readonly int start;
        readonly int barsStart;
        readonly SpudBase bars;

        public BarCounter(SpudBase bars) : this(bars, 0) {}
        public BarCounter(SpudBase bars, int start) : base(bars.manager) {
            this.start = start;
            this.bars = dependsOn(bars);
            barsStart = bars.count();
        }

        protected override int calculate() {
            return bars.count() - barsStart + start;
        }

    }
}