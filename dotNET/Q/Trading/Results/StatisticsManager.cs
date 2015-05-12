using System;
using System.Collections.Generic;
using file;
using Q.Amazon;
using Q.Util;
using systemdb.metadata;

namespace Q.Trading.Results {
    public class StatisticsManager : Objects {
        readonly System system;
        readonly SystemArguments arguments;
        readonly Dictionary<Collectible, StatisticsCollector> collectors = new Dictionary<Collectible, StatisticsCollector>();
        readonly Portfolio allPortfolio;
        public event Action<Position, Trade> onNewTrade;
        bool hasData;

        public StatisticsManager(System system, SystemArguments arguments) {
            this.system = system;
            this.arguments = arguments;
            system.addCollectorsTo(arguments, collectors);
            each(arguments.portfolios, p => p.addCollectorsTo(arguments, collectors));
            allPortfolio = new Portfolio("ALL", arguments.siv());
            each(arguments.symbols, symbol => allPortfolio.Add(symbol, 1.0));
            allPortfolio.addCollectorsTo(arguments, collectors);
            onNewTrade += doNothing;
        }

        public void addOrder(Position position, Trade trade) {
            eachKey(collectors, port => port.addOrder(collectors[port], position, trade));
            onNewTrade(position, trade);
            hasData = true;
        }

        public void addBar(Dictionary<Symbol, Bar> bars, Dictionary<Symbol, double> fxRates) {
            eachKey(collectors, port => port.addBar(collectors[port], system, bars, fxRates));
            hasData = true;
        }

        public void writeSTOFiles(bool doMetricFiles) {
            each(collectors, (portfolio, collector) => collector.writeSTOFiles(portfolio.name, doMetricFiles));
            if (!doMetricFiles || !arguments.parameters.isCloudSTO()) return;

            var systemId = arguments.systemId();
            var run = arguments.parameters.runNumber();
            MetricFiles.writeToS3(systemId, run, metrics());
            FetchSTOParameters.key(systemId, run).write(serialize(arguments.parameters));
        }

        public MetricResults metrics() {
            var metrics = new MetricResults();
            var start = DateTime.Now;
            each(collectors, (portfolio, collector) => metrics[portfolio.name] = collector.metrics());
            var seconds = (DateTime.Now - start).TotalSeconds;
            LogC.info("metric calculation took " + seconds + " seconds (" + (collectors.Count / seconds) + " collectors per second)");
            return metrics;
        }

        public double netProfit(Collectible collectible) {
            return collectors[collectible].netProfit();
        }

        public double netProfit() {
            return collectors[allPortfolio].netProfit();
        }

        public void writeCurveFiles(LiveSystem liveSystem, QDirectory directory) {
            directory.createIfMissing();
            eachKey(collectors, collectible => collectors[collectible].writeCurveFile(collectible, liveSystem, directory));
        }

        public StatisticsCollector collector(Collectible c) {
            return collectors[c];
        }

        public StatisticsCollector allCollector() {
            return collectors[allPortfolio];
        }
        
        public StatisticsCollector portfolioCollector(Portfolio portfolio) {
            return collectors[portfolio];
        }

        public IEnumerable<Collectible> collectibles() {
            return collectors.Keys;
        }

        public void addCollectible(Collectible collectible) {
            Bomb.when(hasData, () => "can only add collectibles before run begins");
            collectible.addCollectorsTo(arguments, collectors);
        }
    }
}