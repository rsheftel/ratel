using System;
using System.Drawing;
using Q.Spuds.Core;
using Q.Spuds.Indicators;
using Q.Trading;

namespace Q.Systems {
    public abstract class NDayBreakBase : SymbolSystem {
        internal readonly AverageTrueRangeEW atr;
        protected double lastTradeATR;
        protected readonly Spud<double> breakOutHigh;
        protected readonly Spud<double> breakOutLow;
        protected readonly Spud<double> breakDownHigh;
        protected readonly Spud<double> breakDownLow;
        readonly double risk;
        readonly int breakDays;
        protected bool stopNeedsRecalc;
        double currentStopLevel;

        protected NDayBreakBase(QREBridgeBase bridge, Symbol symbol, 
            Converter<BarSpud, ComparableSpud<double>> high, 
            Converter<BarSpud, ComparableSpud<double>> low
        ) : base(bridge, symbol) {
            atr = new AverageTrueRangeEW(bars, parameter<int>("ATRLen"));
            
            breakDays = parameter<int>("BreakDays") - 1;
            breakOutHigh = high(bars).highest(breakDays);
            breakOutLow = low(bars).lowest(breakDays);
            breakDownHigh = high(bars).highest(breakDays / 2);
            breakDownLow = low(bars).lowest(breakDays / 2);

            risk = parameter<double>("Risk");

            addToPlot(breakOutHigh, "breakout high", Color.Red);
            addToPlot(breakOutLow, "breakout low", Color.Blue);
            addToPlot(breakDownHigh, "breakdown high", Color.DeepPink);
            addToPlot(breakDownLow, "breakdown low", Color.DeepSkyBlue);
        }

        protected override void onFilled(Position position, Trade trade) {
            lastTradeATR = atr;
            stopNeedsRecalc = true;
        }

        protected long tradeSize() {
            var tradeSize = (long) ((equity() * risk) / (stopPoints() * bigPointValue()));
            return tradeSize < 1 ? 1 : tradeSize;
        }

        protected double stopLevel(long tradeSize) {
            if (!stopNeedsRecalc) return currentStopLevel;

            var offset = stopPoints();
            if (notScaled() && tradeSize == 1)
                offset = (equity() * risk) / (tradeSize * bigPointValue());
            currentStopLevel = position().lastTrade().price - position().direction() * offset;
            stopNeedsRecalc = false;
            
            return currentStopLevel;
        }

        bool notScaled() {
            return positions().Count == 0 || position().trades().Count == 1;
        }

        protected double scaleUpLevel() {
            return position().lastTrade().price + position().direction() * parameter<double>("upATR") * atr;
        }

        protected string lOrS() {
            return position().lOrS();
        }

        double stopPoints() {
            return parameter<int>("nATR") * atr;
        }

        double equity() {
            var initialEquity = parameter<double>("InitEquity");
            return parameter<bool>("FixEquity") ? initialEquity : initialEquity + profit();
        }

        protected void placeInitialOrders() {
            placeLongEntry();
            placeShortEntry();
        }

        protected void placeShortEntry() {
            placeOrder(sell("BreakOut S", protectiveStop(breakOutLow), tradeSize(), oneBar()));
        }

        protected void placeLongEntry() {
            placeOrder(buy("BreakOut L", protectiveStop(breakOutHigh), tradeSize(), oneBar()));
        }

        protected void placeStopsAndPyramids() {
            var size = tradeSize();
            if (position().trades().Count < parameter<int>("MaxPyramid")) {
                placeOrder(position().scaleUp("ScaleUp " + lOrS(), protectiveStop(scaleUpLevel()), size, oneBar()));
            }
            var breakDown = position().longShort(breakDownLow, breakDownHigh);
            var moneyStop = stopLevel(size);
            var d = position().direction();
            placeOrder(position().exit("Stop E" + lOrS(), protectiveStop(moneyStop), oneBar()));
            placeOrder(position().exit("BreakDn E" + lOrS(), protectiveStop(breakDown), oneBar()));
            if (atr > 2 * lastTradeATR) 
                placeOrder(position().exit("Vol Double E" + lOrS(), limit(bar.close - d * atr), oneBar()));
        }
    }
}