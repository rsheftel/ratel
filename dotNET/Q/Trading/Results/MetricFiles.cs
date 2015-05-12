using System.Collections.Generic;
using amazon;
using db;
using file;
using java.lang;
using java.util;
using Q.Util;
using sto;
using util;

namespace Q.Trading.Results {
    public class MetricFiles : Util.Objects {
        public static void writeOne(int systemId, string marketName, int runNumber, Dictionary<string, double> metrics) {
            var values = MetricResultsTable.METRICS.blank();
            eachKey(metrics, name => values.put(name, new Double(metrics[name])));
            MetricResultsTable.METRICS.insert(systemId, marketName, runNumber, values);
            Db.commit();
        }

        public static void writeOneToCsv(QFile file, int runNumber, Dictionary<string, double> metrics) {
            var values = "" + runNumber;
            if (!file.exists()) {
                var header = "run," + join(",", metrics.Keys);
                each(metrics.Keys, k => values += "," + metrics[k]);
                file.create(header + "\n" + values + "\n");
            } else {
                each(list<string>(file.csvHeader()), k => values += k.Equals("run") ? "" : "," + (metrics.ContainsKey(k) ? "" + metrics[k] : ""));
                file.append(values + "\n");
            }
        }

        public static void writeToS3(int id, int number, MetricResults metrics) {
            key(id, number).write(metrics.java());
        }

        static MetaBucket.Key key(int id, int number) {
            return new EC2Runner("" + id).s3Cache().bucket().key("metrics.", "" + number);
        }

        public static MetricResults readFromS3(int id, int number) {
            var map = (Map) key(id, number).read(5000);
            return new MetricResults(map);
        }

        public static void Main(string[] arguments) {
            var args = Arguments.arguments(arguments, jStrings("systemId", "run", "symbol"));
            var results = readFromS3(args.integer("systemId"), args.integer("run"));
            LogC.info(toShortString(results[args.@string("symbol")]));

            
        }
    }
}