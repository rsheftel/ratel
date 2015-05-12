using System;
using System.Collections.Generic;
using System.Text.RegularExpressions;
using Q.Spuds.Core;
using Q.Trading.Results;
using Q.Trading.Slippage;
using Q.Util;
using systemdb.data;
using systemdb.live;
using systemdb.metadata;
using tsdb;
using util;
using Date=java.util.Date;
using List=java.util.List;
using JTick = systemdb.data.Tick;
using JSymbol = systemdb.data.Symbol;
using Objects=Q.Util.Objects;
using O=Q.Util.Objects;

namespace Q.Trading {
    public class Session {
        public static readonly string DAY = "DAY";
        public static readonly string NIGHT = "NIGHT";
        public static readonly string POST_CLOSE = "POST_CLOSE";
        public static readonly string PRE_OPEN = "PRE_OPEN";
    }

    public class Symbol : O, IEquatable<Symbol>,SystemKey, Collectible {

        public static readonly Symbol NULL = new Symbol("NULLSYMBOL");
        static readonly SymbolDataCache cache = new SymbolDataCache();

        readonly string name_;
        public double bigPointValue;
        readonly int hashcode;

        public Symbol(string name, double bigPointValue) {
            name_ = name;
            this.bigPointValue = bigPointValue;
            hashcode = name.GetHashCode();
        }

        public Symbol(string name) : this(name, 1) {}

        public Symbol(JSymbol jMarket) :this(jMarket.name(), jMarket.bigPointValue()) {}
        public string name {
            get { return name_; }
        }

        public bool Equals(Symbol symbol) {
            return symbol != null && Equals(name, symbol.name);
        }

        public override bool Equals(object obj) {
            return ReferenceEquals(this, obj) || Equals(obj as Symbol);
        }

        public override int GetHashCode() {
            return hashcode;
        }

        public bool coveredBy(Dictionary<Symbol, Bar> bars) {
            return bars.ContainsKey(this);
        }

        public List<Symbol> symbols() {
            return list(this);
        }

        public override string ToString() {
            return name;
        }

        public static void clearCache() {
            cache.clear();
            Fixed.clearCache();
        }

        public Order buy(string description, OrderDetails details, long size, OrderDuration duration) {
            return new Order(description, this, details, Direction.LONG, size, duration);
        }

        public Order sell(string description, OrderDetails details, long size, OrderDuration duration) {
            return new Order(description, this, details, Direction.SHORT, size, duration);
        }

        public JSymbol javaSymbol() {
            return new JSymbol(name, bigPointValue);
        }


        public class JTickListener : TickListener {
            readonly Action<Tick> doOnTick;
            bool doOHLTicks;
            
            public JTickListener(Action<Tick> tick, bool doOHLTicks) {
                doOnTick = tick;
                this.doOHLTicks = doOHLTicks;
            }

            public void onTick(JTick tick) {
                var reTicks = new List<Tick>();
                if (tick.time.before(Dates.daysAgo(2, Dates.now()))) return;
                var tickTime = date(tick.time);
                if(doOHLTicks) {
                    reTicks.Add(new Tick(tick.open, 0, tickTime));
                    var openCloserToLow = tick.open - tick.low <= tick.high - tick.open;
                    if(openCloserToLow) {
                        reTicks.Add(new Tick(tick.low, 0, tickTime));
                        reTicks.Add(new Tick(tick.high, 0, tickTime));
                    } else {
                        reTicks.Add(new Tick(tick.high, 0, tickTime));
                        reTicks.Add(new Tick(tick.low, 0, tickTime));
                    }
                    doOHLTicks = false;
                }
                reTicks.Add(new Tick(tick.last, (ulong) tick.volume, tickTime));
                foreach (var reTick in reTicks)
                    doOnTick(reTick);
            }
        }

        public JTickListener subscribe(Action<Tick> doOnTick, bool doOHLTicks) {
            var listener = new JTickListener(doOnTick, doOHLTicks);
            cache.subscribe(this, listener);
            return listener;
        }

        public void subscribe(Action<DateTime, double> doOnTick) {
            var listener = new JObservationListener(doOnTick);
            cache.subscribe(this, listener);
        }

        public void subscribe(Action<Bar> doOnPartialBar) {
            cache.subscribe(this, new JPartialBarListener(doOnPartialBar));
        }

        public class JPartialBarListener : TickListener {
            readonly Action<Bar> doOnPartialBar;

            public JPartialBarListener(Action<Bar> doOnPartialBar) {
                this.doOnPartialBar = doOnPartialBar;
            }

            public void onTick(JTick tick) {
                doOnPartialBar(new Bar(tick));
            }
        }

        class JObservationListener : ObservationListener {
            readonly Action<DateTime, double> doOnTick;

            public JObservationListener(Action<DateTime, double> doOnTick) {
                this.doOnTick = doOnTick;
            }

            public void onUpdate(Date time, double value) {
                doOnTick(date(time), value);
            }
        }

        public SymbolSpud<Bar> bars(BarSpud spud) {
            return SymbolSpud<Bar>.bars(this, spud);
        }

        public SymbolSpud<double> doubles(BarSpud spud) {
            return SymbolSpud<double>.doubles(this, spud);
        }

        public SymbolSpud<double> doubles(BarSpud spud, double defalt) {
            return SymbolSpud<double>.doubles(this, spud, defalt);
        }

        public SymbolSpud<double> doublesNoLive(BarSpud spud) {
            return SymbolSpud<double>.doubles(this, spud, true);
        }
        
        public SymbolSpud<double> doublesNoLive(BarSpud spud, double defalt) {
            return SymbolSpud<double>.doubles(this, spud, defalt, true);
        }

        public Symbol relatedPrefix(string newPrefix) {
            return related(newPrefix, @"^[^.]+");
        }

        public Symbol related(string newPrefix, string pattern) {
            return new Symbol(Regex.Replace(name, pattern, newPrefix));
        }

        public Symbol relatedSuffix(string newSuffix) {
            return related(newSuffix, @"[^.]+$");
        }

        public Symbol relatedSuffixFromFirstDot(string newSuffix) {
            return related(newSuffix, @"\..+");
        }

        public List bars() {
            return javaSymbol().bars();
        }

        public List bars(Range range, Interval interval) {
            return interval.Equals(Interval.DAILY) 
                ? javaSymbol().bars(range) 
                : javaSymbol().bars(range, interval);
        }

        public Observations observations() {
            return javaSymbol().observations();
        }

        public string type() {
            return cache.type(this);
        }

        public double pnl(double amount, double earlierPrice, double laterPrice) {
            return bigPointValue * amount * (laterPrice - earlierPrice);
        }

        public void publish(Bar current) {
            javaSymbol().jmsLive().publish(current.jTick());
        }
        
        public DateTime processCloseOrdersTime() {
            return date(session(Session.DAY).processCloseAt());
        }

        public MarketSession session(string sessionName) {
            return cache.session(this, sessionName);
        }

        public SlippageCalculator slippageCalculator(BarSpud bars) {
            return cache.slippageCalculator(this, bars);
        }

        public void setSlippageForTest(double newSlippage) {
            overrideSlippageCalculator(typeof(Fixed));
            Fixed.setSlippageForTest(this, newSlippage);
        }

        public void setCloseTimeForTest(string closeTime, int closeOffsetSeconds) {
            cache.overrideCloseTime(this, closeTime, closeOffsetSeconds);
        }

        public void overrideSlippageCalculator(Type calculator) {
            cache.overrideSlippageCalculator(this, calculator);
        }

        List<MarketPeriod> activePeriods() {
            return cache.activePeriods(this);
        }

        public bool isPeriodStart(DateTime time) {
            var periods = activePeriods();
            return exists(
                periods, period => period.hasStart() && period.start().getTime() == jDate(time).getTime());
        }
        
        public bool isPeriodEnd(DateTime time) {
            var periods = activePeriods();
            return exists(periods, period => period.hasEnd() && period.end().getTime() == jDate(time).getTime());
        }

        public bool isPeriodInactive(DateTime time) {
            var periods = activePeriods();
            return !exists(periods, period => period.range().containsInclusive(jDate(time)));
        }

        public static void clearPeriodsCache() {
            cache.clearActivePeriods();
        }

        public Order buySell(Direction direction, string description, OrderDetails details, long size, OrderDuration duration) {
            return direction.order(description, this, details, size, duration);
        }

        public DateTime closeAt() {
            return date(cache.session(this, Session.DAY).close());
        }

        public void addOrder(StatisticsCollector collector, Position position, Trade trade) {
            if(!collects(position)) return;
            collector.addOrder(new WeightedPosition(position), new WeightedTrade(trade));
        }

        public void addBar(StatisticsCollector collector, System system, Dictionary<Symbol, Bar> bars, Dictionary<Symbol, double> fxRates) {
            if(!bars.ContainsKey(this)) return;
            collector.addBar(convert(system.positions(this), p => new WeightedPosition(p)), dictionaryOne(this, bars[this]), fxRates);
        }

        public void addCollectorsTo(SystemArguments arguments, Dictionary<Collectible, StatisticsCollector> collectors) {
            collectors[this] = new StatisticsCollector(arguments);
        }

        public IEnumerable<Position> allPositions(System system) {
            return system.positions(this);
        }

        public BarSpud barsMaybe() {
            return null;
        }

        public bool collects(Position position) {
            return position.symbol.Equals(this);
        }

        public bool collects(Trade trade) {
            return trade.order().symbol.Equals(this);
        }

        public Currency currency() {
            return cache.currency(this);
        }

        public void setCurrencyForTest(string currency) {
            cache.setCurrencyForTest(this, currency);
        }

        public Symbol fxRateSymbol() {
            return currency().fxRateSymbol();
        }

        public void setBigPointValue(double contractSize) {
            bigPointValue = contractSize;
        }

        public string bloombergTicker() {
            return Bomb.ifNull(cache.bloombergTicker(this), () => "no row defined in MarketTickers for " + this);
        }

        public bool hasBloombergTicker() {
            return cache.bloombergTicker(this) != null;
        }

        public void setTypeForTest(string newType) {
            cache.setTypeForTest(this, newType);
        }
    }

    internal class SymbolDataCache : Objects {
        readonly LazyDictionary<Symbol, Type> slippageCalculators = new LazyDictionary<Symbol, Type>(
            symbol => Type.GetType(MarketTable.MARKET.slippageCalculator(symbol.name), true, false)
        );
        readonly LazyDictionary<Symbol, Currency> currencies = new LazyDictionary<Symbol, Currency>(
            symbol => new Currency(Bomb.ifNull(symbol.javaSymbol().currency(), () => "no currency for " + symbol.javaSymbol()))
        );
        readonly LazyDictionary<Symbol, string> bloombergTickers = new LazyDictionary<Symbol, string>(
            symbol => MarketTickersTable.TICKERS.has(symbol.name) ? MarketTickersTable.TICKERS.lookup(symbol.name) : null
        );
        readonly LazyDictionary<Symbol, string> types = new LazyDictionary<Symbol, string>(
            symbol => symbol.javaSymbol().type()
        );
        readonly LazyDictionary<Symbol, MasterTickListener> tickListeners = new LazyDictionary<Symbol, MasterTickListener>(
            symbol => new MasterTickListener(symbol)
        );
        readonly LazyDictionary<Symbol, MasterObservationListener> observationListeners = new LazyDictionary<Symbol, MasterObservationListener>(
            symbol => new MasterObservationListener(symbol)
        );

        readonly LazyDictionary<Symbol, LazyDictionary<string, MarketSession>> sessions = 
            new LazyDictionary<Symbol, LazyDictionary<string, MarketSession>>(
                symbol => new LazyDictionary<string, MarketSession>(
                    name => MarketSessionTable.SESSION.forName(symbol.name, name)
                )
            );
        readonly LazyDictionary<Symbol, List<MarketPeriod>> activePeriods_ = 
            new LazyDictionary<Symbol, List <MarketPeriod>>(
                symbol => list<MarketPeriod>(MarketHistoryTable.MARKET_HISTORY.activePeriods(symbol.name))
            );

        public List<MarketPeriod> activePeriods(Symbol symbol) { return activePeriods_.get(symbol); }
        public MarketSession session(Symbol symbol, string name) { return sessions.get(symbol).get(name); }
        Type slippageCalculator(Symbol symbol) { return slippageCalculators.get(symbol); }

        public void clearActivePeriods() {
            activePeriods_.clear();
        }

        public void overrideSlippageCalculator(Symbol symbol, Type calculator) {
            slippageCalculators.overwrite(symbol, calculator);
        }

        public SlippageCalculator slippageCalculator(Symbol symbol, BarSpud bars) {
            var calculator = slippageCalculator(symbol);
            var constructor = Bomb.ifNull(
                calculator.GetConstructor(new[] {typeof(Symbol), typeof(BarSpud)}),
                () => "Could not find slippage constructor for symbol " + this + ", matching " + calculator.FullName + "(Symbol, BarSpud)"
            );
            return (SlippageCalculator) constructor.Invoke(new object[] {symbol, bars});
        }

        public void clear() {
            LogC.note("refactor SDC to be a lazydictionary of symbol=> CacheData");
            activePeriods_.clear();
            sessions.clear();
            slippageCalculators.clear();
            currencies.clear();
            bloombergTickers.clear();
            types.clear();
            tickListeners.clear();
            observationListeners.clear();
        }

        public void overrideCloseTime(Symbol symbol, string time, int offset) {
            MarketSessionTable.SESSION.update(symbol.name, Session.DAY, time, offset);
            sessions.remove(symbol);
        }

        public Currency currency(Symbol symbol) {
            return Bomb.ifNull(currencies.get(symbol), () => "can't get currency for " + symbol);
        }

        public void setCurrencyForTest(Symbol symbol, string currency) {
            currencies.overwrite(symbol, new Currency(currency));
        }

        public string bloombergTicker(Symbol symbol) {
            return bloombergTickers.get(symbol);
        }

        public string type(Symbol symbol) {
            return types.get(symbol);
        }

        public void setTypeForTest(Symbol symbol, string newType) {
            types.overwrite(symbol, newType);
        }

        public void subscribe(Symbol symbol, TickListener listener) {
            tickListeners.get(symbol).add(listener);
        }

        public void subscribe(Symbol symbol, ObservationListener listener) {
            observationListeners.get(symbol).add(listener);
        }
    }

    class MasterTickListener : Objects, TickListener {
        readonly List<TickListener> listeners = new List<TickListener>();
        readonly Symbol symbol;
        JTick lastTick;

        public MasterTickListener(Symbol symbol) {
            this.symbol = symbol;
            symbol.javaSymbol().subscribe(this);
        }

        public void onTick(JTick t) {
            lastTick = t;
            each(listeners, listener => safeProcessTick(listener, t));
        }

        void safeProcessTick(TickListener listener, JTick t) {
            try {
                listener.onTick(t);
            } catch (Exception e) {
                LogC.err(symbol.name + " failed processing " + t, e);
            }
        }

        public void add(TickListener listener) {
            listeners.Add(listener);
            if(lastTick != null)
                safeProcessTick(listener, lastTick);
        }
    }

    class MasterObservationListener : O, ObservationListener {
        readonly Symbol symbol;
        readonly List<ObservationListener> listeners = new List<ObservationListener>();
        Date lastDate;
        double lastValue = double.NaN;

        public MasterObservationListener(Symbol symbol) {
            this.symbol = symbol;
            symbol.javaSymbol().subscribe(this);
        }

        public void onUpdate(Date date, double value) {
            lastValue = value;
            lastDate = date;
            each(listeners, listener => safeProcessUpdate(listener, date, value));
        }

        void safeProcessUpdate(ObservationListener listener, Date date, double value) {
            try {
                listener.onUpdate(date, value);
            } catch (Exception e) {
                LogC.err(symbol.name + " failed processing observation " + ymdHuman(date) + ": " + value, e);
            }
        }
                
        public void add(ObservationListener listener) {
            listeners.Add(listener);
            if(lastDate != null)
                safeProcessUpdate(listener, lastDate, lastValue);
        }
    }
}