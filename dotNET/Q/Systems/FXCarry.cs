using System;
using System.Drawing;
using Q.Spuds.Core;
using Q.Spuds.Indicators;
using Q.Trading;
using Q.Trading.Results;

namespace Q.Systems {
    public class FXCarry : SymbolSystem {
        const string STOP_OUT = "StopOut";
        public readonly SymbolSpud<double> payoutRatio;
        readonly Spud<double> atr;
        readonly double tradeSizeParameter;
        readonly double recoveryAmount;
        readonly int recoveryPeriod;
        readonly double trigger;
        readonly double maxTrigger;
        readonly double triggerCushion;

        public FXCarry(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {
            payoutRatio = payoutRatioSymbol(symbol).doubles(bars);
            atr = new AverageTrueRange(bars, 100);
            tradeSizeParameter = parameter<double>("TradeSize");
            recoveryAmount = parameter<double>("RecoveryAmount") * tradeSizeParameter;
            recoveryPeriod = parameter<int>("RecoveryPeriod");
            trigger = parameter<double>("Trigger");
            maxTrigger = parameter<double>("MaxTrigger");
            triggerCushion = parameter<double>("TriggerCushion");
            addToPlot(payoutRatio, "payoutRatio", Color.Red, "payoutRatio");
            bars.close.prepare();
            stoppedOut();
        }

        static Symbol payoutRatioSymbol(Collectible symbol) {
            var symbolName = symbol.name;
            var name = symbolName.Substring(0, 8) + "POUT." + symbolName[12];
            return new Symbol(name);
        }

        void stoppedOut() {
            Spud<int> barsSinceStopOut = new BarCounter(bars, -1);
            deactivate(() => isRecovered(barsSinceStopOut, tradeSize()) && cleanup(barsSinceStopOut));
        }

        bool cleanup(SpudBase barsSinceStopOut) {
            bars.removeChild(barsSinceStopOut);
            barsSinceStopOut.manager.remove(barsSinceStopOut);
            return true;
        }

        bool isRecovered(Spud<int> barsSinceStopOut, long size) {
            var barsBack = Math.Min(barsSinceStopOut, recoveryPeriod);
            if(barsSinceStopOut > recoveryPeriod) size = tradeSize();
            var pnl = symbol.pnl(size, bars[barsBack].close, bar.close);
            return pnl > recoveryAmount;
        }

        protected override void onNewBar() {
            tradingLogic();
        }

        protected override void onNewTick(Bar partialBar, Tick tick) {
            tradingLogic();
        }

        protected override void onFilled(Position position, Trade trade) {
            if(trade.description.Equals(STOP_OUT)) {
                stoppedOut();
                stopProcessing();
            }
            if(position.isEntry(trade)) 
                addDynamicExit(new DollarTrailingStop(position, bars.close, parameter<double>("StopLoss") * tradeSizeParameter, STOP_OUT, bridge), false);
            tradingLogic();
        }

        void tradingLogic() {
            if(hasPosition()) checkExits();
            else checkEntry();
        }

        void checkEntry() {
            if(payoutRatio > trigger && payoutRatio <= maxTrigger)
                placeOrder(buy("AttractiveBuy", market(), tradeSize(), oneBar()));
        }

        void checkExits() {
            if(payoutRatio <= trigger - triggerCushion) 
                placeOrder(position().exitLong("NotAttractiveSell", market(), oneBar()));
        }

        long tradeSize() {
            return (long) (tradeSizeParameter*Math.Max(1,(int)(10.0/atr)));
        }

        protected override void onClose() {}
    }
}