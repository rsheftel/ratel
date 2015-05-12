using Q.Spuds.Core;

namespace Q.Spuds.Indicators {
    public class Sum : AggregatorSpud<double> {
        public Sum(Spud<double> values, int periods) : base(values, sum, periods) {}
        public Sum(Spud<double> values) : this(values, Window.INFINITE) {}
    }
}