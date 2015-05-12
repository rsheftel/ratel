
using System.Drawing;
using Q.Spuds.Core;
using Q.Spuds.Indicators;
using Q.Trading;
using System;
using Q.Util;

namespace Q.Systems {    
    public class FXCommodityClose : TrendingMABase {
        readonly Spud<double> maFX;
        readonly bool currencyReversed;
        readonly int maxBarsHeld;

        public FXCommodityClose(QREBridgeBase bridge, Symbol symbol) 
            : base(bridge, symbol, bars => commoditySignal(bridge.arguments().parameters.get<int>("signal"), bars)) {
           maFX = new Average(bars.close, maDays);
           currencyReversed = symbol.name.StartsWith("USD");
            maxBarsHeld = parameter<int>("MaxBarsHeld");
            addToPlot(upperBand, "UpperBand", Color.Blue,"Commodity");
            addToPlot(lowerBand,"LowerBand", Color.Blue,"Commodity");
            addToPlot(ma, "commodMovingAverage", Color.Red,"Commodity");
        }

        protected override void onFilled(Position position, Trade trade) {
           cancelAllOrders();
           if (position.isEntry(trade))
                addDynamicExit(new DollarTrailingStop(position, bars.close, riskDollars, "Exit "+lOrS()+" StopOut", bridge), true);
        }
           
        protected override void onNewBar() {
        }
        
        protected override void onNewTick(Bar partialBar, Tick tick) {
        }

        protected override void onClose() {
            if (hasPosition()) placeExits(); 
            else placeEntries();
        }
        
        protected override void placeEntries() {
            var maSlope = (maFX - maFX[1]);
            if (currencyReversed) maSlope = -maSlope;
            var upperBreak=false;
            var lowerBreak=false;
            if (ma > upperBand && maSlope >= 0) upperBreak = true;
            if (ma < lowerBand && maSlope <= 0) lowerBreak = true;
            if (!upperBreak && !lowerBreak) return;
            var description = (upperBreak ? "Upper" : "Lower") + " Range Break";
            if (upperBreak  == currencyReversed) 
                placeOrder(symbol.sell(description, protectiveStop(bar.close), tradeSize(), oneBar())); 
            else 
                placeOrder(symbol.buy(description, protectiveStop(bar.close), tradeSize(), oneBar()));
        }
         
        protected override void placeExits() {
            var reversalExit = Math.Round((upperBand + lowerBand) / 2, 4);
            var residual = (ma - reversalExit) * position().direction();
            if (position().barsHeld() > maxBarsHeld) 
                placeOrder(position().exit("Exit " + lOrS() + " MaxBars", protectiveStop(bar.close), oneBar()));
            if ((residual < 0 && !currencyReversed)||(residual > 0 && currencyReversed)) 
                placeOrder(position().exit("Exit " + lOrS() + " Reversal", protectiveStop(bar.close), oneBar()));
        }
         
        public override bool runOnClose() {
            return true;
        }
        
        static Spud<double> commoditySignal(int signal, BarSpud bars) {
            Converter<string, Spud<double>> spud = name => {
                var result = new Symbol(name).doubles(bars);
                result.allowStaleTicks();
                return result;
            };
            switch (signal) {
                case 1: return spud("HG.3N");
                case 2: return spud("GC.3N");
                case 3: return spud("NG.3N");
                case 4: return spud("CL.3N");
            }
            throw Bomb.toss("Unknown Signal Type"+signal);
        }
       }

}

    
