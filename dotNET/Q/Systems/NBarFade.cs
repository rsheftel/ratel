using System;
using System.Drawing;
using Q.Spuds.Core;
using Q.Spuds.Indicators;
using Q.Trading;

namespace Q.Systems {
    public class NBarFade : SymbolSystem {
        const int LOOKBACK_BARS = 1;

        readonly int atrLen;
        readonly int nDays;
        readonly double nATREntry;
        readonly double nATRStopStart;
        readonly double stopAfStep;
        readonly double stopAfMax;
        readonly double entryBarWindow;
        readonly bool closeBetter;
        readonly double riskDollars;
        
        internal readonly AverageTrueRangeEW atr;
        readonly Spud<double> entryHighestHigh;
        readonly Spud<double> entryLowestLow;
        readonly Spud<double> exitHighestHigh;
        readonly Spud<double> exitLowestLow;
        bool inConfirm;
        double confirmBarCount;
        long tradeSize;
        double longEntryPrice;
        double shortEntryPrice;
        double atrAtEntry;
        Direction setup;
        DynamicExit parabolicStop;
        readonly RootSpud<double> stopIndicator;

        public NBarFade(QREBridgeBase bridge, Symbol symbol): base(bridge, symbol) {
            atrLen = parameter<int>("ATRLen");
            nDays = parameter<int>("nDays");
            nATREntry = parameter<double>("nATRentry");
            stopAfStep = parameter<double>("stopAfStep");
            stopAfMax = parameter<double>("stopAfMax");
            entryBarWindow = parameter<double>("entryBarWindow");
            closeBetter = parameter<bool>("closeBetter");
            riskDollars = parameter<double>("riskDollars");

            atr = new AverageTrueRangeEW(bars, atrLen); // this should be a daily spud
            nATRStopStart = nATREntry * parameter<double>("exitATRmultiple");

            entryHighestHigh = bars.high.highest(nDays);
            entryLowestLow = bars.low.lowest(nDays);
            var halfNDays = (int) Math.Round(nDays*0.5, 0);
            exitHighestHigh = bars.high.highest(halfNDays);
            exitLowestLow = bars.low.lowest(halfNDays);

            inConfirm = false;

            //Plot methods
            parabolicStop = null;
            stopIndicator = new RootSpud<double>(bars.manager);
            addToPlot(stopIndicator,"ParabolicStop",Color.Red);
            addToPlot(entryHighestHigh, "entryHighestHigh", Color.LightBlue);
            addToPlot(entryLowestLow, "entryLowestLow", Color.Blue);
            addToPlot(exitHighestHigh, "exitHighestHigh", Color.Pink);
            addToPlot(exitLowestLow, "exitLowestLow", Color.Salmon);
            addToPlot(bars.close, "price", Color.Purple);
            addToPlot(atr,"ATR",Color.Blue,"ATRPane");
        }

        protected override void onNewTick(Bar partialBar, Tick tick){
        }

        protected override void onNewBar(){
            //Remove this code later
            if (parabolicStop != null)
                if (parabolicStop.positionClosed()) parabolicStop = null;
                else stopIndicator.set(parabolicStop);
            if (parabolicStop == null) stopIndicator.set(bars[0].close);
            //*****************************************************************

            //Setup from prior bar, now in confirm
            if (inConfirm) {
                confirmBarCount++;
                if (confirmBarCount <= entryBarWindow) {
                    placeEntryOrders();
                }
                else {
                    inConfirm = false;
                }
            }
            if (inConfirm) return;

            //Not in confirm from prior bar, so check for setup
            setup = tradeSetup();
            if (!hasPosition() && (setup != null)) {
                tradeSize = (long) Math.Max(Math.Round(riskDollars / ((nATRStopStart * atr) * bigPointValue()), 0), 1);
                atrAtEntry = atr;
                longEntryPrice = entryLowestLow[1] + atr * nATREntry;
                shortEntryPrice = entryHighestHigh[1] - atr * nATREntry;
                placeEntryOrders();
                inConfirm = true;
                confirmBarCount = 1;
            }
            
            //Currently in position, submit exit orders
            if (hasPosition())
                placeExitOrders();
        }

        protected override void onFilled(Position position, Trade trade){
            if (position.isEntry(trade)){
                //Only place the stop on the fill, the objective exit gets placed at close of bar
                var initialStopPrice = trade.price - (position.direction() * atrAtEntry * nATRStopStart);
                parabolicStop = new ParabolicStop(position,bars,initialStopPrice,stopAfStep,stopAfMax,LOOKBACK_BARS,"Parabolic Stop");
                addDynamicExit(parabolicStop, false);
            }
            inConfirm = false;
        }

        void placeExitOrders() {
            var objectiveExit = position().longShort(exitHighestHigh, exitLowestLow);
            placeOrder(position().exit("Objective Exit " + position().lOrS(), limit(objectiveExit), oneBar()));
        }

        void placeEntryOrders() {
            if (setup == Direction.LONG){
                placeOrder(buy("Enter Long",protectiveStop(longEntryPrice),tradeSize,oneBar()));
            }
            if (setup == Direction.SHORT){
                placeOrder(sell("Enter Short",protectiveStop(shortEntryPrice),tradeSize,oneBar()));
            }
        }

        internal Direction tradeSetup() {
            if (!hasPosition()) {
                if (bar.low < entryLowestLow[1]) {
                    if (closeBetter) {
                        return bar.close > entryLowestLow[1] ? Direction.LONG : null;
                    }
                    return Direction.LONG;
                }
                if (bar.high > entryHighestHigh[1]) {
                    if (closeBetter) {
                        return bar.close < entryHighestHigh[1] ? Direction.SHORT : null;
                    }
                    return Direction.SHORT;
                }
            }
            return null;
        }

        public override bool runOnClose() {
            return false;
        }

        protected override void onClose() {
        }

    }
}