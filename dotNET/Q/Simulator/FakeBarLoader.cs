using System;
using System.Collections.Generic;
using java.util;
using Q.Spuds.Core;
using Q.Trading;
using Q.Trading.Slippage;
using Q.Util;
using util;

namespace Q.Simulator {
    public class FakeBarLoader : Util.Objects, BarLoader {
        readonly IEnumerable<Symbol> symbols;
        readonly LazyDictionary<Symbol, BarSpud> bars;
        readonly LazyDictionary<Symbol, SlippageCalculator> slippageCalculators;
        SpudManager manager_;
        Date current = Dates.now();

        public FakeBarLoader(params Symbol[] symbols) {
            this.symbols = symbols;
            bars = new LazyDictionary<Symbol, BarSpud>(s => barSpud());
            slippageCalculators = new LazyDictionary<Symbol, SlippageCalculator>(s => s.slippageCalculator(bars.get(s)));
        }

        BarSpud barSpud() {
            var spud = new BarSpud(manager()); 
            spud.times.prepare();
            return spud;
        }

        public FakeBarLoader(IEnumerable<Symbol> symbols) {
            this.symbols = symbols;
            bars = new LazyDictionary<Symbol, BarSpud>(s => barSpud());
            slippageCalculators = new LazyDictionary<Symbol, SlippageCalculator>(s => s.slippageCalculator(bars.get(s)));
        }

        public DateTime date(int dateIndex) {
            var times = sort(unique(collect(bars.values(), barSpud => barSpud.times)));
            // simulator index and spud index are inverted
            return times[dateIndex]; 
        }

        public Dictionary<Symbol, Bar> currentBars(DateTime date) {
            return dictionary(symbols, s => bars.get(s)[0]);
        }

        public int numDates() {
            throw new NotImplementedException();
        }

        public Dictionary<Symbol, double> currentSlippages(DateTime date) {
            return dictionary(symbols, s => slippageCalculators.get(s).slippage());
        }

        public void maxBars(int i) {
            throw new NotImplementedException();
        }

        public void add(Symbol symbol, double open, double high, double low, double close) {
            add(symbol, open, high, low, close, Dates.daysAhead(1, current));
        }

        public void add(Symbol symbol, double open, double high, double low, double close, Date time) {
            var bar = new Bar(open, high, low, close, date(time));
            add(symbol, bar);
        }

        void add(Symbol symbol, Bar bar) {
            bars.get(symbol).set(bar);
            current = jDate(bar.time);
        }

        internal void setSpudManager(SpudManager newManager) {
            manager_ = newManager;
        }
        
        SpudManager manager() {
            return Bomb.ifNull(manager_, 
                () => "no spud manager on TestBarLoader - did you forget setSpudManager?");
        }

        public Simulator simulator(SystemArguments args, string prefix) {
            var simulator = new Simulator(args, this, prefix);
            setSpudManager(simulator.bridge.manager);
            return simulator;
        }

        public void add(Dictionary<Symbol, Bar> symbolBars) {
            each(symbolBars, add);
        }
    }
}