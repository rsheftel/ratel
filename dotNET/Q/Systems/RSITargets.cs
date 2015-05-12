using Q.Spuds.Indicators;
using Q.Trading;

namespace Q.Systems {
    public class RSITargets : SymbolSystem {
        readonly AverageTrueRangeEW atr;
        readonly RSI rsiSpud;
        readonly double entryLevel;
        readonly double exitLevel;
        bool stoppedFlag;
        int lastDirection;
        double lastStop;

        public RSITargets(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {
            atr = new AverageTrueRangeEW(bars, parameter<int>("ATRLen"));
            rsiSpud = new RSI(bars.close, parameter<int>("HalfLife"));
            entryLevel = parameter<double>("EntryLevel");
            exitLevel = parameter<double>("ExitLevel");
            stoppedFlag = false;
            lastDirection = 0;
            lastStop = 0;
        }

        public override bool runOnClose(){
            return true;
        }

        protected override void onFilled(Position position, Trade trade) {
            var description = trade.description;
            if (description.Contains("Entry")) {
                lastStop = trade.price - trade.direction * (stopLoss() / (bigPointValue() * trade.size));
                return;
            }

            if(description.Contains("Stop")) {
                stoppedFlag = true;
                lastDirection = description.Contains("Sell") ? 1 : -1;
            }

            lastStop = 0;
        }

        protected override void onNewTick(Bar partialBar, Tick tick) {}
        protected override void onClose() {
            if(hasPosition()) placeExits();
            else placeEntries();
        }

        protected override void onNewBar() {}

        protected void placeEntries() {
            if (stoppedFlag && lastDirection == 1 && rsiSpud[0] < 50 ) return;
            if (stoppedFlag && lastDirection == -1 && rsiSpud[0] > 50) return;
            if (stoppedFlag) stoppedFlag = false;
            var tradeSize = (long) (stopLoss() / (atr * parameter<double>("nATR") * bigPointValue())) ;
            if (rsiSpud[0] < entryLevel) placeOrder(symbol.buy("LongEntry", market(), tradeSize, oneBar()));
            if (rsiSpud[0] > (100 - entryLevel)) placeOrder(symbol.sell("ShortEntry", market(), tradeSize, oneBar()));
        }

        protected void placeExits() {
            if (position().direction().isLong() && rsiSpud[0] > exitLevel) {
                placeOrder(position().exit("ExitLong", market(), oneBar()));
                return;
            }

            if (position().direction().isShort() && rsiSpud[0] < (100 - exitLevel)) {
                placeOrder(position().exit("ExitShort", market(), oneBar()));
                return;
            }
            var stopLevel = bar.close - position().direction() * (stopLoss() / (bigPointValue() * position().size));

            if((position().direction() * (lastStop - stopLevel)) > 0) stopLevel = lastStop;
            else lastStop = stopLevel;
            

            placeOrder(position().exit(position().longShort("SellStopLoss", "CoverStopLoss"), new Stop(stopLevel), oneBar()));   
        }

        protected double stopLoss() {
            return equity() * parameter<double>("Risk");
        }

        protected double equity() {
            var initialEquity = parameter<double>("InitEquity");
            return parameter<bool>("FixEquity") ? initialEquity : initialEquity + profit();
        }
    }
}