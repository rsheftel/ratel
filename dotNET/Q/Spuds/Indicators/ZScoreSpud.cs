using Q.Spuds.Core;

namespace Q.Spuds.Indicators {
    public class ZScoreSpud : Spud<double> {
        readonly Spud<double> value;
        readonly AggregatorSpud<double> mean;
        readonly AggregatorSpud<double> sd;

        public ZScoreSpud(Spud<double> value) : this(value, Window.INFINITE,false) {}

        public ZScoreSpud(Spud<double> value, int windowSize) : this(value,windowSize,false) {}

        public ZScoreSpud(Spud<double> value, bool isBiased) : this(value, Window.INFINITE, isBiased) {}        

        public ZScoreSpud(Spud<double> value, int windowSize, bool isBiased) : base(value.manager) {
            this.value = dependsOn(value);
            mean = dependsOn(new Average(value, windowSize));
            sd = dependsOn(StdDeviation.maybeBiased(value, windowSize, isBiased));
        }

        protected override double calculate() {
            return safeDivide(0, value - mean, sd);            
        }

        public double standardDeviation() {
            return sd[0];
        }
    }
}