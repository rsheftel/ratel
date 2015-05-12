using Q.Spuds.Core;

namespace Q.Spuds.Indicators {
    public class StdDeviationOfSample : AggregatorSpud<double> { // long name so StdDeviation variations sort together
        public StdDeviationOfSample(Spud<double> values, int periods) : base(values, standardDeviation, periods) {}
        public StdDeviationOfSample(Spud<double> values) : this(values, Window.INFINITE) {}
    }
}