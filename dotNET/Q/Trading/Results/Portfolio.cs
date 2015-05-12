using System.Collections;
using System.Collections.Generic;
using Q.Util;
using sto;
using systemdb.metadata;

namespace Q.Trading.Results {
    public class Portfolio : Objects, IEnumerable<Symbol>, Collectible {
        public string name { get; private set; }
        readonly Siv siv;
        readonly Dictionary<Symbol, double> weights = new Dictionary<Symbol, double>();
        java.lang.Object javaRepresentation;

        public Portfolio(string name, Siv siv) {
            this.name = name;
            this.siv = siv;
        }

        public Portfolio(java.lang.Object java) : this((sto.Portfolio) java) {}

        public Portfolio(sto.Portfolio jPortfolio) : this(jPortfolio.name(), jPortfolio.siv()) {
            var msivs = list<WeightedMsiv>(jPortfolio.msivs());
            each(msivs, msiv => Add(new Symbol(MsivTable.MSIVS.forName(msiv.name()).market()), msiv.weight()));
        }


        public void Add(Symbol symbol, double weight) {
            javaRepresentation = null;
            weights.Add(symbol, weight);
        }

        public IEnumerator<Symbol> GetEnumerator() {
            return weights.Keys.GetEnumerator();
        }

        IEnumerator IEnumerable.GetEnumerator() {
            return GetEnumerator();
        }

        public void addOrder(StatisticsCollector collector, Position position, Trade trade) {
            if(!collects(position)) return;
            var w = weight(position);
            collector.addOrder(new WeightedPosition(position, w), new WeightedTrade(trade, w));
        }
        
        public void addBar(StatisticsCollector collector, System system, Dictionary<Symbol, Bar> bars, Dictionary<Symbol, double> fxRates) {
            var symbols = weights.Keys;
            var myBars = dictionary(accept(bars, entry => weights.ContainsKey(entry.Key)));
            if (isEmpty(myBars)) return;
            
            var ours = collect(symbols, symbol => system.positions(symbol));
            var weighted = convert(ours, p => new WeightedPosition(p, weight(p)));
            collector.addBar(weighted, myBars, fxRates);
        }

        public void addCollectorsTo(SystemArguments arguments, Dictionary<Collectible, StatisticsCollector> collectors) {
            collectors.Add(this, new StatisticsCollector(arguments));
        }

        public IEnumerable<Position> allPositions(System system) {
            return collect(weights.Keys, symbol => system.positions(symbol));
        }

        public BarSpud barsMaybe() {
            return null;
        }

        public bool collects(Position position) {
            return collects(position.symbol);
        }

        bool collects(Symbol symbol) {
            return weights.ContainsKey(symbol);
        }

        public bool collects(Trade trade) {
            return collects(trade.order().symbol);
        }

        double weight(Position position) {
            return weights[position.symbol];
        }

        public sto.Portfolio java() {
            if (javaRepresentation == null) {
                var msivs = new List<WeightedMsiv>();
                eachKey(weights, symbol => {
                    var msivName = MsivTable.MSIVS.msiv(symbol.name, siv).name();
                    msivs.Add(new WeightedMsiv(msivName, weights[symbol]));
                });
                javaRepresentation = new sto.Portfolio(name, jList(msivs, x => x));
            }
            return (sto.Portfolio) javaRepresentation;
        }
    }
}