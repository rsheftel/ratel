using Q.Spuds.Core;

namespace Q.Spuds.Indicators {
    public class Max : MinMax<double> { 
        public Max(Spud<double> values, int periods) : base(values, periods, MAX) {}
        public Max(Spud<double> values) : this(values, Window.INFINITE) {}
    }
}