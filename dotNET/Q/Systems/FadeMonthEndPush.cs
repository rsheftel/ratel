using System;
using java.util;
using Q.Spuds.Indicators;
using Q.Trading;
using util;

namespace Q.Systems {
    public class FadeMonthEndPush : SymbolSystem {
        readonly AverageTrueRangeEW atr;
        Date monthEnd;
        readonly string financialCenter;
        
        public FadeMonthEndPush(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {
            atr = new AverageTrueRangeEW(bars, parameter<int>("ATRLen"));
            financialCenter = tsdb.FinancialCenterTable.CENTER.name(parameter<int>("financialCalendar"));
        }

        public override bool runOnClose() {
            return true;
        }

        protected override void onFilled(Position position, Trade trade) {}
        protected override void onNewBar() {}
        protected override void onNewTick(Bar partialBar, Tick tick) {}

        protected override void onClose() {
            var barDate = Dates.midnight(jDate(bar.time));
            recacheMonthEnd(barDate);
            if (!barDate.Equals(monthEnd)) { exitExistingPosition(); return; }
            
            var days = parameter<int>("NDays");
            var currentMove = bar.close - bars[days].close;
            var currentMoveDistance = Math.Abs(currentMove);
            var targetMove = parameter<double>("Multiple") * atr;
            if (currentMoveDistance <= targetMove) return;
            if (currentMove > 0) placeOrder(symbol.sell("ShortEntry", market(), tradeSize(), oneBar()));
            if (currentMove < 0) placeOrder(symbol.buy("LongEntry", market(), tradeSize(), oneBar()));
        }

        protected long tradeSize() {
            var risk = parameter<double>("Risk");
            var tradeSize = (long) ((equity() * risk) / (atr * bigPointValue()));
            return tradeSize < 1 ? 1 : tradeSize;
        }

        protected void exitExistingPosition() {
            if (hasPosition()) placeOrder(position().exit("ExitPosition", market(), oneBar()));
        }

        protected void recacheMonthEnd(Date barDate) {
            if (monthEnd != null && !barDate.after(monthEnd)) return; // monthEnd is still valid.
            var firstOfCurrentMonth = Dates.date(Dates.year(barDate), Dates.monthNumber(barDate), 1);
            var firstOfNextMonth = Dates.monthsAhead(1, firstOfCurrentMonth);
            monthEnd = Dates.businessDaysAgo(1, firstOfNextMonth, financialCenter);
        }

        protected double equity() {
            var initialEquity = parameter<double>("InitEquity");
            return parameter<bool>("FixEquity") ? initialEquity : initialEquity + profit();
        }

    }
}

