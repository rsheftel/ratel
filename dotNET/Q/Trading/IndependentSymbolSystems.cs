using System;

namespace Q.Trading {
    // sealed for now to prevent inadvertent inheritance.  inherit from MultiSymbolSystem instead.
    public sealed class IndependentSymbolSystems<T> : MultiSymbolSystem<T> where T : SymbolSystem {
        public IndependentSymbolSystems(QREBridgeBase bridge) : base(bridge) {}

        public override DateTime onCloseTime() {
            // we never want the IndependentSymbolSystems's onClose to get called.  Each symbol system has its own onCloseTime and timer.
            return now().AddDays(10);
        }
        
        public override bool runOnClose() {
            return any(systems_.Values, s => s.runOnClose());
        }
        
        public override bool runOnNewTick() {
            return any(systems_.Values, s => s.runOnNewTick());
        }

        public override void goLiveDO_NOT_CALL_EXCEPT_FROM_BRIDGE() {
            base.goLiveDO_NOT_CALL_EXCEPT_FROM_BRIDGE();
            forSystems((symbol, symbolSystem) => symbolSystem.goLiveDO_NOT_CALL_EXCEPT_FROM_BRIDGE());
        }
    }
}