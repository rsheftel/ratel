using System.Collections.Generic;
using Q.Trading;

namespace Q.Systems.MultiSymbolSystems {
    public abstract class DTDPortfolioMultiSymbolBase : MultiSymbolSystem<DTDRichCheapV2> {
        protected DTDPortfolioMultiSymbolBase(QREBridgeBase bridge) : base(bridge) {}

        public override bool runOnClose() {
            return true;
        }

        public override void processCloseDO_NOT_CALL_EXCEPT_FROM_BRIDGE(Dictionary<Symbol, Bar> newBars) {
            base.processCloseDO_NOT_CALL_EXCEPT_FROM_BRIDGE(newBars);
            managerOnClose(newBars);
        }

        protected abstract void managerOnClose(Dictionary<Symbol, Bar> current);

    }
}