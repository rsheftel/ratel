using System;
using System.Drawing;
using System.Text.RegularExpressions;
using Q.Spuds.Core;
using Q.Spuds.Indicators;
using Q.Trading;
using Q.Util;

namespace Q.Systems {
    public class CouponSwap : SymbolSystem {
        internal readonly SymbolSpud<double> modelPrice;
        internal readonly SymbolSpud<double> actualPrice;
        internal readonly SymbolSpud<double> rollDecimal;
        private readonly Spud<double> rollTicks;
        private readonly RichCheapSpud richCheap;
        private readonly CrossOverSpud<double> longEntryCross;
        private readonly CrossOverSpud<double> shortEntryCross;
        private readonly CrossOverSpud<double> longExitCross;
        private readonly CrossOverSpud<double> shortExitCross;
        private readonly long tradeSize;
        private readonly long maxSize;
        private readonly double entryTicks;
        private readonly double stopTicks;
        private readonly EWMA longMA;
        readonly double rollCutOff;
        readonly double rollCutOffMargin;
        private readonly CrossOverSpud<double> longRollCross;
        private readonly CrossOverSpud<double> shortRollCross;

        public CouponSwap(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {
            //Set up signal data
            string tickerHeader;
            if (symbol.name.Substring(symbol.name.Length-3,3) == "TRS") {
                tickerHeader = symbol.name.Substring(0, 5);
            }else if (Regex.IsMatch(symbol.name,@"....\.\d")){
                var tickerParts = symbol.name.Split(Convert.ToChar("."));
                tickerHeader = (tickerParts[0] == "FNCL" ? "F" : "D");
                tickerHeader = tickerHeader + tickerParts[1] + tickerParts[2];
                tickerHeader = tickerHeader + tickerParts[3] + tickerParts[4];
            }else throw Bomb.toss("Bad symbol name, not able to figure out the signal data.");

            modelPrice = new Symbol(tickerHeader + "MDL").doubles(bars);
            actualPrice = new Symbol(tickerHeader + "ACT").doubles(bars);
            rollDecimal = new Symbol(tickerHeader + "WRL").doubles(bars);
            rollTicks = new Times(rollDecimal,rollDecimal.manager.constant(32.0));

            tradeSize = parameter<long>("TradeSize");
            maxSize = parameter<long>("MaxPyramid") * tradeSize;
            entryTicks = parameter<double>("EntryTicks");
            stopTicks = parameter<double>("StopTicks");

            richCheap = new RichCheapSpud(modelPrice, actualPrice);
            longEntryCross = new CrossOverSpud<double>(richCheap,entryTicks);
            shortEntryCross = new CrossOverSpud<double>(richCheap,-entryTicks);
            longExitCross = new CrossOverSpud<double>(richCheap, parameter<double>("ExitTicks"));
            shortExitCross = new CrossOverSpud<double>(richCheap, -parameter<double>("ExitTicks"));

            longMA = new EWMA(richCheap, parameter<double>("HalfLife"));
            rollCutOff = parameter<double>("RollCutOff");
            rollCutOffMargin = parameter<double>("RollCutOff.Margin");

            longRollCross = new CrossOverSpud<double>(rollDecimal, -(rollCutOff - rollCutOffMargin)/32);
            shortRollCross = new CrossOverSpud<double>(rollDecimal, (rollCutOff - rollCutOffMargin)/32);

            //Add Plots
            addToPlot(modelPrice,"Model Price",Color.Red,"Coupon Swap");
            addToPlot(actualPrice, "Actual Price", Color.Blue,"Coupon Swap");
            addToPlot(rollTicks, "Weighted Roll", Color.Green, "Roll");
        }

        protected override void onFilled(Position position, Trade trade) {
        }

        protected override void onNewBar() {
        }

        protected override void onNewTick(Bar partialBar, Tick tick) {
        }

        public override bool runOnClose(){
            return true;
        }

        class RichCheapSpud : Spud<double>{
            readonly Spud<double> model;
            readonly Spud<double> actual;

            public RichCheapSpud(Spud<double> model, Spud<double> actual) : base(model.manager) {
                this.model = dependsOn(model);
                this.actual = dependsOn(actual);
            }

            protected override double calculate() {
                return (model - actual)*32;
            }
        }

        protected override void onClose() {
            var nextTradeSize = Math.Min(tradeSize, maxSize - positionSize());

            //Entries
            if (nextTradeSize > 0) 
                placeEntries(nextTradeSize);

            //Exits
            if (hasPosition()) 
                placeExits();
        }

        void placeExits() {
            if (first(positions()).direction().isLong()) {              //Long Exits
                if (longExitCross.crossedBelow())                       //ZScore based objective exit
                    exitAllPositions("CS");
                else if (rollTicks < -(rollCutOff + rollCutOffMargin))  //Roll Exit
                    exitAllPositions("Roll");
                else if (bar.close <= entryPrice() - stopTicks / 32)    //Money management stop loss
                    exitAllPositions("Stop Loss");
            } else {                                                    //Short Exits
                if (shortExitCross.crossedAbove())                      //ZScore based objective exit
                    exitAllPositions("CS");
                else if (rollTicks > (rollCutOff + rollCutOffMargin))   //Roll Exit
                    exitAllPositions("Roll");
                else if (bar.close >= entryPrice() + stopTicks / 32)    //Money management stop loss
                    exitAllPositions("Stop Loss");
            }
        }

        void placeEntries(long nextTradeSize) {
            //ZScore based Entry
            if (longEntryCross.crossedAbove() && (richCheap > longMA) && (rollTicks > -(rollCutOff - rollCutOffMargin))) 
                placeOrder(symbol.buy("CS Buy", market(), nextTradeSize, oneBar()));
            if (shortEntryCross.crossedBelow() && (richCheap < longMA) && (rollTicks < (rollCutOff + rollCutOffMargin))) 
                placeOrder(symbol.sell("CS Sell", market(), nextTradeSize, oneBar()));
            //Roll re-entry
            if (longRollCross.crossedAbove() && (richCheap > entryTicks) && (richCheap > longMA))
                placeOrder(symbol.buy("Roll XOver Buy", market(), nextTradeSize, oneBar()));
            if (shortRollCross.crossedBelow() && (richCheap < -entryTicks) && (richCheap < longMA))
                placeOrder(symbol.sell("Roll XOver Sell", market(), nextTradeSize, oneBar()));
        }

        void exitAllPositions(string tradeDesc) {
            eachIt(positions(), (i, position) =>
                placeOrder(position.exit(tradeDesc + " Exit" + position.lOrS() + "_" + i, market(), oneBar()))
                );
        }

        long positionSize() {
            return (long) sum(convert(positions(),position=>(double)position.size));
        }

        double entryPrice() {
            return first(last(positions()).trades()).price;
        }
    }
}