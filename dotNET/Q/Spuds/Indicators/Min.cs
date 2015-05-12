using Q.Spuds.Core;

namespace Q.Spuds.Indicators {
    public class Min : MinMax<double> { 
        public Min(Spud<double> values, int periods) : base(values, periods, MIN) {}
        public Min(Spud<double> values) : this(values, Window.INFINITE) {}
    }
}