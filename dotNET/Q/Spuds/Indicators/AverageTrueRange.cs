using Q.Spuds.Core;
using Q.Trading;

namespace Q.Spuds.Indicators {
    public class AverageTrueRange : AggregatorSpud<double> {
        public AverageTrueRange(Spud<Bar> bars, int i) : base(new TrueRange(bars), average, i) {}
    }
}