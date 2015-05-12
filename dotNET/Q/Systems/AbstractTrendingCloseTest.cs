using Q.Trading;

namespace Q.Systems {
    public abstract class AbstractTrendingCloseTest<T> : OneSymbolSystemTest<T> where T : TrendingMABase {
        protected void requireLongEntry(double longEntry, long size) {
            hasOrders(
                symbol().buy("Upper Range Break", protectiveStop(longEntry), size, oneBar())
                );
        }

        protected void requireLongExit(double reversalLevel, double stopLevel, long size) {
            hasOrders(
                position().exit("Exit L StopOut", stop(stopLevel), oneBar()),
                position().exit("Exit L Reversal", protectiveStop(reversalLevel), oneBar()));
        }

        protected void requireLongStop(double stopLevel) {
            hasOrders(
                position().exit("Exit L StopOut", stop(stopLevel), oneBar())
                );
        }

        protected void requireShortEntry(double shortEntry, long size) {
            hasOrders(
                symbol().sell("Lower Range Break", protectiveStop(shortEntry), size, oneBar())
                );
        }

        protected void requireShortStop(double stopLevel) {
            hasOrders(
                position().exit("Exit S StopOut", stop(stopLevel), oneBar())
                );
        }

        protected void requireShortExit(double reversalLevel, double stopLevel, long size) {
            hasOrders(
                position().exit("Exit S StopOut", stop(stopLevel), oneBar()),
                position().exit("Exit S Reversal", protectiveStop(reversalLevel), oneBar())
                );
        }

        protected override Symbol initializeSymbol() {
            return new Symbol("RE.TEST.TY.1C", 10000);
        }
    }
}