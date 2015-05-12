using Q.Spuds.Core;

namespace Q.Spuds.Indicators {
    public class StdDeviationOfPopulation : AggregatorSpud<double> { // long name so StdDeviation variations sort together
        public StdDeviationOfPopulation(Spud<double> values, int periods) : base(values, populationStandardDeviation, periods) {}
        public StdDeviationOfPopulation(Spud<double> values) : this(values, Window.INFINITE) {}
    }
}