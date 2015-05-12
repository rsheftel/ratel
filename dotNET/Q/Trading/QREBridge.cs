using System;
using System.Collections.Generic;
using Q.Recon;
using Q.Spuds.Core;
using Q.Trading.Results;
using Q.Util;
using systemdb.data;
using systemdb.metadata;
using System.Drawing;
using util;
using Objects=Q.Util.Objects;

namespace Q.Trading {
    public abstract class QREBridgeBase : Objects {
        public readonly SpudManager manager = new SpudManager();
        public readonly Dictionary<Symbol, Bar> partialBars= new Dictionary<Symbol, Bar>();
        public event Action barComplete;
        
        protected void fireBarComplete() {
            barComplete();
        }
        protected QREBridgeBase() {
            barComplete += doNothing;
        }

        public abstract void processBar(Dictionary<Symbol, Bar> current);
        public abstract void processTick(Symbol symbol, Tick tick);
        public abstract void orderFilled(Position position, Trade trade);
        public abstract void shutdown();
        public abstract SystemArguments arguments();
        public abstract TradeMonitor monitor();

        public abstract IEnumerable<Order> allOrders();
        public abstract List<Order> orders(Symbol symbol);
        public abstract IEnumerable<Position> allPositions();
        public abstract IEnumerable<Position> positions(Collectible collectible);

        public abstract StatisticsManager statistics();
        public abstract void processClose(Dictionary<Symbol, Bar> current);
        public abstract void cancelOrder(Order o);

        public abstract Dictionary<Symbol, BarSpud> bars();

        public abstract bool inLeadBars();
        public abstract bool inLiveMode();
        public abstract void addToPlot(Collectible collectible, PlotDefinition definition);

        public abstract MetricResults metrics();

        public abstract BarSpud bars(Symbol s);
        public abstract double slippage(Symbol s);
        public abstract void setCurrentSlippage(Dictionary<Symbol, double> newSlippage);
        public abstract Interval interval();
        public abstract bool runOnClose();

        public abstract double fxRate(Symbol symbol);

        public Dictionary<Symbol, Bar> cutBar(DateTime time) {
            var current = new Dictionary<Symbol, Bar>();
            eachKey(partialBars, symbol => current[symbol] = partialBars[symbol].update(time));
            partialBars.Clear();
            return current;
        }

        public abstract void processClose(Dictionary<Symbol, Bar> current, System systemToProcess);
    }

    public class QREBridge<S> : QREBridgeBase where S : System {
        public readonly S system;
        bool isLive;

        readonly SystemArguments arguments_;
        readonly LiveSystem liveSystem;
        readonly TradeMonitor monitor_;
        readonly StatisticsManager statistics_;
        readonly Dictionary<Symbol, BarSpud> bars_ = new Dictionary<Symbol, BarSpud>();
        Dictionary<Symbol, double> currentSlippage_;
        readonly Interval interval_;
        internal readonly LazyDictionary<Symbol, SymbolSpud<Bar>> fxRates;
        readonly  Dictionary<Symbol, DateTime> lastTickProcessed = new Dictionary<Symbol, DateTime>();

        public QREBridge(SystemArguments arguments, string topicPrefix) {
            arguments_ = arguments;
            if (arguments.runMode() == RunMode.LIVE) {
                liveSystem = arguments.liveSystem();
                monitor_ = new LiveTradeMonitor(liveSystem, arguments.symbols, topicPrefix);
            } else { 
                liveSystem = null;
                monitor_ = new TradeMonitor();
            }
            each(arguments.symbols, symbol => bars_[symbol] = new BarSpud(manager));
            system = System.create<S>(this);
            interval_ = arguments.interval();
            if(!arguments.runInNativeCurrency)
                fxRates = new LazyDictionary<Symbol, SymbolSpud<Bar>>(symbol => 
                   symbol.fxRateSymbol().bars(bars(symbol)).allowStaleTicks()
                );
            statistics_ = new StatisticsManager(system, arguments);
            arguments.logSystemCreation();
        }

        public QREBridge(SystemArguments arguments) : this(arguments, OrderTable.prefix) {}

        public override void shutdown() {
            monitor().shutdown();
            statistics().writeSTOFiles(true);
        }

        public override SystemArguments arguments() {
            return arguments_;
        }

        public override Interval interval() {
            return interval_;
        }

        public override bool runOnClose() {
            return system.runOnClose();
        }

        public override double fxRate(Symbol symbol) {
            if (arguments().runInNativeCurrency) return 1;
            return symbol.currency().isUSD() ? 1 : fxRates.get(symbol)[0].close;
        }

        public override TradeMonitor monitor() {
            return monitor_;
        }

        public override IEnumerable<Order> allOrders() {
            return system.allOrders();
        }

        public override List<Order> orders(Symbol symbol) {
            return system.orders(symbol);
        }
        
        public override IEnumerable<Position> allPositions() {
            return system.allPositions();
        }

        public override IEnumerable<Position> positions(Collectible collectible) {
            return collectible.allPositions(system);
        }

        public override StatisticsManager statistics() {
            return statistics_;
        }

        public override void processBar(Dictionary<Symbol, Bar> current) {
            if(isEmpty(current)) {
                LogC.info("no data for bar - skipping");
                return;
            }
            try {
                LogC.info("--------- " + first(current).Value.time + " --------");
                each(allPositions(), p => p.newBar());
                manager.newBar();
                updateBars(current);
                statistics().addBar(current, dictionary(current.Keys, symbol => fxRate(symbol))); // both tests should fail?
                system.processBarDO_NOT_CALL_EXCEPT_FROM_BRIDGE(current);
                manager.recalculate();
                fireBarComplete();
            } catch (Exception e) {
                LogC.info("exception caught in processBar: ", e);
                throw;
            }
        }

        void updateBars(IDictionary<Symbol, Bar> current) {
            eachKey(current, s => bars_[s].set(current[s]));
        }

        public override void processTick(Symbol symbol, Tick tick) {
             if(!partialBars.ContainsKey(symbol)) 
                    partialBars[symbol] = new Bar(tick.price, tick.price, tick.price, tick.price, tick.time);
            partialBars[symbol] = partialBars[symbol].update(tick);
            var processThisTick = system.runOnNewTick() || !lastTickProcessed.ContainsKey(symbol) || lastTickProcessed[symbol] <= tick.time.AddMinutes(-1);
            try {
                 if(processThisTick) {
                    manager.newTick();
                    if (!isLive) {
                        isLive = true;
                        manager.goLive();
                        system.goLiveDO_NOT_CALL_EXCEPT_FROM_BRIDGE();
                        monitor().goLive(system);
                    }
                    var partialBar = updateBarSpud(symbol);
                     if (Systematic.isLoggingTicks()) LogC.info("processing tick " + symbol + ": bar=" + partialBar + ", tick=" + tick);
                    system.processTickDO_NOT_CALL_EXCEPT_FROM_BRIDGE(symbol, partialBar, tick);
                    manager.recalculate();
                    lastTickProcessed[symbol] = tick.time;
                }
                monitor().tickProcessed(symbol, tick);
            } catch(Exception e) {
                LogC.info("exception caught in processTick: ", e);
                throw;
            }
        }

        Bar updateBarSpud(Symbol symbol) {
            var partialBar = partialBars[symbol];
            bars_[symbol].set(partialBar);
            bars_[symbol].lastTickedAt(partialBar.time);
            return partialBar;
        }

        public override void orderFilled(Position position, Trade trade) {
            try {
                LogC.info("SIMFILL: " + trade + " position: " + position);
                system.orderFilledDO_NOT_CALL_EXCEPT_FROM_BRIDGE(position, trade);
                statistics().addOrder(position, trade);
                manager.recalculate();
                monitor().orderFilled(position, trade, system.tradeEmail);

            } catch(Exception e) {
                LogC.info("exception caught in orderFilled: ", e);
                throw;
            }
        }

        public override void processClose(Dictionary<Symbol, Bar> current, System systemToProcess) {
            try {
                if (!system.runOnClose()) return;
                manager.newTick(); 
                updateBars(current);
                systemToProcess.processCloseDO_NOT_CALL_EXCEPT_FROM_BRIDGE(current);
            } catch(Exception e) {
                LogC.info("exception caught in processClose: ", e);
                throw;
            }
        }

        public override void processClose(Dictionary<Symbol, Bar> current) {
            processClose(current, system);
        }
      
        public override MetricResults metrics() {
            return statistics().metrics();
        }

        public override BarSpud bars(Symbol s) {
            return system.barSpud(s);
        }

        public override double slippage(Symbol s) {
            return currentSlippage_[s];
        }

        public override void setCurrentSlippage(Dictionary<Symbol, double> newSlippage) {
            currentSlippage_ = newSlippage;
        }

        public override void cancelOrder(Order o) {
            system.cancelOrder(o);
        }

        public override Dictionary<Symbol, BarSpud> bars() {
            return bars_;
        }

        public override bool inLeadBars() {
            return manager.barCount() <= arguments().leadBars;
        }

        public override bool inLiveMode() {
            return isLive;
        }

        public override void addToPlot(Collectible collectible, PlotDefinition definition) {
            arguments().fireNewPlotRequest(collectible, definition);
        }

        public void fill(Order order, double price) {
            var trade = new Trade(order, price, order.size, slippage(order.symbol), fxRate(order.symbol));
            var position = order.fill(trade);
            orderFilled(position, trade);
        }

    }

    public class PlotDefinition : Objects {
        public readonly Spud<double> spud;
        public readonly string name;
        public readonly Color color;
        public readonly string pane;

        public PlotDefinition(Spud<double> spud, string name, Color color, string pane) {
            this.spud = spud;
            this.name = name;
            this.color = color;
            this.pane = pane;
        }

        public override string ToString() {
            return name + " in " + color + (hasContent(pane) ? " on " + pane : "");
        }

        public void requireManagerMatches(BarSpud bars) {
            Bomb.unless(spud.manager.Equals(bars.manager), ()=> "spud manager does not match bar spud - time may be out of sync");
        }
    }
}
