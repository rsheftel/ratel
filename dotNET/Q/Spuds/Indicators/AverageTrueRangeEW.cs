using Q.Spuds.Core;
using Q.Trading;

namespace Q.Spuds.Indicators {
    public class AverageTrueRangeEW : EWMA {
        public AverageTrueRangeEW(Spud<Bar> bars, int halfLife) : base(new TrueRange(bars), halfLife) {}
    }
}