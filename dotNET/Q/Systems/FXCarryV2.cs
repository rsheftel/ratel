using System;
using System.Drawing;
using Q.Spuds.Core;
using Q.Spuds.Indicators;
using Q.Trading;
using Q.Trading.Results;

namespace Q.Systems {
    public class FXCarryV2 : SymbolSystem {
        const string STOP_OUT = "StopOut";
        public readonly SymbolSpud<double> payoutRatioLong;
        public readonly SymbolSpud<double> payoutRatioShort;
        readonly Spud<double> atr;
        internal BollingerBand upperBand;
        internal BollingerBand lowerBand;
        readonly double riskDollars;
        readonly int bollingerBandBarsBack;
        readonly int nATR;
        readonly Average ma;
        readonly int atrLen;
        readonly double bollingerBandDeviations;
        readonly double trigger;
        readonly double maxTrigger;
        readonly double triggerCushion;
        internal bool stoppedOut;

        public FXCarryV2(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {
            payoutRatioLong = payoutRatioSymbol(symbol, "Long").doubles(bars).allowStaleTicks();
            payoutRatioShort = payoutRatioSymbol(symbol, "Short").doubles(bars).allowStaleTicks();
            nATR = parameter<int>("nATR");
            atrLen = parameter<int>("ATRLen");
            atr = new AverageTrueRange(bars, atrLen);
            riskDollars = parameter<double>("RiskDollars");
            bollingerBandBarsBack = parameter<int>("BollingerBandBarsBack");
            ma = new Average(bars.close, bollingerBandBarsBack);
            bollingerBandDeviations = parameter<double>("BollingerBandDeviations");
            trigger = parameter<double>("Trigger");
            maxTrigger = parameter<double>("MaxTrigger");
            triggerCushion = parameter<double>("TriggerCushion");
            addToPlot(payoutRatioLong, "payoutRatioLong", Color.Red, "payoutRatioLong");
            addToPlot(payoutRatioShort, "payoutRatioShort", Color.Red, "payoutRatioShort");
            upperBand = new BollingerBand(bars.close, bollingerBandBarsBack, bollingerBandDeviations);
            lowerBand = new BollingerBand(bars.close, bollingerBandBarsBack, -bollingerBandDeviations);
            addToPlot(upperBand, "upperBand", Color.Blue);
            addToPlot(lowerBand,"lowerBand",Color.Blue);
            bars.close.prepare();
            stoppedOut = true;
        }

        static Symbol payoutRatioSymbol(Collectible symbol, string longShort) {
            var symbolName = symbol.name;
            var name = symbolName.Substring(0, 6) + "6MPOUT.";
            switch (longShort) {
                case "Long":
                    name = name + "C";
                    break;
                case "Short":
                    name = name + "P";
                    break;
            }
            return new Symbol(name);
        }

        bool isRecovered() {
            if (payoutRatioLong[0] > 0) return bars.close[0] > upperBand[0];
            if (payoutRatioShort[0] > 0) return bars.close[0] < lowerBand[0];
            return (false);
        }

        protected override void onNewBar() {}

        protected override void onNewTick(Bar partialBar, Tick tick) {}

        protected override void onFilled(Position position, Trade trade) {
            if(trade.description.Equals(STOP_OUT)) {
                stoppedOut = true;
            }
            if(position.isEntry(trade)) 
                addDynamicExit(new DollarTrailingStop(position, bars.close, parameter<double>("RiskDollars") , STOP_OUT, bridge), true);
            tradingLogic();
        }

        void tradingLogic() {
            if(hasPosition()) checkExits();
            else checkEntry();
        }

        void checkEntry() {
            if(payoutRatioLong > trigger && payoutRatioLong <= maxTrigger && !stoppedOut && (ma[0] - ma[1]) > 0)
                placeOrder(buy("AttractiveBuy", market(), tradeSize(), oneBar()));
            if (payoutRatioShort > trigger && payoutRatioShort <= maxTrigger && !stoppedOut && (ma[0] - ma[1]) < 0)
                placeOrder(sell("AttractiveSell", market(), tradeSize(), oneBar()));
//            LogC.debug(commaSep("ATR:", atr));
        }

        void checkExits() {
            if ((payoutRatioLong <= trigger - triggerCushion) && position().direction().isLong()) 
                placeOrder(position().exitLong("NotAttractiveSell", market(), oneBar()));
            if ((payoutRatioShort <= trigger - triggerCushion) && position().direction().isShort()) 
                placeOrder(position().exitShort("NotAttractiveBuy", market(), oneBar())); 
        }

        long tradeSize() {
            return (long) Math.Max(riskDollars/(nATR * atr * bigPointValue()),1.0);
        }

        protected override void onClose() {
            if (isRecovered()) stoppedOut = false;
            tradingLogic();
        }

        public override bool runOnClose() {
            return true;
        }
    }
}