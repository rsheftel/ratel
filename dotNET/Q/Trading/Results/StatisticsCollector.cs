using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using file;
using Q.Util;
using systemdb.metadata;

namespace Q.Trading.Results {
    public class StatisticsCollector : Objects {
        readonly SystemArguments arguments;
        readonly List<DateTime> dates_ = new List<DateTime>();
        readonly List<double> pnl_ = new List<double>();
        readonly List<double> positions_ = new List<double>();
        readonly List<WeightedTrade> currentTrades = new List<WeightedTrade>();
        Dictionary<Symbol, double> lastPositions = new Dictionary<Symbol, double>();
        readonly Dictionary<Symbol, double> lastCloses = new Dictionary<Symbol, double>();
        int open;
        readonly Dictionary<string, TradeTracker> trackers = new Dictionary<string, TradeTracker>();
        static readonly Metrics metrics_ = new Metrics();
        List<Metrics.Drawdown> allDrawdowns_;

        public StatisticsCollector(SystemArguments arguments) {
            this.arguments = arguments;
            trackers.Add("ALL", new TradeTracker((position, pnl) => true));
            trackers.Add("WINNING", new TradeTracker((position, pnl) => isWin(pnl)));
            trackers.Add("LOSING", new TradeTracker((position, pnl) => isLoser(pnl)));
            trackers.Add("NEUTRAL", new TradeTracker((position, pnl) => pnl == 0));
            trackers.Add("LONG_WINNING", new TradeTracker((position, pnl) => isWin(pnl)   && position.entry().isLong()));
            trackers.Add("LONG_LOSING", new TradeTracker((position, pnl) => isLoser(pnl) && position.entry().isLong()));     
            trackers.Add("SHORT_WINNING", new TradeTracker((position, pnl) => isWin(pnl)   && position.entry().isShort()));
            trackers.Add("SHORT_LOSING", new TradeTracker((position, pnl) => isLoser(pnl) && position.entry().isShort()));
        }

        public StatisticsCollector(IEnumerable<DateTime> dates, IEnumerable<double> pnl, IEnumerable<double> position) : this(null) {
            dates_.AddRange(dates);
            pnl_.AddRange(pnl);
            positions_.AddRange(position);
        }

        public void addBar(IEnumerable<WeightedPosition> pos, Dictionary<Symbol, Bar> bars, Dictionary<Symbol, double> fxRates) {
            pnl_.Add(positionPnl(bars, fxRates) + tradePnl(bars, fxRates));
            dates_.Add(first(bars.Values).time);
            positions_.Add(totalPosition(pos));
            lastPositions = positionMap(pos);
            currentTrades.Clear();
        }

        static double totalPosition(IEnumerable<WeightedPosition> positions) {
            if(isEmpty(positions)) return 0;
            var symbol = first(positions).symbol();
            var sum = 0.0;
            each(positions, position => sum += position.symbol().Equals(symbol) ? position.amount() : double.NaN);
            return sum;
        }

        static Dictionary<Symbol, double> positionMap(IEnumerable<WeightedPosition> pos) {
            var result = new Dictionary<Symbol, double>();
            each(pos, p => { if(!result.ContainsKey(p.symbol())) result[p.symbol()] = 0.0; result[p.symbol()] += p.amount(); });
            return result;
        }
   
        double tradePnl(IDictionary<Symbol, Bar> bars, IDictionary<Symbol, double> fxRates) {
            var result = 0.0;
            foreach (var trade in currentTrades) {
                var symbol = trade.symbol();
                var close = bars.ContainsKey(symbol) ? fxAdjustedClose(symbol, bars[symbol], fxRates): lastCloses[symbol];
                result += trade.pnl(close, arguments.runInNativeCurrency);
            }
            return result;
        }

        double fxAdjustedClose(Symbol symbol, Bar bar, IDictionary<Symbol, double> fxRates) {
            if (arguments.runInNativeCurrency) return bar.close;
            return bar.close * fxRates[symbol];
        }

        double positionPnl(IDictionary<Symbol, Bar> bars, IDictionary<Symbol, double> fxRates) {
            var result = 0.0;
            foreach(var symbol in lastPositions.Keys) {
                if(!bars.ContainsKey(symbol)) continue;
                var bar = bars[symbol];
                var current = fxAdjustedClose(symbol, bar, fxRates);
                var previous = lastCloses[symbol];
                result += symbol.pnl(lastPositions[symbol], previous, current);
            }
            each(bars, (symbol, bar) => lastCloses[symbol] = fxAdjustedClose(symbol, bar, fxRates));
            return result;
        }

        public double netProfit() { return Metrics.NET_PROFIT(this); }


        public void writeSTOFiles(string marketName, bool doMetricFiles) {
            if (arguments.parameters.isSTO()) {
                var path = arguments.curveDir(marketName);
                LogC.info("write sto curve file on " + path);
                CurveFiles.writeOne(path, dates_, pnl_, positions_);
                if(doMetricFiles)
                    MetricFiles.writeOne(arguments.systemId(), marketName, arguments.parameters.runNumber(), metrics());
            } else if(arguments.parameters.isCloudSTO()) {
                CurveFiles.writeToS3(arguments.systemId(), marketName, arguments.parameters.runNumber(), dates_, pnl_, positions_);
            }
        }
  
        public void writeCurveFile(Collectible collectible, LiveSystem liveSystem, QDirectory directory) {
            CurveFiles.writeOne(directory.file(liveSystem.fileName(collectible.name) + ".bin").path(), dates_, pnl_, positions_);
        }



        static bool isLoser(double pnl) {
            return pnl < 0;
        }

        static bool isWin(double pnl) {
            return pnl > 0;
        }

        public void addOrder(WeightedPosition position, WeightedTrade trade) {
            currentTrades.Add(trade);
            if (position.isEntry(trade)) { open++; return; } 
            if (!position.isClosed()) return;
            var profit = position.pnl(true, arguments.runInNativeCurrency);
            each(trackers.Values, tracker => tracker.addMaybe(position, profit));
            open--;
        }

        public Dictionary<string, double> metrics() {
            return metrics_.values(this);
        }

        public TradeTracker neutral() {
            return trackers["NEUTRAL"];
        }

        public TradeTracker longWinning() {
            return trackers["LONG_WINNING"];
        }

        public TradeTracker longLosing() {
            return trackers["LONG_LOSING"];
        }

        public TradeTracker shortWinning() {
            return trackers["SHORT_WINNING"];
        }

        public TradeTracker shortLosing() {
            return trackers["SHORT_LOSING"];
        }

        public TradeTracker all() {
            return trackers["ALL"];
        }

        public TradeTracker losing() {
            return trackers["LOSING"];
        }

        public TradeTracker winning() {
            return trackers["WINNING"];
        }

        public int openTrades() {
            return open;
        }

        public List<double> pnl() {
            return pnl_;
        }

        public List<DateTime> dates() {
            return dates_;
        }

        public ICollection<double> positions() {
            return positions_;
        }

        public void cacheAllDrawdowns(List<Metrics.Drawdown> drawdowns) {
            allDrawdowns_ = drawdowns;
        }

        public ReadOnlyCollection<Metrics.Drawdown> allDrawdowns() {
            return allDrawdowns_.AsReadOnly();
        }
    }

    public class WeightedTrade {
        readonly Trade t;
        readonly double weight;

        public WeightedTrade(Trade t) : this (t, 1.0) {}
        public WeightedTrade(Trade t, double weight) {
            this.t = t;
            this.weight = weight;
        }

        public Symbol symbol() {
            return t.order().symbol; // do not use order for anything else, it is unscaled.
        }

        public double pnl(double close) {
            return pnl(close, false);
        }

        public double size() {
            return t.size * weight;
        }

        public bool isEntryOn(Position p) {
            return p.isEntry(t);
        }

        public bool Equals(WeightedTrade obj) {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            return Equals(obj.t, t) && obj.weight == weight;
        }

        public override bool Equals(object obj) {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            return obj.GetType() == typeof (WeightedTrade) && Equals((WeightedTrade) obj);
        }

        public override int GetHashCode() {
            unchecked {
                return (t.GetHashCode() * 397)^weight.GetHashCode();
            }
        }

        public bool isLong() {
            return t.isLong();
        }

        public bool isShort() {
            return t.isShort();
        }

        public double pnl(double close, bool runInNativeCurrency) {
            return t.pnl(close, runInNativeCurrency) * weight;
        }
    }

    public class WeightedPosition {
        readonly Position p;
        readonly double weight;

        public WeightedPosition(Position p) : this (p, 1.0) {}
        public WeightedPosition(Position p, double weight) {
            this.p = p;
            this.weight = weight;
        }

        public Symbol symbol() {
            return p.symbol;
        }

        public double amount() {
            return p.amount * weight;
        }

        public bool isEntry(WeightedTrade trade) {
            return trade.isEntryOn(p);
        }

        public bool isClosed() {
            return p.isClosed();
        }

        public double pnl(bool applySlippage, bool runInNativeCurrency) {
            return weight * p.pnl(applySlippage, runInNativeCurrency);
        }

        public int barsHeld() {
            return p.barsHeld();
        }

        public WeightedTrade entry() {
            return new WeightedTrade(p.entry(), weight);
        }

        public bool Equals(WeightedPosition obj) {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            return Equals(obj.p, p) && obj.weight == weight;
        }

        public override bool Equals(object obj) {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            return obj.GetType() == typeof (WeightedPosition) && Equals((WeightedPosition) obj);
        }

        public override int GetHashCode() {
            unchecked {
                return (p.GetHashCode() * 397)^weight.GetHashCode();
            }
        }

        public double slippage() {
            return weight * p.slippage();
        }
    }
}