using org.apache.commons.math.stat.descriptive;
using Q.Spuds.Core;
using Q.Util;

namespace Q.Spuds.Indicators {
    public class StatisticsSpud<T> : Spud<double> where T : UnivariateStatistic, new() {
        readonly int windowSize;
        readonly UnivariateStatistic statistic;
        readonly Spud<double> values;
        public static readonly int INFINITE = DescriptiveStatistics.INFINITE_WINDOW;

        public StatisticsSpud(Spud<double> values) : this(values, INFINITE) {}
        public StatisticsSpud(Spud<double> values, int windowSize) : this(values, windowSize, new T()) {}
        public StatisticsSpud(Spud<double> values, T statistic) : this(values, INFINITE, statistic) {}
        public StatisticsSpud(Spud<double> values, int windowSize, T statistic) : base(values.manager) {
            this.windowSize = windowSize;
            this.statistic = statistic;
            this.values = dependsOn(values);
        }

        protected override double calculate() {
            Bomb.unless(values.hasContent(), () => "use of uninitialized spud in StatisticsSpud");
            var array = windowSize == INFINITE ? values.toArray() : values.toArray(windowSize);
            return statistic.evaluate(array);
        }
    }
}