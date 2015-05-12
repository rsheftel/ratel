using System;
using System.Collections.Generic;
using java.util;
using Q.Trading;
using Q.Util;

namespace Q.Spuds.Core {
    public abstract class SymbolSpud<T> : Spud<T> {
        readonly Symbol symbol;
        readonly bool noLiveUpdates;
        readonly BarSpud bars_;
        readonly Dictionary<DateTime, T> data;
        readonly List<DateTime> datesReverse;
        bool inTestMode;
        protected bool inLive;
        protected DateTime lastTickedAt;
        protected object tickLock = new object();

        static readonly Dictionary<Symbol, IEnumerable<DateTime>> datesCache = new Dictionary<Symbol, IEnumerable<DateTime>>();
        static readonly Dictionary<Symbol, IEnumerable<Bar>> barsCache = new Dictionary<Symbol, IEnumerable<Bar>>();
        static readonly Dictionary<Symbol, List<double>> valuesCache = new Dictionary<Symbol, List<double>>();
        bool allowStaleTicks_;

        protected SymbolSpud(Symbol symbol, BarSpud bars, IEnumerable<DateTime> dates, IEnumerable<T> ts) : this(symbol, bars, dates, ts, false) {}

        protected SymbolSpud(Symbol symbol, BarSpud bars, IEnumerable<DateTime> dates, IEnumerable<T> ts, bool noLiveUpdates) : base(bars.manager) {
            this.symbol = symbol;
            this.noLiveUpdates = noLiveUpdates;
            bars_ = dependsOn(bars);
            if(!noLiveUpdates)
                manager.onLive += doSubscribe;
            data = dictionary(dates, ts);
            datesReverse = list(dates);
            datesReverse.Reverse();
        }

        public override string ToString() {
            return "SymbolSpud@" + symbol; // if you try to realize a spud value here, you will break stuff.
        }

        protected override T calculate() {
            var target = bars_[0].time;
            if(data.ContainsKey(target)) return data[target];
            var date = datesReverse.Find(d => d < target);
            if(date.Equals(default(DateTime))) Bomb.toss("no initializedValue for " + symbol.name + " <= date " + target);
            return data[date];
        }
        
        bool hasValidData() {
            var target = bars_[0].time;
            if(data.ContainsKey(target)) return true;
            return datesReverse.Find(d => d < target) != default(DateTime);
        }

        public override T this[int index] {
            get {
                if (index != 0 || !inLive || !bars_.hasTicked() || noLiveUpdates) return base[index];
                // if we are dirty, then we need to be careful not to overwrite the live value with last night's
                beClean();
                var tickTime = bars_.lastTickedAt();
                try { if(!allowStaleTicks_) wait(50, 10, () => tickTime.CompareTo(lastTickReceived()) < 1); } 
                catch {
                    LogC.info("dropping tick @"  + ymdHuman(bars_[0].time) + 
                        " symbol " + symbol.name + " has not ticked since " + ymdHuman(lastTickedAt));
                    throw new AbortBar("stale data in symbol spud " + this);
                }
                return base[index];
            }
        }

        DateTime lastTickReceived() {
            lock(tickLock) {
                return lastTickedAt;
            }
        }

        public override bool canUpdate() {
            return base.canUpdate() && (isInitialized() || hasValidData());
        }

        public void doSubscribe() {
            inLive = true;
            subscribe();
        }

        protected abstract void subscribe();
        
        public void enterTestMode() {
            Bomb.when(inTestMode, () => "cannot enter testmode when in testmode!");
            datesReverse.Clear();
            data.Clear();
            inTestMode = true;
        }

        public static SymbolSpud<Bar> bars(Symbol symbol, BarSpud spud) {
            if(!barsCache.ContainsKey(symbol)) {
                var jbars = list<systemdb.data.Bar>(symbol.bars());
                barsCache[symbol] = convert(jbars, jbar => new Bar(jbar));
                datesCache[symbol] = convert(barsCache[symbol], bar => bar.time);
            }
            return new SymbolBarSpud(symbol, spud, datesCache[symbol], barsCache[symbol]);
        }
        
        public static SymbolSpud<double> doubles(Symbol symbol, BarSpud spud) {
            return doubles(symbol, spud, null);
        }

        public static SymbolSpud<double> doubles(Symbol symbol, BarSpud spud, double? defalt) {
            return doubles(symbol, spud, defalt, false);
        }

        public static SymbolSpud<double> doubles(Symbol symbol, BarSpud spud, double? defalt, bool noLiveUpdates) {
            if(!valuesCache.ContainsKey(symbol)) {
                var observations = symbol.observations();
                datesCache[symbol] = convert<Date, DateTime>(list<Date>(observations.times()), date);
                valuesCache[symbol] = list(observations.values());
            }
            return new SymbolValueSpud(symbol, spud, datesCache[symbol], valuesCache[symbol], defalt, noLiveUpdates);
        }

        public static SymbolSpud<double> doubles(Symbol symbol1, BarSpud spud, bool noLiveUpdates) {
            return doubles(symbol1, spud, null, noLiveUpdates);
        }

        class SymbolValueSpud : SymbolSpud<double> {
            readonly double? defalt;

            public SymbolValueSpud(Symbol symbol, BarSpud spud, IEnumerable<DateTime> dates, IEnumerable<double> bars, double? defalt, bool noLiveUpdates) 
                : base(symbol, spud, dates, bars, noLiveUpdates) {
                this.defalt = defalt;
            }

            protected override double calculate() {
                if (defalt == null) return base.calculate();
                return hasValidData() ? base.calculate() : (double) defalt;
            }

            protected override void subscribe() {
                symbol.subscribe(delegate(DateTime time, double value) {
                    lock(tickLock) {
                        lastTickedAt = time;
                        set(value);
                    }
                });
            }
        }

        class SymbolBarSpud : SymbolSpud<Bar> {
            public SymbolBarSpud(Symbol symbol, BarSpud spud, IEnumerable<DateTime> dates, IEnumerable<Bar> bars) 
                : base(symbol, spud, dates, bars) {}

            protected override void subscribe() {
                symbol.subscribe(
                    delegate(Bar b) {
                        lock(tickLock) {
                            lastTickedAt = b.time;
                            set(b);
                        }
                    });
            }

        }

        public void add(DateTime current, T value) {
            Bomb.unless(inTestMode, () => "cannot add values directly to symbol spud unless in test mode");
            Bomb.when(data.ContainsKey(current), () => "data already contains " + current);
            data.Add(current, value);
            Bomb.unless(isEmpty(datesReverse) || current > datesReverse[0], () => "trying to add a date that isn't after the latest point");
            datesReverse.Insert(0, current);
        }

        public void overwrite(DateTime current, T value) {
            Bomb.unless(inTestMode, () => "cannot add values directly to symbol spud unless in test mode");
            if(data.ContainsKey(current)) {
                data[current] = value;
                return;
            }
            add(current, value);
        }

        public void addTick(DateTime current, T value) {
            lastTickedAt = current;
            set(value);
        }

        public SymbolSpud<T> allowStaleTicks() {
            allowStaleTicks_ = true;
            return this;
        }
    }

}
