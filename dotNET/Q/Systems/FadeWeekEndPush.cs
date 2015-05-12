using System;
using Q.Spuds.Indicators;
using Q.Trading;
using Q.Util;

namespace Q.Systems {
    public class FadeWeekEndPush : SymbolSystem {
        readonly AverageTrueRangeEW atr;
        readonly int nDays;
        readonly double multiple;
        readonly double risk;
        readonly double initialEquity;
        readonly bool fixEquity;
        readonly int exitDay;
        readonly double stopLossMultiple;

        public FadeWeekEndPush(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {
            atr = new AverageTrueRangeEW(bars, parameter<int>("ATRLen"));
            nDays = parameter<int>("NDays");
            multiple = parameter<double>("Multiple");
            risk = parameter<double>("Risk");
            initialEquity = parameter<double>("InitEquity");
            fixEquity = parameter<bool>("FixEquity");
            exitDay = parameter<int>("ExitDay");
            stopLossMultiple = parameter<double>("StopLossMultiple");
            bars.close.prepare();
        }

        public override bool runOnClose() {
            return true;
        }

        protected override void onFilled(Position position, Trade trade) {
            if(!trade.description.Contains("Entry")) return;
            var dollarStop = risk * equity();
            addDynamicExit(new DollarTrailingStop(position, bars.close, dollarStop, "TrailingStop", bridge), false); // true?
        }

        protected override void onNewBar() {}
        protected override void onNewTick(Bar partialBar, Tick tick) {}

        protected override void onClose() {
            if (hasPosition()) { setExits(bar.time); return; }

            if (bar.time.DayOfWeek != DayOfWeek.Friday) return;

            var currentMove = bar.close - bars[nDays].close;
            var currentMoveDistance = Math.Abs(currentMove);
            var targetMove = multiple * atr;
            if (currentMoveDistance <= targetMove) return;
            if (currentMove > 0) placeOrder(symbol.sell("ShortEntry", market(), tradeSize(), oneBar()));
            if (currentMove < 0) placeOrder(symbol.buy("LongEntry", market(), tradeSize(), oneBar()));
        }

        void setExits(DateTime barDate) {
            if((int) barDate.DayOfWeek == exitDay) {
                placeOrder(position().exit("ExitDayReached", market(), oneBar()));
                return;
            }
            LogC.note("this should be ObjectiveExit");
            if(position().pnlWithSlippage(bars.close, slippage(), arguments().runInNativeCurrency, bridge.fxRate(position().symbol)) > (multiple * atr * bigPointValue() * position().size)) 
                placeOrder(position().exit("TargetReturnReached", market(), oneBar()));
        }

        long tradeSize() {
            var tradeSize = (long) ((equity() * risk) / (atr * stopLossMultiple * bigPointValue()));
            return tradeSize < 1 ? 1 : tradeSize;
        }

        protected void exitExistingPosition() {
            if (hasPosition()) placeOrder(position().exit("ExitPosition", market(), oneBar()));
        }

        double equity() {
            return fixEquity ? initialEquity : initialEquity + profit();
        }

    }
}