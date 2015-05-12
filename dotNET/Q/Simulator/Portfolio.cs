using System;
using System.Collections.Generic;
using db;
using file;
using Q.Trading;
using Q.Util;
using systemdb.metadata;
using systemdb.portfolio;
using tsdb;
using util;
using JMarket=systemdb.metadata.Market;

namespace Q.Simulator {
    public class PortfolioGroupLoad : Util.Objects {
        public static void Main(string[] args) {
            var arguments = Arguments.arguments(args, jStrings("prefix", "dir"));
            var dir = new QDirectory(arguments.@string("dir"));
            var rep = Tag.parse(dir.file("group.xml").text());
            Groups.GROUPS.load(rep.child("group"), arguments.@string("prefix"));
            Db.commit();
        }
    }

    public class Portfolio : Util.Objects {
        public static void Main(string[] args) {
            var arguments = Arguments.arguments(args, jStrings("group", "start", "end", "dir", "asOf", "metricSource", "overwrite"));
            var groupName = arguments.@string("group");
            var start = date(arguments.get("start", jDate("1990/01/01")));
            var end = date(arguments.get("end", Dates.midnight()));
            if(arguments.containsKey("asOf"))
                freezeNow(date(arguments.date("asOf")));
            var metricSource = arguments.containsKey("metricSource") ? new DataSource(arguments.@string("metricSource")) : null;
            var dir = new QDirectory(arguments.get("dir"));
            dir.createIfMissing();
            var group = Groups.GROUPS.forName(groupName);
            var overwrite = arguments.get("overwrite", false);
            Bomb.unless(overwrite || isEmpty(dir.files()), 
                () => dir.path() + " contains existing files and overwrite is false!");
            writeGroupXml(dir, group, start, end);
            var liveMarkets = list<MsivPv>(Groups.GROUPS.liveMarkets(groupName));
            var marketsBySystem = new Dictionary<LiveSystem, List<JMarket>>();
            each(liveMarkets, liveMarket => marketsBySystem[liveMarket.liveSystem()] = new List<JMarket>());
            each(liveMarkets, liveMarket => marketsBySystem[liveMarket.liveSystem()].Add(new JMarket(liveMarket.market())));
            each(marketsBySystem, (system, markets) => generateCurves(system, markets, dir, start, end, metricSource));
            Environment.Exit(0);
        }

        static void writeGroupXml(QDirectory outputDirectory, Group group, DateTime start, DateTime end) {
            var groupXml = Group.asXml(group);
            groupXml.add("asOf", ymdHuman(now()));
            groupXml.add("runDate", ymdHuman(reallyNow()));
            groupXml.add("start", ymdHuman(start));
            groupXml.add("end", ymdHuman(end));
            outputDirectory.file("group.xml").create(groupXml.longXml());
        }

        static void generateCurves(LiveSystem system, IEnumerable<JMarket> markets, QDirectory directory, DateTime start, DateTime end, DataSource metricSource) {
            var symbols = convert(markets, market => new Symbol(market.name(), market.bigPointValue()));
            var parameters = new Parameters {
                {"systemId", system.id()},
                {"RunMode", (double) RunMode.LIVE}
            };
            var startLoading = DateTime.Now;
            Bomb.when(system.details().runInNativeCurrency(), () => "portfolio optimization requires systems to run in dollars, not native currency");
            var bars = new SystemDbBarLoader(system.details().interval(), symbols, start, end);
            var simulator = new Simulator(new SystemArguments(symbols, parameters), bars, "QUEDGE");
            var startProcessing = DateTime.Now;
            var perSecond = simulator.processBars();
            saveMetric(system, "marketBarsPerSecond", metricSource, perSecond);
            var startMetricCalc = DateTime.Now;
            simulator.metrics();
            saveMetric(system, "metricCalculationSeconds", metricSource, secondsSince(startMetricCalc));
            saveMetric(system, "totalRunSeconds", metricSource, secondsSince(startProcessing));
            simulator.writeCurveFiles(directory);
            saveMetric(system, "totaSeconds", metricSource, secondsSince(startLoading));
            Db.commit();
        }

        static void saveMetric(LiveSystem system, string metric, DataSource metricSource, double perSecond) {
            var reallyMidnight = Dates.midnight(Dates.reallyNow());
            if(metricSource != null)
                system.series(metric).with(metricSource).write(reallyMidnight, perSecond);
        }
    }
}
