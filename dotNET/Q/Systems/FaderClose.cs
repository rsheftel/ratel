using System;
using System.Drawing;
using Q.Spuds.Core;
using Q.Spuds.Indicators;
using Q.Trading;
using Q.Util;

namespace Q.Systems {
    public class FaderClose : SymbolSystem
    {
        internal readonly Spud<double> maRaw;
        internal readonly AggregatorSpud<double> priceStDev;
        internal readonly QRegression projectionSpud;
        private readonly int regressionProjBars;
        private readonly int regressionBars;
        private readonly double zEntry;
        private readonly double zExit;
        private readonly double minPnLMultTC;
        private readonly double stopMultiple;
        private readonly double riskDollars;
        private readonly double rSqrScale;
        private double stopPrice;
        internal readonly Spud<double> levelProjection;

        public FaderClose(QREBridgeBase bridge, Symbol symbol): base(bridge, symbol)
        {
            //Set up raw moving average
            var maLength = parameter<int>("maLength");
            switch(parameter<int>("maType")) {
                case 1:
                    maRaw = new Average(bars.close, maLength);
                    break;
                case 2:
                    maRaw = new KAMA(bars.close, 2, 30, maLength);
                    break;
                default:
                    Bomb.toss("Not valid maType");
                    break;
            }

            //Set up stdDev
            priceStDev = new StdDeviationOfSample(bars.close, parameter<int>("stDevLength"));
            
            //Set up regression
            regressionBars = parameter<int>("regressionBars");
            Bomb.when((parameter<int>("LeadBars") < regressionBars), () => "LeadBars cannot be less than regressionBars." + arguments());
            var count = new BarCounter(bars).transform(i => (double) i);
            projectionSpud = new QRegression(maRaw, count, regressionBars, false);
            regressionProjBars = parameter<int>("regressionProjectionBars");
            levelProjection = projectionSpud.transform(barsRegression => barsRegression.predict(count + regressionProjBars));

            //Set up other parameters
            zEntry = parameter<double>("ZEntry");
            zExit = parameter<double>("ZExit");
            minPnLMultTC = parameter<double>("minPnLMultTC");
            stopMultiple = parameter<double>("stopMultiple");
            riskDollars = parameter<double>("RiskDollars");
            rSqrScale = parameter<double>("rSqrScale");
            Bomb.when((rSqrScale != 0), () => "rSquare parameter not implemented yet.");

            addToPlot(maRaw,"maRaw",Color.Blue);
            addToPlot(levelProjection,"Projection", Color.Red);
            addToPlot(priceStDev,"StDev",Color.Green,"Support");
        }

        protected override void onFilled(Position position, Trade trade){
        }

        protected override void onNewBar(){
        }

        protected override void onNewTick(Bar partialBar, Tick tick){
        }

        public override bool runOnClose()
        {
            return true;
        }

        protected override void onClose() {
            //Trade Setup
            var setup = tradeSetup();
            //Sizing setup
            var alpha = Math.Abs(bars.close - levelProjection);
            var expectedPnL = alpha - (2 * slippage());
            //Confirmation
            var confirm = (expectedPnL > (minPnLMultTC * slippage()));

            //Entry trades
            if (confirm && setup != null) {
                var stopDistance = stopMultiple * alpha;
                var tradeSize = (long) Math.Max(Math.Round(riskDollars / (stopDistance * bigPointValue()),0),1);
                //Implement below when needed.
                //maRsqr = rsquare(barnumber, close, regressionBars);
                //tradeSize = tradeSize * (1-rSqrScale*(1 - maRsqr));

                if (setup.isLong()) {
                    placeOrder(symbol.buy("Enter Long", limit(bar.close), tradeSize, oneBar()));
                    stopPrice = bar.close - stopDistance;
                }
                if (setup.isShort()) {
                    placeOrder(symbol.sell("Enter Short", limit(bar.close), tradeSize, oneBar()));
                    stopPrice = bar.close + stopDistance;                    
                }
            }

            //Objective Exits
            if(hasPosition()) objectiveExits();

            //Stoploss Exits
            if (hasPosition()) stopLossExits();
        }

        void stopLossExits() {
            if (position().direction().isLong() & (bars.close < stopPrice))
                placeOrder(position().exit("LX Stop", limit(bar.close), oneBar()));
            else if (position().direction().isShort() & (bars.close > stopPrice))
                placeOrder(position().exit("SX Stop", limit(bar.close), oneBar()));
        }

        void objectiveExits() {
            if (position().direction().isLong()) {
                if (bars.close > (levelProjection - priceStDev * zExit)) 
                    placeOrder(position().exit("LX Objective", limit(bar.close), oneBar()));
            }
            else if (position().direction().isShort()) {
                if (bars.close < (levelProjection + priceStDev * zExit)) 
                    placeOrder(position().exit("SX Objective", limit(bar.close), oneBar()));
            }
        }

        internal Direction tradeSetup() {
            if (!hasPosition()) {
                if (bars.close < (levelProjection - priceStDev*zEntry)) return Direction.LONG;
                return bars.close > (levelProjection + priceStDev*zEntry) ? Direction.SHORT : null;
            }
            return null;
        }
    }
}