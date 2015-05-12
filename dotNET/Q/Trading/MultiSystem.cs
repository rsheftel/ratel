using System;
using System.Collections.Generic;
using mail;
using Q.Trading.Results;
using Q.Util;
using systemdb.metadata;

namespace Q.Trading {
    public abstract class MultiSystem<T, S> : System where T : SystemKey where S : SingleSystem<T> {
        protected internal readonly Dictionary<T, S> systems_ = new Dictionary<T, S>();
        readonly LazyDictionary<Symbol, List<S>> systemsBySymbol = new LazyDictionary<Symbol, List<S>>(symbol => new List<S>());
        protected MultiSystem(QREBridgeBase bridge) : base(bridge) {}

        protected void addSystem(T key, S system) {
            systems_[key] = system;
            each(key.symbols(), symbol => systemsBySymbol.get(symbol).Add(system));
        }

        public override void processBarDO_NOT_CALL_EXCEPT_FROM_BRIDGE(Dictionary<Symbol, Bar> b) {
            forSystems((key, componentSystem) => {
                if (key.coveredBy(b)) 
                    componentSystem.processBarDO_NOT_CALL_EXCEPT_FROM_BRIDGE(b);
            });
        }

        public override void processTickDO_NOT_CALL_EXCEPT_FROM_BRIDGE(Symbol symbol, Bar partialBar, Tick tick) {
            each(systems(symbol), system =>  system.processTickDO_NOT_CALL_EXCEPT_FROM_BRIDGE(symbol, partialBar, tick));
        }

        internal List<S> systems(Symbol symbol) {
            return systemsBySymbol.get(symbol);
        }

        public override void processCloseDO_NOT_CALL_EXCEPT_FROM_BRIDGE(Dictionary<Symbol, Bar> newBars) {
            forSystems((key, componentSystem) => {
                 if (key.coveredBy(newBars)) 
                     componentSystem.processCloseDO_NOT_CALL_EXCEPT_FROM_BRIDGE(newBars);
             });
        }

        public override void orderFilledDO_NOT_CALL_EXCEPT_FROM_BRIDGE(Position position, Trade trade) {
            trade.order().system().orderFilledDO_NOT_CALL_EXCEPT_FROM_BRIDGE(position, trade);
        }

        public override DateTime onCloseTime() {
            return requireAllMatchFirst(systems_.Values, system => system.onCloseTime());
        }

        protected void forSystems(Action<T, S> run) {
            eachKey(systems_, key => run(key, systems_[key]));
        }

        public override List<Order> orders(Symbol symbol) {
            return collect(systems(symbol), system => system.orders(symbol));
        }

        public override List<Position> positions(Symbol symbol) {
            return collect(systems(symbol), system => system.positions(symbol));
        }

        public override void cancelOrders(Predicate<Order> needsCancel) {
            forSystems((key, componentSystem) => componentSystem.cancelOrders(needsCancel));
        }

        public override void cancelOrder(Order o) {
            o.system().cancelOrder(o);
        }

        protected override void onFilled(Position position, Trade trade) { throw new NotImplementedException(); }
        protected override void onNewBar(Dictionary<Symbol, Bar> b) { throw new NotImplementedException(); }
        protected override void onClose(Dictionary<Symbol, Bar> current) { throw new NotImplementedException(); }
        protected override void onNewTick(Symbol symbol, Bar partialBar, Tick tick) { throw new NotImplementedException(); }

        public override sealed void placeOrder(Order order) {
            Bomb.toss("cannot place an order directly on a MultiSystem.  Use component system instead.");
        }

        public override Email tradeEmail(LiveSystem liveSystem, Trade trade, int liveOrderId) {
            return trade.order().system().tradeEmail(liveSystem, trade, liveOrderId);
        }

        public override void addCollectorsTo(SystemArguments arguments, Dictionary<Collectible, StatisticsCollector> collectors) {
            forSystems((key, componentSystem) => componentSystem.addCollectorsTo(componentSystem.arguments(), collectors));
        }
    }
}