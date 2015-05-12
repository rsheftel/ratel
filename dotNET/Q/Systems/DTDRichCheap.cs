using System.Drawing;
using Q.Spuds.Core;
using Q.Spuds.Indicators;
using Q.Trading;


namespace Q.Systems {
    public class DTDRichCheap : SymbolSystem {
        internal readonly SymbolSpud<double> richCheap;
        internal readonly SymbolSpud<double> dtd;
        readonly ZScoreSpud zScore;
        readonly double triggerLong;
        readonly double triggerShort;
        readonly double exitShort;
        readonly double exitLong;
        readonly int lengthZScore;
        readonly double stopLoss;

        public DTDRichCheap(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {
            requireOrdered("exitShortLevel", "triggerShort");
            requireOrdered("triggerLong", "exitLongLevel");
            requireOrdered("triggerLong", "exitShortLevel");
            requireOrdered("exitLongLevel", "triggerShort");
            if(!isActive()) return;
            
            richCheap = symbol.relatedSuffix("RICHCHEAP").doubles(bars);
            dtd = symbol.relatedSuffix("DTD").doubles(bars);
            lengthZScore = parameter<int>("lengthZScore");
            zScore = new ZScoreSpud(richCheap, lengthZScore, true);

            triggerLong = parameter<double>("triggerLong");
            triggerShort = parameter<double>("triggerShort");
            exitShort = parameter<double>("exitShortLevel");
            exitLong = parameter<double>("exitLongLevel");
            stopLoss = parameter<double>("lossStopLevel");
            deactivate(() => dtd.hasContent() && richCheap.count() >= lengthZScore);

            addToPlot(dtd, "DTD", Color.Red, "dtd");
            addToPlot(richCheap, "Rich / Cheap", Color.Blue, "richCheap");
            
        }

        void requireOrdered(string first, string second) {
            var firstValue = parameter<double>(first);
            var secondValue = parameter<double>(second);
            if (firstValue >= secondValue) deactivate();
        }

        protected override void onNewBar() {
            tradingLogic();
        }

        void tradingLogic() {
            checkLBO();
            checkStopLoss();
            if (!hasPosition()) placeInitialOrders(); 
            else placeExits();
        }

        void checkStopLoss() {
            if (!hasPosition()) return;
            var tradeName = sellCover() + " Stop Loss";
            if (openPositionPnl() >= -stopLoss) return;

            placeOrder(position().exit(tradeName, market(), oneBar()));
            var direction = position().direction();
            deactivateAndStop(() => zScore * direction >= 0);
        }

        protected override void onFilled(Position position, Trade trade) {
            if(isActive())
                tradingLogic();
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

        void placeExits() {
            var exitLevel = position().longShort(exitLong, exitShort);
            var tradeName = position().lOrS() + "X";
            if(zScore * position().direction() > exitLevel * position().direction())
                placeOrder(position().exit(tradeName, market(), oneBar()));
        }

        void placeInitialOrders() {
            if (zScore > triggerShort)
                placeOrder(symbol.sell("SE", market(), parameter<long>("shortSize"), oneBar()));
            else if (zScore < triggerLong)
                placeOrder(symbol.buy("LE", market(), parameter<long>("longSize"), oneBar()));
        }

        protected override void onNewTick(Bar partialBar, Tick tick) {
            tradingLogic();
        }

        protected override void onClose() {}
    }
}