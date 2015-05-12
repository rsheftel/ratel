using System;
using System.Collections.Generic;
using file;
using java.util;
using Q.Spuds.Core;
using Q.Trading;
using Q.Trading.Results;
using Q.Util;
using systemdb.data;
using util;
using Bar=Q.Trading.Bar;
using JBar = systemdb.data.Bar;
using Symbol=Q.Trading.Symbol;
using Tick=Q.Trading.Tick;
using Timer=System.Threading.Timer;

namespace Q.Simulator {
    public class Simulator : Util.Objects {
        readonly BarLoader data;
        internal readonly QREBridgeBase bridge;
        public readonly List<Symbol> symbols;
        int dateIndex;
        bool gottenTicks;
        bool hasRunLiveClose;
        Dictionary<Symbol, Bar> partialBars { get { return bridge.partialBars; }}
        internal readonly List<Trade> trades = new List<Trade>();
        readonly Dictionary<Symbol, Date> lastClose = new Dictionary<Symbol, Date>();
        readonly LazyDictionary<Symbol, Action<Bar>> newBarListeners;
        Timer cutBarTimer;
        readonly Interval interval;
        long marketBarsProcessed;
        DateTime lastCutBarTime;

        event Action processBarComplete;

        public Simulator(SystemArguments args, BarLoader data, string topicPrefix) {
            newBarListeners = new LazyDictionary<Symbol, Action<Bar>>(symbol => doNothing);
            this.data = data;
            symbols = args.symbols;
            bridge = args.bridgeBase(topicPrefix);
            dateIndex = 0;
            processBarComplete += doNothing;
            interval = args.interval();
        }

        public Simulator(SystemArguments args, string topicPrefix) : this(args, new SystemDbBarLoader(args.interval(), args.symbols), topicPrefix) {}

        public void nextBar() {
            var date = data.date(dateIndex);
            var current = data.currentBars(date);
            marketBarsProcessed += current.Count;
            nextBar(date, current, false);
        }

        void nextBar(DateTime date, Dictionary<Symbol, Bar> current, bool isLive) {
            trades.Clear();
            if(!isLive) {  // in live mode, we have already filled orders for the bar we just went through (in the tick processing logic) 
                bridge.setCurrentSlippage(data.currentSlippages(date));
                LogC.info("filling orders for bar: " + ymdHuman(date));
                fillOrders(current);
            }
            LogC.info("calling onNewBar for bar: " + ymdHuman(date));
            bridge.processBar(current);
            processBarComplete();
            each(current, (symbol, bar) => { var action = newBarListeners.get(symbol); action(bar); });
            dateIndex++;
        }

        void fillOrders(Dictionary<Symbol, Bar> current) {
            each(current, fillOrdersOpen);
            each(current, fillOrdersHighLow);
            processCloseMaybe(current);
            each(current, fillOrdersClose);
        }

        void processCloseMaybe(Dictionary<Symbol, Bar> current) {
            if(interval.isDaily()) {
                bridge.processClose(current);
                return;
            }
            var closesToProcess = new Dictionary<Symbol, Bar>();

            eachKey(current, symbol => {
                var barTime = jDate(current[symbol].time);
                var barCloseTime = symbol.session(Session.DAY).processCloseAt(Dates.midnight(barTime));
                if (lastClose.ContainsKey(symbol) && barCloseTime.Equals(lastClose[symbol])) return; // already done the close for this bar
                if (barTime.before(barCloseTime)) return; // too early to run the close.
                closesToProcess[symbol] = current[symbol];
                lastClose[symbol] = barCloseTime;   
            });
            bridge.processClose(closesToProcess);
             
        }

        void fillOrdersOpen(Symbol symbol, Bar bar) {
            fillOrders(symbol, new Tick(bar.open, 0, bar.time), order => order.fillPrice(bar.open, true), false);
        }

        void fillOrdersHighLow(Symbol symbol, Bar bar) {
            var highLow = bar.orderedHighLow();
            fillOrdersNotOpen(symbol, new Tick(highLow[0], 0, bar.time), false);
            fillOrdersNotOpen(symbol, new Tick(highLow[1], 0, bar.time), false);
        }

        void fillOrdersNotOpen(Symbol symbol, Tick tick, bool isClose) {
            fillOrders(symbol, tick, order => order.fillPrice(tick.price, false), isClose);
        }

        void fillOrdersClose(Symbol symbol, Bar bar) {
            fillOrdersNotOpen(symbol, new Tick(bar.close, 0, bar.time), true);
        }

        void fillOrders(Symbol symbol, Tick tick, Converter<Order, double> fillPrice, bool isClose) {
            var fillable = accept(orders(symbol), order=> isFillable(order, symbol, tick.price, isClose));
            fillable = sort(fillable, (a, b) => {
                var fillA = Math.Abs(tick.price - fillPrice(a));
                var fillB = Math.Abs(tick.price - fillPrice(b));
                var priceOrder = fillB.CompareTo(fillA);
                return priceOrder != 0 ? priceOrder : a.id.CompareTo(b.id);
            });
            each(fillable, order => fillOrderMaybe(order, tick, fillPrice, isClose));
        }

        static bool isFillable(Order order, Symbol symbol, double tick, bool isClose) {
            var matches = order.matches(symbol);
            var canFill = order.canFill(tick, isClose);
            return matches && canFill;
        }

        void fillOrderMaybe(Order order, Tick tick, Converter<Order, double> fillPrice, bool isClose) {
            if(order.canFill(tick.price, isClose)) fillAt(order, fillPrice(order), tick.time);
        }

        void fillAt(Order order, double fillPrice, DateTime time) {
            var trade = new Trade(order, fillPrice, order.size, bridge.slippage(order.symbol), time, bridge.fxRate(order.symbol));
            var position = order.fill(trade);
            trades.Add(trade);
            bridge.orderFilled(position, trade);
        }

        public IEnumerable<Order> allOrders() {
            return bridge.allOrders();
        }

        public List<Order> orders(Symbol symbol) {
            return bridge.orders(symbol);
        }

        public IEnumerable<Position> allPositions() {
            return bridge.allPositions();
        }

        public IEnumerable<Position> positions(Collectible collectible) {
            return bridge.positions(collectible);
        }

        public bool hasNextBar() {
            return dateIndex < data.numDates();
        }

        public DateTime currentDate() {
            return data.date(dateIndex-1);
        }

        public double pnl() {
            return bridge.statistics().netProfit();
        }
        
        public double pnl(Collectible collectible) {
            return bridge.statistics().netProfit(collectible);
        }

        public StatisticsCollector collector(Collectible collectible) {
            return bridge.statistics().collector(collectible);
        }
        
        public StatisticsCollector allCollector() {
            return bridge.statistics().allCollector();
        }

        public double processBars() {
            var start = reallyNow();
            marketBarsProcessed = 0;
            while(hasNextBar()) nextBar();
            var seconds = reallyNow().Subtract(start).TotalSeconds;
            var marketBarsPerSecond = marketBarsProcessed/seconds;
            LogC.info("Processed " + marketBarsProcessed + " marketBars in " + seconds + " seconds. (" + Strings.nDecimals(2, marketBarsPerSecond) + " marketBars/sec)");
            return marketBarsPerSecond;
        }        
        
        public void processBars(int count) {
            zeroTo(count, i => nextBar());
        }

        public void waitForTick() {
            wait(() => gottenTicks);
            gottenTicks = false;
        }

        public void goLive() {
            each(symbols, symbol => symbol.subscribe(tick => processTick(tick, symbol), interval.isDaily()));
            lastCutBarTime = now();
            setCutBarTimer();
        }

        void setCutBarTimer() {
            var nextTime = nextBarTime();
            timerManager().atTime(nextTime, cutBar, out cutBarTimer);
            lastCutBarTime = nextTime;
        }

        DateTime nextBarTime() {
            return date(interval.nextBoundary(jDate(lastCutBarTime)));
        }

        void cutBar() {
            lock(partialBars) {
                var time = now();
                nextBar(time, bridge.cutBar(time), true);
                setCutBarTimer();
            }
        }

        internal void processTick(Tick tick, Symbol symbol) {
            lock(partialBars) {
                trades.Clear();
                var price = tick.price;
                var runClose = bridge.runOnClose() && !hasRunLiveClose && now().CompareTo(symbol.closeAt()) >= 0;
                fillOrders(symbol, tick, order => price, runClose);
               bridge.processTick(symbol, tick);
                gottenTicks = true;
                if (runClose) hasRunLiveClose = true;
            }
        }

        public void shutdown() {
            bridge.shutdown();
        }

        public void writeCurveFiles(QDirectory directory) {
            bridge.statistics().writeCurveFiles(bridge.arguments().liveSystem(), directory);
        }

        public void dumpContents(string filename) {
            new QFile(filename).overwrite("" + pnl());
        }

        public T theSymbolSystem<T>(Symbol symbol) where T : SymbolSystem {
            var typedBridge = (QREBridge<IndependentSymbolSystems<T>>) bridge;
            var multiSymbol = typedBridge.system;
            return the(multiSymbol.systems(symbol));
        }

        public void waitForBar() {
            var gottenBars = false;
            Action gotBars = () => gottenBars = true;
            processBarComplete += gotBars;
            wait(() => gottenBars);
            processBarComplete -= gotBars;
        }

        public void clearTimer() {
            cutBarTimer.Dispose();
            cutBarTimer = null;
        }

        public BarSpud bars(Symbol s) {
            return bridge.bars(s);
        }

        public MetricResults metrics() {
            return bridge.metrics();
        }

        public void addNewTradeListener(Action<Position, Trade> onNewTrade) {
            bridge.statistics().onNewTrade += onNewTrade;
        }

        public List<double> equity(Collectible collectible) {
            return cumulativeSum(bridge.statistics().collector(collectible).pnl());
        }

        public List<DateTime> dates(Collectible collectible) {
            return bridge.statistics().collector(collectible).dates();
        }

        public double slippage(Symbol symbol) {
            return bridge.slippage(symbol);
        }

        public IEnumerable<Collectible> collectibles() {
            return bridge.statistics().collectibles();
        }

        public bool runInNativeCurrency() {
            return bridge.arguments().runInNativeCurrency;
        }

        public double fxRate(Symbol symbol) {
            return bridge.fxRate(symbol);
        }

        public void addCollectible(Collectible collectible) {
            bridge.statistics().addCollectible(collectible);
        }

        public void addNewBarListener(Symbol symbol, Action<Bar> onNewBar) {
            newBarListeners.overwrite(symbol, newBarListeners.get(symbol) + onNewBar);
        }

        public void removeNewBarListener(Symbol symbol, Action<Bar> onNewBar) {
            newBarListeners.overwrite(symbol, newBarListeners.get(symbol) - onNewBar);           
        }

        public double pnlForPosition(Position position) {
            return pnlForPosition(position, slippage(position.symbol));
        }

       public double pnlForPosition(Position position, double currentSlippage) {
            return position.pnlWithSlippage(bars(position.symbol)[0].close, currentSlippage, runInNativeCurrency(), fxRate(position.symbol));
        }

    }

    public class SystemDbBarLoader : Util.Objects, BarLoader {
        readonly Dictionary<Symbol, Range> ranges;
        readonly Dictionary<Symbol, Dictionary<DateTime, Bar>> bars_;
        readonly List<DateTime> dates;        
        readonly LazyDictionary<DateTime, Dictionary<Symbol, double>> slippageCache = new LazyDictionary<DateTime, Dictionary<Symbol, double>>(date => new Dictionary<Symbol, double>());


        public SystemDbBarLoader(Interval interval, IEnumerable<Symbol> symbols) 
            : this(interval, symbols, dictionary(symbols, s => Range.allTime())) {
        }

        public SystemDbBarLoader(Interval interval, IEnumerable<Symbol> symbols, DateTime start) 
            : this(interval, symbols, dictionary(symbols, s => new Range(jDate(start), null))) {}

        public SystemDbBarLoader(Interval interval, IEnumerable<Symbol> symbols, DateTime start, DateTime end) 
            : this(interval, symbols, dictionary(symbols, s => Range.range(ymdHuman(start), ymdHuman(end)))) {}

        public SystemDbBarLoader(Interval interval, IEnumerable<Symbol> symbols, Dictionary<Symbol, Range> ranges) {
            this.ranges = ranges;
            var symbolBars = list(convert(symbols, symbol => barList(symbol, interval)));
            var symbolBarsByDate = convert(symbolBars, barList => dictionary(times(barList), barList));
            bars_ = dictionary(symbols, symbolBarsByDate);
            dates = sort(unique(collect<IEnumerable<Bar>, DateTime>(symbolBars, times)));
            each(symbols, populateSlippageCache);
        }

        void populateSlippageCache(Symbol symbol) {
            var manager = new SpudManager();
            var bars = bars_[symbol];
            var spud = new BarSpud(manager);
            var calculator = symbol.slippageCalculator(spud);
            each(sort(bars.Keys), date => {
                spud.set(bars[date]);
                slippageCache.get(date)[symbol] = calculator.slippage();
                manager.newBar();
            });
        }

        IEnumerable<Bar> barList(Symbol symbol, Interval interval) {
            var jBars = list<JBar>(symbol.bars(ranges[symbol], interval));
            try {
                return convert(jBars, jbar => new Bar(jbar, false));
            } catch(Exception e) {
                throw Bomb.toss("error loading bars for symbol: " + symbol, e);
            }
        }

        static IEnumerable<DateTime> times(IEnumerable<Bar> barList) {
            return convert(barList, bar => bar.time);
        }

        public DateTime date(int dateIndex) {
            return dates[dateIndex];
        }
        
        public Dictionary<Symbol, Bar> currentBars(DateTime date) {
            var entriesWithCurrentBars = accept(bars_, (symbol, bars) => bars.ContainsKey(date));
            return dictionary(
                convert(entriesWithCurrentBars, entry => entry.Key), 
                convert(entriesWithCurrentBars, entry => entry.Value[date])
            );
        }

        public int numDates() {
            return dates.Count;
        }

        public Dictionary<Symbol, double> currentSlippages(DateTime date) {
            return slippageCache.get(date);
        }

    }
}
