using System;
using Q.Spuds.Core;
using Q.Spuds.Indicators;
using Q.Trading;

namespace Q.Systems {
    public class FadeWeekEndPushClose : SymbolSystem {
        readonly AverageTrueRangeEW atr;
        readonly int nDays;
        readonly double multiple;
        readonly double risk;
        readonly double initialEquity;
        readonly bool fixEquity;
        readonly int exitDay;
        readonly double stopLossMultiple;
        double currentStopLevel;
        int currentBarsInTrade;

        public FadeWeekEndPushClose(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {
            atr = new AverageTrueRangeEW(bars, parameter<int>("ATRLen"));
            nDays = parameter<int>("NDays");
            multiple = parameter<double>("Multiple");
            risk = parameter<double>("Risk");
            initialEquity = parameter<double>("InitEquity");
            fixEquity = parameter<bool>("FixEquity");
            exitDay = parameter<int>("ExitDay");
            stopLossMultiple = parameter<double>("StopLossMultiple");
            currentStopLevel = 0;
            currentBarsInTrade = 0;
            bars.close.prepare();
        }

        public override bool runOnClose() {
            return true;
        }

        protected override void onFilled(Position position, Trade trade) {
            if(!trade.description.Contains("Entry")) return;
            currentBarsInTrade = 0;
            var dollarStop = risk * equity();
            setStopLevel(position, bars.close, dollarStop);
        }

        void setStopLevel(Position position, ComparableSpud<double> close, double dollarStop) {
            currentStopLevel = close - position.direction() * (dollarStop / (position.size * bigPointValue()));
        }

        protected override void onNewBar() {}
        protected override void onNewTick(Bar partialBar, Tick tick) {}

        protected override void onClose() {
            if (hasPosition()) {
                currentBarsInTrade++;
                setExits(); return; }

            if (bar.time.DayOfWeek != DayOfWeek.Friday) return;

            var currentMove = bar.close - bars[nDays].close;
            var currentMoveDistance = Math.Abs(currentMove);
            var targetMove = multiple * atr;
            if (currentMoveDistance <= targetMove) return;
            if (currentMove > 0) placeOrder(symbol.sell("ShortEntry", market(), tradeSize(), oneBar()));
            if (currentMove < 0) placeOrder(symbol.buy("LongEntry", market(), tradeSize(), oneBar()));
        }

        protected void setExits() {
            if(currentBarsInTrade == exitDay) {
                placeOrder(position().exit("ExitDayReached", market(), oneBar()));
                return;
            }
            if(position().pnlWithSlippage(bars.close, slippage(), arguments().runInNativeCurrency, bridge.fxRate(position().symbol)) > (multiple * atr * bigPointValue() * position().size)) 
                placeOrder(position().exit("TargetReturnReached", market(), oneBar()));
            if(position().direction().isLong() && bars.close <= currentStopLevel) 
                placeOrder(position().exit("TrailingStopClose", market(), oneBar()));
            if(position().direction().isShort() && bars.close >= currentStopLevel)
                placeOrder(position().exit("TrailingStopClose", market(), oneBar()));
        }

        protected long tradeSize() {
            var tradeSize = (long) ((equity() * risk) / (atr * stopLossMultiple * bigPointValue()));
            return tradeSize < 1 ? 1 : tradeSize;
        }

        protected void exitExistingPosition() {
            if (hasPosition()) placeOrder(position().exit("ExitPosition", market(), oneBar()));
        }


        protected double equity() {
            return fixEquity ? initialEquity : initialEquity + profit();
        }

    }
}