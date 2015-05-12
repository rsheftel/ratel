using System.Collections.Generic;

namespace Q.Trading.Results {
    public interface Collectible {
        string name { get; }
        void addOrder(StatisticsCollector collector, Position position, Trade trade);
        void addBar(StatisticsCollector collector, System system, Dictionary<Symbol, Bar> bars, Dictionary<Symbol, double> fxRates);
        void addCollectorsTo(SystemArguments arguments, Dictionary<Collectible, StatisticsCollector> collectors);
        IEnumerable<Position> allPositions(System system);
        BarSpud barsMaybe();
        bool collects(Position position);
        bool collects(Trade trade);
    }
}