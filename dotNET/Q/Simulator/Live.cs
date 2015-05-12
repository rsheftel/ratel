using System;
using bloomberg;
using Q.Trading;
using Q.Util;
using systemdb.metadata;
using util;
using Market=systemdb.metadata.Market;

namespace Q.Simulator {
    public class Live : Util.Objects {
        const string PREFIX_DEFAULT = "TOMAHAWK";

        public static void Main(string[] args) {
            var arguments = Arguments.arguments(args, jStrings("system", "prefix"));
            var system = arguments.get("system");
            var prefix = arguments.get("prefix", PREFIX_DEFAULT);
            LogC.setOut("Tomahawk", Systematic.logsDir().file("Tomahawk." + system + ".log").path(), true);
            LogC.useJavaLog = true;
            LogC.info("running system " + system + ", process " + processId());
            var liveSystems = accept(list<LiveSystem>(MsivLiveHistory.LIVE.liveSystems()), ls => ls.siv().system().Equals(system));
            each(liveSystems, liveSystem => {
                Bomb.when(liveSystem.details().runInNativeCurrency(), () => "not allowed to run live systems in native currency");
                var markets = list<Market>(liveSystem.markets());
                var symbols = convert(markets, market => new Symbol(market.name(), market.bigPointValue()));
                var systemId = liveSystem.id();
                var parameters = new Parameters {
                    {"systemId", systemId},
                    {"RunMode", (double) RunMode.LIVE}
                };
                Bomb.when(isEmpty(symbols), () => "No markets for " + systemId);
                var systemArguments = new SystemArguments(symbols, parameters);

                var start = date(systemArguments.interval().isDaily() ? BloombergSecurity.BBG_START_HISTORICAL : BloombergSecurity.BBG_START_INTRADAY);
                if(parameters.has("DaysBack")) start = now().AddDays(-parameters.get<int>("DaysBack"));
                var loader = new SystemDbBarLoader(liveSystem.details().interval(), symbols, start);
                var simulator = new Simulator(systemArguments, loader, prefix);
                simulator.processBars();
                simulator.goLive();
            });
            sleep(Int32.MaxValue);
        }

    }
}
