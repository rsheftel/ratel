using System;
using System.Collections.Generic;
using System.Drawing;
using Q.Spuds.Core;
using Q.Trading.Results;
using Q.Util;

namespace Q.Trading {
    public abstract class SymbolSystem : SingleSystem<Symbol> {
        protected readonly Symbol symbol;
        protected internal readonly new BarSpud bars;
        readonly SpudManager manager; // TEST ONLY DO ***NOT*** REFERENCE
        bool parentManagerHasPushedDown;
        public override string name { get { return symbol.name; }}

        protected SymbolSystem(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {
            this.symbol = symbol;
            manager = new SpudManager();
            bars = new BarSpud(manager);
            base.bars[symbol].pushedDown += () => {
                parentManagerHasPushedDown = true; 
            };
            base.bars[symbol].valueSet += newValue => {
                if(parentManagerHasPushedDown) {
                    manager.newBar();
                    parentManagerHasPushedDown = false;
                }
                bars.set(newValue);
            };
            base.bars[symbol].manager.onLive += () => manager.goLive();
            base.bars[symbol].manager.onRecalculate += () => manager.recalculate();
            base.bars[symbol].ticked += time => bars.lastTickedAt(time);
        }

        protected internal Bar bar { get { return bars[0]; }}

        protected void addToPlot(Spud<double> spud, string plotName, Color color, string pane) {
            addToPlot(this, spud, plotName, color, pane);
        }

        protected void addToPlot(Spud<double> spud, string plotName, Color color) {
            addToPlot(this, spud, plotName, color);
        }

        protected abstract void onNewBar();
        protected abstract void onNewTick(Bar partialBar, Tick tick);
        protected abstract void onClose();

        protected override sealed void onNewBar(Dictionary<Symbol, Bar> b) {
            if(isActive()) 
                onNewBar();
        }

        protected override sealed void onNewTick(Symbol s, Bar partialBar, Tick tick) {
            if(isActive())
                onNewTick(partialBar, tick);
        }

        protected override sealed void onClose(Dictionary<Symbol, Bar> current) {
            var active = false;
            try {
                active = isActive();
            } catch {
                if (!forecastMode) throw;
            }
            if (active)
                onClose();
        }

        public override DateTime onCloseTime() {
            return symbol.processCloseOrdersTime();
        }

        protected internal override bool inLeadBars() {
            return bars.count() <= arguments().leadBars;
        }

        public override void info(string message) {
            LogC.info(symbol.name + ":" + message);
        }

        protected Order buy(string description, OrderDetails details, long size, OrderDuration duration) {
            return symbol.buy(description, details, size, duration);
        }
        protected Order sell(string description, OrderDetails details, long size, OrderDuration duration) {
            return symbol.sell(description, details, size, duration);
        }

        public static T create<T>(string className, QREBridgeBase bridge, Symbol symbol) where T : SymbolSystem {
            var type = Type.GetType(className, true, false);
            var c = type.GetConstructor(new[] { typeof(QREBridge<T>), typeof(Symbol) });
            Bomb.ifNull(c, () => "no constuctor matching qrebridge of type, symbol on class " + className);
            return (T) c.Invoke(new object[] { bridge, symbol });
        }

        public override string marketName() {
            return symbol.name;
        }

        protected double bigPointValue() {
            return symbol.bigPointValue;
        }

        protected virtual double openPositionPnl() {
            return position().pnlNoSlippage(bars[0].close, arguments().runInNativeCurrency, bridge.fxRate(position().symbol));
        }

        protected bool isLong() {
            return hasPosition() && position().direction().isLong();
        }

        protected bool isShort() {
            return hasPosition() && position().direction().isShort();
        }

        protected bool noPosition() {
            return !hasPosition();
        }

        internal override IEnumerable<Position> allPositions() {
            return positions(symbol);
        }

        public override IEnumerable<Order> allOrders() {
            return orders(symbol);
        }

        public virtual Position position() {
            return position(symbol);
        }

        public List<Order> orders() {
            return orders(symbol);
        }

        public List<Position> positions() {
            return positions(symbol);
        }

        public override void placeOrder(Order order) {
            Bomb.unless(order.symbol.Equals(symbol), ()=>"SymbolSystem cannot currently place orders on other symbols.  see comment in MultiSymbolSystem.orders()");
            base.placeOrder(order);
        }

        public double slippage() {
            return slippage(symbol);
        }

        public override void addBar(StatisticsCollector collector, System system, Dictionary<Symbol, Bar> theBars, Dictionary<Symbol, double> fxRates) {
            if(!theBars.ContainsKey(symbol)) return;
            var weighted = convert(positions(), p => new WeightedPosition(p));
            collector.addBar(weighted, dictionaryOne(symbol, theBars[symbol]), fxRates);
        }

        protected double profit() {
            return bridge.statistics().netProfit(this);
        }

        public override BarSpud barsMaybe() {
            return bars;
        }

        public override void addCollectorsTo(SystemArguments arguments, Dictionary<Collectible, StatisticsCollector> collectors) {
            collectors[this] = new StatisticsCollector(arguments);
        }
    }
}