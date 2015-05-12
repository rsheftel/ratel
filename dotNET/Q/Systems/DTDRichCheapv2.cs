using System;
using System.Drawing;
using Q.Spuds.Core;
using Q.Spuds.Indicators;
using Q.Trading;
using Q.Util;

namespace Q.Systems {
    public class DTDRichCheapV2 : SymbolSystem {
        internal readonly SymbolSpud<double> richCheap;
        internal readonly SymbolSpud<double> dtd;
        internal readonly SymbolSpud<double> spread;
        internal readonly SymbolSpud<double> dv01;
        internal readonly ZScoreSpud zScore;
        readonly double triggerLong;
        readonly double triggerShort;
        readonly double exitShort;
        readonly double exitLong;
        readonly int lengthZScore;
        readonly double stopLoss;
        readonly double timeStopBars;
        readonly double trailingStopFlag;
        readonly double maxSpread;
        readonly double profitObjectiveMultiple;
        double trailingStop;
        bool expectedProfitCalculationDisallowedForTest;

        public DTDRichCheapV2(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {
            requireOrdered("exitShortLevel", "triggerShort");
            requireOrdered("triggerLong", "exitLongLevel");
            requireOrdered("triggerLong", "exitShortLevel");
            requireOrdered("exitLongLevel", "triggerShort");
            if(!isActive()) return;
            
            richCheap = symbol.relatedSuffix("RICHCHEAP").doubles(bars);
            dtd = symbol.relatedSuffix("DTD").doubles(bars);
            spread = symbol.relatedSuffix("SPREAD").doubles(bars);
            lengthZScore = parameter<int>("lengthZScore");
            zScore = new ZScoreSpud(richCheap, lengthZScore, true);
            dv01 = symbol.relatedSuffix("DV01").doubles(bars);

            triggerLong = parameter<double>("triggerLong");
            triggerShort = parameter<double>("triggerShort");
            exitShort = parameter<double>("exitShortLevel");
            exitLong = parameter<double>("exitLongLevel");
            stopLoss = parameter<double>("lossStopLevel");
            timeStopBars = parameter<double>("timeStopBars");
            trailingStopFlag = parameter<double>("trailingStopFlag");
            maxSpread = parameter<double>("maxSpread");
            profitObjectiveMultiple = parameter<double>("profitObjectiveMultiple");
           
            trailingStop = 0;
            deactivate(() => dtd.hasContent() && richCheap.count() >= lengthZScore);

            addToPlot(dtd, "DTD", Color.Red, "dtd");
            addToPlot(richCheap, "Rich / Cheap", Color.Blue, "richCheap");
        }

        void requireOrdered(string first, string second) {
            var firstValue = parameter<double>(first);
            var secondValue = parameter<double>(second);
            if (firstValue >= secondValue) deactivate();
        }

        public override bool runOnClose() {
            return (true);
        }

        protected override void onClose() {
            if(hasPosition() && trailingStopFlag == 1) resetTrailingStop();
            tradingLogic();
        }

        void resetTrailingStop() {
            var tempTrailingStop = bars[0].close - position().direction() * (stopLoss / (bigPointValue() * position().size));
            if(position().direction().isLong() && tempTrailingStop > trailingStop) trailingStop = tempTrailingStop;
            if(position().direction().isShort() && tempTrailingStop < trailingStop) trailingStop = tempTrailingStop;
        }

        protected override void onNewBar() {}

        void tradingLogic() {
            checkLBO();
            checkStopLoss();
            checkTimeStop();
            if (!hasPosition()) placeInitialOrders(); 
            else placeExits();
        }

        void checkTimeStop() {
            if (!hasPosition()) return;
            var tradeName = sellCover() + " Time Stop";
            if (position().barsHeld() < timeStopBars) return;

            placeOrder(position().exit(tradeName, market(), oneBar()));
            var direction = position().direction();
            deactivateAndStop(() => zScore * direction >= 0);
        }

        void checkStopLoss() {
            if (!hasPosition()) return;
            var tradeName = sellCover() + " Stop Loss";
            if (trailingStopFlag == 0 && openPositionPnl() >= -stopLoss) return;
            if (trailingStopFlag == 1 && position().direction().isLong() && bars[0].close >= trailingStop) return;
            if (trailingStopFlag == 1 && position().direction().isShort() && bars[0].close <= trailingStop) return;
            placeOrder(position().exit(tradeName, market(), oneBar()));
            var direction = position().direction();
            deactivateAndStop(() => zScore * direction >= 0);
        }

        protected override void onFilled(Position position, Trade trade) {
            if(!(trade.description.Equals("LE") || trade.description.Equals("SE"))) return;
            if(trailingStopFlag == 0) return;
            trailingStop = trade.price - trade.direction * (stopLoss / (bigPointValue() * trade.size));
        }

        void checkLBO() {
            if (dtd.count() < 4 || dtd <= 4 || dtd[1] <= 4 || dtd-dtd[3] <= 2) return;
            if (hasPosition()) 
                placeOrder(position().exit(sellCover() + " Probable LBO", market(), oneBar()));
            deactivateAndStop(() => dtd < 3 && dtd[1] < 3);
        }

        string sellCover() {
            return position().longShort("Sell", "Cover");
        }

        bool isBadTime() {
            return symbol.isPeriodEnd(bar.time) || symbol.isPeriodInactive(bar.time);
        }

        void placeExits() {
            var tradeName = position().lOrS() + "X";
            if(isBadTime()) {
                tradeName = tradeName + " END MARKET PERIOD";
                placeOrder(position().exit(tradeName, market(), oneBar()));
            }
            var exitLevel = position().longShort(exitLong, exitShort);
            
            if(zScore * position().direction() > exitLevel * position().direction())
                placeOrder(position().exit(tradeName, market(), oneBar()));
        }

        void placeInitialOrders() {
            if(spread > maxSpread || isBadTime()) return;
            var profitObjective = profitObjectiveMultiple * slippage();
            if (zScore > triggerShort && expectedProfit(Direction.SHORT) > profitObjective)
                placeOrder(symbol.sell("SE", market(), parameter<long>("shortSize"), oneBar()));
            else if (zScore < triggerLong && expectedProfit(Direction.LONG) > profitObjective)
                placeOrder(symbol.buy("LE", market(), parameter<long>("longSize"), oneBar()));
        }

        public double expectedProfit(Direction direction) {
            var exitLevel = direction.longShort(exitLong, exitShort);
            return Math.Abs(zScore[0] - exitLevel) * zScore.standardDeviation() * dv01;
        }

        protected override void onNewTick(Bar partialBar, Tick tick) {
            tradingLogic();
        }

        public double expectedProfitWithSlippage(double slippageMultiplier) {
            Bomb.when(expectedProfitCalculationDisallowedForTest, () => "expected profit disallowed for test!");
            var entry = hasPosition() ? -1 : 1;
            var tradeDirection = hasPosition() ? position().direction() : order().direction;
            var tradeSize = hasPosition() ? position().size : order().size;
            var expectedProfitDollars = expectedProfit(tradeDirection) * tradeSize * symbol.bigPointValue;
            var expectedSlippages = tradeSize * slippage(symbol) * slippageMultiplier;
            return expectedProfitDollars - (entry * expectedSlippages);
        }

        internal void setExpectedProfitCalculationDisallowedForTest(bool setting) {
            expectedProfitCalculationDisallowedForTest = setting;
        }
    }
}