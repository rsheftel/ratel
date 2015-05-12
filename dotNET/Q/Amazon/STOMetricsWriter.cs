using System;
using System.Collections.Generic;
using file;
using java.io;
using Q.Util;
using systemdb.metadata;

namespace Q.Amazon {
    internal class STOMetricsWriter : Objects {
        readonly SystemDetailsTable.SystemDetails details;
        readonly bool reuseExistingFiles;
        readonly QDirectory tempDir;
        
        readonly QDirectory metricsDir;
        readonly List<string> metrics = new List<string>();
        readonly List<string> markets = new List<string>();
        readonly Dictionary<string, Writer> appendersByMarket = new Dictionary<string, Writer>();

        public STOMetricsWriter(SystemDetailsTable.SystemDetails details, bool reuseExistingFiles) {
            this.details = details;
            this.reuseExistingFiles = reuseExistingFiles;
            tempDir = new QDirectory(@"C:\tempMetricFiles\" + details.id());
            metricsDir = new QDirectory(details.stoDir()).directory(new[] {details.stoId(), "Metrics"});
        }

        public void writeResults(int run, IDictionary<string, Dictionary<string, double>> results) {
            try {
                lock(metrics) { // everybody waits for the first lock acquirer to populate the metrics and markets and get out of the way.
                    if (isEmpty(metrics)) {
                        markets.AddRange(sort(results.Keys));
                        if (reuseExistingFiles) metrics.AddRange(metricsFromExistingFile());
                        else metrics.AddRange(first(results).Value.Keys);
                        eachKey(results, market => {
                            assignAppender(market);
                            if (!reuseExistingFiles) 
                                lock (appendersByMarket[market]) appendersByMarket[market].write("run," + join(",", metrics) + "\n");
                        });
                        LogC.info("FIRST RESULT READ: metrics, markets established.");
                    } 
                }
                Bomb.unless(listEquals(sort(results.Keys), markets), 
                    () => "markets in first results was " + toShortString(markets) + " but is now " + toShortString(sort(results.Keys)) + 
                        " runs cannot have different market/portfolio combinations - the sto has been corrupted.");
                eachKey(results, market => {
                     var symbolMetrics = convert(metrics, metric => results[market][metric]);
                     var strings = convert(symbolMetrics, x => toMetricCsv(x));
                     lock(appendersByMarket[market]) {
                         appendersByMarket[market].write(run + "," + join(",", strings) + "\n");
                         appendersByMarket[market].flush();
                     }
                 });
            } catch(Exception e) {
                throw Bomb.toss("failed processing " + run + " : markets " + commaSep(results.Keys), e);
            }
        }

        void assignAppender(string market) {
            var file = tempDir.file(details.siv().sviName("_") + "_" + market + ".csv");
            if (!reuseExistingFiles) file.deleteIfExists();
            appendersByMarket[market] = file.appender();
        }

        static string toMetricCsv(double input) {
            return input.ToString().Replace("Infinity", "Inf");
        }

        public static double fromMetricCsv(String input) {
            try {
                return double.Parse(input.Replace("Inf", "Infinity"));
            } catch (Exception e) {
                throw Bomb.toss("error parsing as double: " + input, e);
            }
        }

        IEnumerable<string> metricsFromExistingFile() {
            var csv = new CsvStreamer(allFile(), true);
            try {
                return rest(list<string>(csv.header()));
            } finally {
                csv.close();
            }
        }

        QFile allFile() {
            return tempDir.file(details.siv().sviName("_") + "_ALL.csv");
        }

        public void copyMetrics() {
            tempDir.copy(metricsDir);
        }

        public void createDirectories() {
            tempDir.createIfMissing();
            LogC.makeOldDir(metricsDir.path());
        }

        public void closeFiles() {
            each(appendersByMarket, (name, writer) => writer.close());
        }

        public List<int> completedRuns() {
            var all = allFile();
            if(!all.exists()) return new List<int>();
            var csv = new CsvStreamer(all, true);
            var result = new List<int>();
            while(trueDat()) {
                var next = csv.next();
                if(next == null) break;
                var record = list<string>(next);
                result.Add(int.Parse(first(record)));
            }
            return result;
        }
    }
}