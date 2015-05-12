using System;
using System.Drawing;
using Q.Spuds.Core;
using Q.Spuds.Indicators;
using Q.Trading;
using Q.Util;

namespace Q.Systems {
    public class SwingMA : SymbolSystem {

        readonly double entryATRMargin;
        readonly double exitATRMargin;
        readonly int stopATRs;
        readonly double riskDollars;
        internal readonly AverageTrueRangeEW atr;
        readonly Spud<double> maSlow;
        readonly Spud<double> maFast;

        public SwingMA(QREBridgeBase bridge, Symbol symbol): base(bridge, symbol) {
            entryATRMargin = parameter<double>("EntryATRMargin");
            exitATRMargin = 2 * entryATRMargin;
            stopATRs = parameter<int>("StopATRs");
            riskDollars = parameter<double>("RiskDollars");

            atr = new AverageTrueRangeEW(bars, parameter<int>("ATRLen"));
            maSlow = new Average(bars.close, parameter<int>("MASlow"));
            //Set up raw moving average
            switch(parameter<int>("MAType")) {
                case 1:
                    maSlow = new Average(bars.close, parameter<int>("MASlow"));
                    maFast = new Average(bars.close, parameter<int>("MAFast"));
                    break;
                case 2:
                    maSlow = new KAMA(bars.close, 2, 30, parameter<int>("MASlow"));
                    maFast = new KAMA(bars.close, 2, 30, parameter<int>("MAFast"));
                    break;
                default:
                    Bomb.toss("Not valid MAType");
                    break;
            }

            addToPlot(maSlow,"maSlow",Color.Blue);
            addToPlot(maFast,"maFast",Color.Red);
            addToPlot(atr,"ATR",Color.Blue,"ATRPane");
        }

        protected override void onNewTick(Bar partialBar, Tick tick){
        }

        protected override void onNewBar(){
            if (hasPosition()) {
                placeExitOrders();
            } else placeEntryOrders();
        }

        protected override void onFilled(Position position, Trade trade){
            if (!position.isEntry(trade)) return;
            var stopPrice = trade.price + (-position.direction() * atr * stopATRs);
            addDynamicExit(new PriceTrailingStop(position, bars, stopPrice, "Money Stop " + position.lOrS()), false);
        }

        void placeExitOrders() {
            var openPx = bar.close;
            if ((position().direction().isLong()) && ((bar.close < maSlow) || (bar.close > maFast))) {
                placeOrder( position().exit("Price Exit " + position().lOrS(), protectiveStop(openPx - exitATRMargin * atr), oneBar()));
            }
            if ((position().direction().isShort()) && ((bar.close > maSlow) || (bar.close < maFast))) {
                placeOrder(position().exit("Price Exit " + position().lOrS(), protectiveStop(openPx + exitATRMargin * atr), oneBar()));
            }
        }

        void placeEntryOrders() {
            var openPx = bar.close;
            var tradeSize = (long) Math.Max(Math.Round(riskDollars / (atr * stopATRs * bigPointValue()),0),1);

            if ((bar.close >= maSlow) && (bar.close < maFast)) {
                placeOrder(symbol.buy("Swing Enter L", protectiveStop(openPx + (entryATRMargin * atr)), tradeSize, oneBar()));
            }
            if ((bar.close <= maSlow) && (bar.close > maFast)) {
                placeOrder(symbol.sell("Swing Enter S", protectiveStop(openPx - (entryATRMargin * atr)), tradeSize, oneBar()));
            }
        }

        public override bool runOnClose() {
            return false;
        }

        protected override void onClose() {
        }
    }
}