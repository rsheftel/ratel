using Q.Spuds.Core;

namespace Q.Spuds.Indicators {
    public class StdDeviation { 
        // abbreviation because this is how most people know it
        StdDeviation() {}
        // this class provides a hook  for choosing a type of standard deviation based on a param. 
        // if you know which you want, use the appropriate constructor directly.
        public static AggregatorSpud<double> maybeBiased(Spud<double> value, int windowSize, bool isBiased) {
            if (isBiased) return new StdDeviationOfPopulation(value, windowSize);
            return new StdDeviationOfSample(value, windowSize);
        }
    }
}