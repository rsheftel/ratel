using System;
using System.Collections.Generic;
using Q.Trading;
using Q.Util;
using systemdb.metadata;
using util;
using JMarket=systemdb.metadata.Market;
using QPortfolio=Q.Trading.Results.Portfolio;
namespace Q.Simulator {
    public class STO : Util.Objects {
        public static void populateSymbolsPortfolios(SystemDetailsTable.SystemDetails details, out List<Symbol> symbols, out List<Trading.Results.Portfolio> portfolios) {
            var marketNames = list<string>(MsivBacktestTable.BACKTEST.markets(details.siv(), details.stoId()));
            symbols = list(STO.symbols(marketNames));
            portfolios = list<sto.Portfolio, Trading.Results.Portfolio>(sto.Portfolio.portfolios(details.id()), j => new Trading.Results.Portfolio(j));
        }

        public static IEnumerable<Symbol> symbols(List<string> marketNames) {
            var markets = convert(marketNames, name => condensedSql("loading symbol " + name, () => new JMarket(name)));
            var symbols = convert(markets, market => new Symbol(market.name(), market.bigPointValue()));
            return symbols;
        }

        public static void Main(string[] args) {
            var arguments = Arguments.arguments(args, jStrings("systemId", "start", "end", "dumpParamsOnly", "slippageCalculator"));
            var systemId = arguments.integer("systemId");
            var start = arguments.integer("start");
            var end = arguments.get("end", start);
            var dumpParamsOnly = arguments.get("dumpParamsOnly", false);
            
            var details = SystemDetailsTable.DETAILS.details(systemId);
            List<Symbol> symbols;
            List<Trading.Results.Portfolio> portfolios;
            populateSymbolsPortfolios(details, out symbols, out portfolios);
            if(arguments.containsKey("slippageCalculator")) {
                var calculator = Type.GetType(arguments.@string("slippageCalculator"));
                each(symbols, s => s.overrideSlippageCalculator(calculator));
            }
                
            var data = loader(details, symbols);
            for(var runNumber = start; runNumber <= end; runNumber++) { 
                run(systemId, symbols, portfolios, runNumber, data, dumpParamsOnly, true);
            }
            Environment.Exit(0);
        }

        public static SystemDbBarLoader loader(SystemDetailsTable.SystemDetails details, IEnumerable<Symbol> symbols) {
            var symbolRanges = dictionary(symbols, 
                symbol => MsivBacktestTable.BACKTEST.range(details.id(), symbol.name));
            return new SystemDbBarLoader(details.interval(), symbols, symbolRanges);
        }

        public static Simulator run(int systemId, IEnumerable<Symbol> symbols, IEnumerable<QPortfolio> portfolios, int runNumber, BarLoader data, bool dumpParamsOnly, bool shutdown) {
            var parameters = new Parameters {
                {"systemId", systemId},
                {"RunMode", (double) RunMode.STO},
                {"RunNumber", runNumber}
            };
            try {
                parameters.runNumber();
                LogC.err("" + parameters);
                if (dumpParamsOnly) return null;
                var simulator = new Simulator(new SystemArguments(symbols, portfolios, parameters), data, "QUEDGE");
                simulator.processBars();
                if (shutdown) simulator.shutdown();
                else {
                    var metrics = simulator.metrics();
                    LogC.verbose(() => "skipping shutdown based on noShutdown parameter.");
                    LogC.verbose(() => toShortString(metrics["ALL"]));
                }
                return simulator;
            } catch (Exception e) {
                throw Bomb.toss("\nFailed in run " + runNumber + "\nUsing:\n" + parameters + "\n", e);
            }
        }
    }
}
