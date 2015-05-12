using Q.Spuds.Core;

namespace Q.Spuds.Indicators {
    public class Average : AggregatorSpud<double> {
        public Average(Spud<double> values, int periods) : base(values, average, periods) {}
        public Average(Spud<double> values) : this(values, Window.INFINITE) {}
    }
}