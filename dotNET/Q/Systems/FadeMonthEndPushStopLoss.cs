using Q.Trading;

namespace Q.Systems {
    public class FadeMonthEndPushStopLoss : FadeMonthEndPush {
        readonly double stopLoss;

        public FadeMonthEndPushStopLoss(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {
            stopLoss = equity() * parameter<double>("Risk") * parameter<double>("StopLossMultiple");
        }

        protected override void onNewBar() {
            if(noPosition()) return;
            var stopLevel = bar.close - position().direction() * (stopLoss / (bigPointValue() * position().size));
            placeOrder(position().exit(position().longShort("SellStopLoss", "CoverStopLoss"), new Stop(stopLevel), oneBar()));
            //system.setPending(symbol.exitAt(stopLevel, name, position));
        }
    }
}