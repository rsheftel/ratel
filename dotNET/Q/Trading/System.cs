using System.Collections.Generic;
using System;
using System.Drawing;
using System.Threading;
using mail;
using Q.Spuds.Core;
using Q.Trading.Results;
using Q.Util;
using systemdb.metadata;
using util;
using Exception=System.Exception;

namespace Q.Trading {
    public abstract class System : Util.Objects, Collectible {
        

        readonly LazyDictionary<Symbol, List<Position>> positions_ = new LazyDictionary<Symbol, List<Position>>(symbol => new List<Position>());
        readonly LazyDictionary<Symbol, List<Order>> orders_ = new LazyDictionary<Symbol,  List<Order>>(symbol => new List<Order>());
        protected readonly QREBridgeBase bridge;
        bool inClose;
        protected bool forecastMode;
        internal readonly List<DynamicExit> dynamicExits = new List<DynamicExit>();
        internal readonly List<DynamicExit> dynamicExitsOnClose = new List<DynamicExit>();
        readonly List<Predicate> reactivationConditions = new List<Predicate>();
        internal Timer onCloseTimer;
        

        protected System(QREBridgeBase bridge) {
            this.bridge = bridge;
        }

        protected Dictionary<Symbol, BarSpud> bars {
            get { return bridge.bars(); }
        }

        public virtual BarSpud barSpud(Symbol s) {
            return bars[s];
        }

        public double slippage(Symbol s) {
            return bridge.slippage(s);
        }

        protected void addToPlot(Collectible collectible, Spud<double> spud, string plotName, Color color, string pane) {
            bridge.addToPlot(collectible, new PlotDefinition(spud, plotName, color, pane));
        }

        protected void addToPlot(Collectible collectible, Spud<double> spud, string plotName, Color color) {
            addToPlot(collectible, spud, plotName, color, null);
        }

        public SystemArguments arguments() {
            return bridge.arguments();
        }

        public virtual void goLiveDO_NOT_CALL_EXCEPT_FROM_BRIDGE() {
            // careful adding stuff here, as it is currently not delegated to component systems in a composite (except for Independent*)
            setOnCloseTimerMaybe();
        }

        void setOnCloseTimerMaybe() {
            if (!runOnClose()) return;
            var timeTilClose = onCloseTime().Subtract(now());
            if (timeTilClose.TotalSeconds < 0) return;
            timerManager().atTime(onCloseTime(), doCloseLive, out onCloseTimer);
            onCloseTimer.Equals(null); // onCloseTimer is "unused" but we need the declaration to prevent garbage collection
        }

        void doCloseLive() {
            var range = bridge.interval().range(jDate(onCloseTime()));
            var goodBars = dictionary(accept(bridge.partialBars, (symbol, bar) => range.start().before(jDate(bar.time))));
            bridge.processClose(goodBars, this);
        }

        public abstract DateTime onCloseTime();

        public virtual void processBarDO_NOT_CALL_EXCEPT_FROM_BRIDGE(Dictionary<Symbol, Bar> newBars) {
            cancelOrders(o => newBars.ContainsKey(o.symbol) && o.duration.shouldCancelOnNewBar(newBars[o.symbol].time));
            if (inLeadBars()) {
                info("lead bar"); 
                return;
            }
            processDynamicExits(dynamicExits);
            try {
                onNewBar(newBars);
            } catch (AbortBar) {}
        }

        protected static Exception stopProcessing() {
            throw new AbortBar();
        }

        public virtual void cancelOrders(Predicate<Order> needsCancel) {
            each(orders_.values(), ordersList => ordersList.RemoveAll(o => {
                var cancel = needsCancel(o);
                if(!cancel) return false;
                o.cancel();
                return true;
            }));
        }

        public virtual void cancelOrder(Order o) {
            o.cancel();
            orders_.get(o.symbol).Remove(o);
        }

        public virtual void processTickDO_NOT_CALL_EXCEPT_FROM_BRIDGE(Symbol symbol, Bar partialBar, Tick tick) {
            cancelOrders(o => o.duration.shouldCancelOnTick());
            try {
                if(runOnNewTick())
                    onNewTick(symbol, partialBar, tick);
            } catch(AbortBar) {}
            if (!runOnClose()) return;
            try {
                forecastMode = true;
                monitor().resetForecasts(this);
                doCloseLive();
            } finally {
                forecastMode = false;
            }
        }

        public virtual void processCloseDO_NOT_CALL_EXCEPT_FROM_BRIDGE(Dictionary<Symbol, Bar> newBars) {
            if (!runOnClose()) return;
            
            try {
                inClose = true;
                if (!inLeadBars()) {
                    processDynamicExits(dynamicExitsOnClose);
                    onClose(newBars);
                }
            } catch(AbortBar) {
            } finally {
                inClose = false;
            }

        }

        public virtual bool runOnClose() {
            return false;
        }

        public virtual bool runOnNewTick() {
            return false;
        }

        TradeMonitor monitor() {
            return bridge.monitor();
        }

        protected abstract void onNewTick(Symbol symbol, Bar partialBar, Tick tick);
        protected abstract void onNewBar(Dictionary<Symbol, Bar> b);
        protected abstract void onFilled(Position position, Trade trade);
        protected abstract void onClose(Dictionary<Symbol, Bar> current);

        protected internal virtual bool inLeadBars() {
            return bridge.inLeadBars();
        }

        void updatePositionsAndOrders(Position position, Order order) {
            var positionList = positions(position.symbol);
            var hasPosition = positionList.Contains(position);
            if(hasPosition && position.isClosed()) positionList.Remove(position);
            else if(!hasPosition) positionList.Add(position);
            orders_.get(order.symbol).Remove(order);
        }

        public virtual void orderFilledDO_NOT_CALL_EXCEPT_FROM_BRIDGE(Position position, Trade trade) {
            trade.updateOrder(findOrder(trade.order()));
            updatePositionsAndOrders(position, trade.order());
            try { onFilled(position, trade); }
            catch (AbortBar) {}
            if (position.isClosed())
                cancelOrders(otherOrder => otherOrder.hasPosition() && otherOrder.position().Equals(position));
        }

        Order findOrder(Order order) {
            return the(accept(orders(order.symbol), o => o.Equals(order)));
        }

        public static S create<S> (string className, QREBridgeBase bridge) where S : System {
            return create<S>(className, true, bridge);
        }


        public static S create<S>(string className, bool useMultiSymbol, QREBridgeBase bridge) where S : System {
            var type = Type.GetType(className, true, false);
            if(useMultiSymbol && type.IsSubclassOf(typeof(SymbolSystem)))
                type = typeof (IndependentSymbolSystems<>).MakeGenericType(type);
            var c = type.GetConstructor(new[] { typeof(QREBridge<S>) });
            var system = (S) c.Invoke(new object[] { bridge });
            return system;
        }

        public static S create<S>(QREBridgeBase bridge) where S : System {
            return create<S>(bridge.arguments().parameters.systemClassName(), bridge);
        }

        public virtual new void info(string message) {            
            LogC.info(message);
        }

        public Order order() {
            return the(the(orders_.values()));
        }

        protected bool hasPosition(Symbol symbol) {
            return hasContent(positions(symbol));
        }

        public virtual bool hasPosition() {
            return hasContent(allPositions());
        }

        public bool hasOrders() {
            return hasContent(allOrders());
        }

        public virtual List<Order> orders(Symbol symbol) {
            return orders_.get(symbol);
        }

        public void cancelAllOrders() {
            cancelOrders(alwaysTrue);
        }

        public virtual void placeOrder(Order order) {
            var place = (forecastMode ? "forecast" : "place");
            LogC.info(place + " order: " + order);
            order.placedBy(this, bars[order.symbol][0].time);
            var symbol = order.symbol;
            Bomb.unless(isActive(), () => "cannot " + place + " an order in a deactivated symbol: " + order);
            if(inClose) order.placedOnClose();
            if (forecastMode) {
                if(order.canFill(Bomb.missing(bars, symbol)[0].close, true)) {
                    // bug - if two exits are placed in onClose, they can both be "filled" for the 
                    // forecast, which is not what would happen in the actual onClose
                    monitor().orderForecast(this, order);
                }
            } else {
                Bomb.when(exists(orders(symbol), order.descriptionAndSymbolMatch), () =>
                    "symbol+description must be unique for outstanding orders.  Tried placing: " + 
                    order + "\nin:\n" + toShortString(orders(symbol)));
                monitor().orderPlaced(order);
                orders_.get(symbol).Add(order);
            }
        }

        public void placeOrders(IEnumerable<Order> orders) {
            each(orders, placeOrder);
        }


        public virtual Position position(Symbol symbol) {
            var myPositions = positions(symbol);
            if (myPositions.Count <= 1) return the(myPositions);
            var content = "";
            foreach (var p in myPositions) content += p + "\n";
            Log.info("found multiple open positions: \n" + content);
            throw Bomb.toss("found multiple open positions: \n" + content + "\nthe end...");
        }

        public virtual List<Position> positions(Symbol symbol) {
            return positions_.get(symbol);
        }

        internal virtual IEnumerable<Position> allPositions() {
            return collect(arguments().symbols, symbol => positions(symbol));
        }

        public T parameter<T>(string parameterName) {
            return arguments().parameters.get<T>(parameterName);
        }

        protected static OrderDetails stop(double stopLevel) { return Order.stop(stopLevel); }
        protected static OrderDetails limit(double limitLevel) { return Order.limit(limitLevel); }
        protected static OrderDetails stopLimit(double level) { return stopLimit(level, level); }
        protected static OrderDetails stopLimit(double stopLevel, double limitLevel) { return Order.stopLimit(stopLevel, limitLevel); }
        protected static OrderDetails protectiveStop(double level) { return protectiveStop(level, level); }
        protected static OrderDetails protectiveStop(double stopLevel, double limitLevel) { return Order.protectiveStop(stopLevel, limitLevel); }
        protected static OrderDetails market() { return Order.market(); }

        protected static OrderDuration fillOrKill() { return FillOrKill.FILL_KILL; }
        protected static OrderDuration oneBar() { return OneBar.ONE; }
        protected static OrderDuration oneDay() { return new OneDay(); }     
        protected static OrderDuration onTheClose() { return OnClose.ON_CLOSE; }

        public bool hasOrder(Order order) {
            return exists(orders(order.symbol), o => o.Equals(order));
        }

        public virtual string marketName() {
            return "ALL";
        }

        protected void addDynamicExit(DynamicExit exit, bool placeOnClose) {
            if(placeOnClose) {
                dynamicExitsOnClose.Add(exit);
                exit.onPositionClosed(() => { dynamicExitsOnClose.Remove(exit); exit.remove();});
            }  else {
                exit.placeOrder(this);
                dynamicExits.Add(exit);
                exit.onPositionClosed(() => { dynamicExits.Remove(exit); exit.remove();});
            }
        }

        void processDynamicExits(IEnumerable<DynamicExit> exits) {
            each(exits, exit => exit.placeOrder(this));
        }

        protected internal void deactivate(Predicate condition) {
            var barCount = bridge.manager.barCount();
            Predicate conditionAndBarPassed = () => bridge.manager.barCount() >  barCount && condition();
            reactivationConditions.Add(conditionAndBarPassed);
        }

        protected void deactivate() {
            deactivate(() => false);
        }

        protected internal void deactivateAndStop(Predicate reactivationCondition) {
            deactivate(reactivationCondition);
            stopProcessing();
        }

        protected internal bool isActive() {
            if (isEmpty(reactivationConditions)) return true;
            Bomb.when(hasContent(allPositions()), () => "can't deactivate with existing position!");
            reactivationConditions.RemoveAll(p => p());
            return isEmpty(reactivationConditions);
        }

        public virtual Email tradeEmail(LiveSystem liveSystem, Trade trade, int liveOrderId) {
            return PositionMonitor.basicTradeEmail(liveSystem, trade, liveOrderId);
        }

        public virtual IEnumerable<Order> allOrders() {
            return collect(arguments().symbols, symbol => orders(symbol));
        }

        public virtual string name { 
            get { return GetType().Name + "-ALL"; }
        }

        public void addOrder(StatisticsCollector collector, Position position, Trade trade) {
            if(trade.order().system() != this) return;
            collector.addOrder(new WeightedPosition(position), new WeightedTrade(trade));
        }

        public virtual void addBar(StatisticsCollector collector, System system, Dictionary<Symbol, Bar> theBars, Dictionary<Symbol, double> fxRates) {
            var weighted = convert(allPositions(), p => new WeightedPosition(p));
            collector.addBar(weighted, theBars, fxRates);
        }

        public virtual void addCollectorsTo(SystemArguments arguments, Dictionary<Collectible, StatisticsCollector> collectors) {
            collectors[this] = new StatisticsCollector(arguments);
        }

        public IEnumerable<Position> allPositions(System system) {
            return allPositions();
        }

        public virtual BarSpud barsMaybe() {
            return null;
        }

        public bool collects(Position position) {
            return position.entry().order().system() == this;
        }

        public bool collects(Trade trade) {
            return trade.order().system() == this;
        }
    }
}