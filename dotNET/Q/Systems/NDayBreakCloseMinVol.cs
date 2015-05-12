using Q.Trading;

namespace Q.Systems {
    public class NDayBreakCloseMinVol : NDayBreakBase {
        public NDayBreakCloseMinVol(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol, bars => bars.close, bars => bars.close) {}

        protected override void onNewBar() {}
        protected override void onNewTick(Bar partialBar, Tick tick) {}
        
        public override bool runOnClose() { return true; }
        protected override void onClose() {
            if(hasPosition()) placeStopsAndPyramids();
            else checkEntry();
        }

        void checkEntry() {
            if(atr > parameter<double>("minATRLong")) placeLongEntry();
            if(atr > parameter<double>("minATRShort")) placeShortEntry();
        }
    }
}