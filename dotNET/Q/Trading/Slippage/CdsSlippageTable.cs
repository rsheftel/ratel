using Q.Util;

namespace Q.Trading.Slippage {
    public class CdsSlippageTable {
        public static double slippageInBps(double bp) {
            if(bp < 0) return 3.0;
            if(bp < 100) return interpolate(bp, 0, 100, 3.0, 10.0);
            if(bp < 250) return (interpolate(bp, 100, 250, 10.0, 15.0));
            if(bp < 500) return interpolate(bp, 250, 500, 15.0, 20.0);
            if(bp < 750) return interpolate(bp, 500, 750, 20.0, 25.0);
            return bp >= 1000 ? 30.0 : interpolate(bp, 750, 1000, 25.0, 30.0);
        }

        static double interpolate(double indexSpread, double lowX, double highX, double lowY, double highY) {
            Bomb.when(indexSpread < lowX || indexSpread > highX, () => "indexValue is outside of " + lowX + " to" + highX + " range");
            return ((indexSpread - lowX) / (highX - lowX)) * (highY - lowY) + lowY;
        }
    }
}