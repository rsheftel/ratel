using System;
using System.Collections.Generic;
using Q.Systems.PairSystems;
using Q.Trading;

namespace Q.Systems.MultiPairSystems {
    public class CommodityCarryMaxTradesPerContract : MultiPairSystem<CommodityCarry, CommodityCarryPairGenerator> {
        readonly MaxTradesManager<CommodityCarry, Pair> manager;

        public CommodityCarryMaxTradesPerContract(QREBridgeBase bridge) : base(bridge) {
            manager = new MaxTradesManager<CommodityCarry, Pair>(systems_.Values, parameter<int>("maxTradesPerContract"), 
                (a, b) => Math.Abs(a.payoutRatio[0]).CompareTo(Math.Abs(b.payoutRatio[0])), 
                system => system.placeExits());
        }

        public override void processCloseDO_NOT_CALL_EXCEPT_FROM_BRIDGE(Dictionary<Symbol, Bar> newBars) {
            base.processCloseDO_NOT_CALL_EXCEPT_FROM_BRIDGE(newBars);
            manager.onClose(newBars);
        }

        public override bool runOnClose() {
            return true;
        }

    }
}