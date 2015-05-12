using System;
using System.Collections.Generic;
using System.IO;
using System.Threading;
using amazon;
using file;
using Q.Simulator;
using Q.Util;
using systemdb.metadata;
using util;

namespace Q.Trading.Results {
    public class CurveFiles: Util.Objects {
        public static void writeOne(string path, int barCount) {
            using(var fs = File.Create(path))
            using(var bw = new BinaryWriter(fs))
                zeroTo(barCount, rows => zeroTo(3, columns => bw.Write(1.0)));
        }

    
        public static void writeOne(string path, List<DateTime> dates, List<double> pnls, List<double> positions) {
            using(var fs = File.Create(path))
                using(var bw = new BinaryWriter(fs))
                    writeOne(dates, pnls, positions, bw);
        }

        static void writeOne(IEnumerable<DateTime> dates, IEnumerable<double> pnls, IEnumerable<double> positions, BinaryWriter bw) {
            each(dates, pnls, positions, (date, pnl, position) => {
                                             bw.Write(jDate(date).getTime() / 1000.0);
                                             bw.Write(pnl);
                                             bw.Write(position);
                                         });
            
        }

        public static void writeToS3(int id, string name, int number, List<DateTime> dates, List<double> pnls, List<double> positions) {
            using(var ms = new MemoryStream()) {
                using (var bw = new BinaryWriter(ms)) 
                    writeOne(dates, pnls, positions, bw);
                var bytes = ms.ToArray();
                
                key(id, name, number).write(bytes);
            }
        }

        static MetaBucket.Key key(int id, string name, int number) {
            return new EC2Runner("" + id).s3Cache().bucket().key("curves.", name + "-" + number);
        }

        public static void readFromS3(int id, string name, int number, string path) {
            var data = (byte[]) key(id, name, number).read(5000);
            var file = new QFile(path);
            file.ensurePath();
            file.overwrite(data);
        }

        public static void Main(string[] args) {
            Log.doNotDebugSqlForever();
            var startTime = now();
            var arguments = Arguments.arguments(args, jStrings("systemId", "runs", "start", "end", "markets", "skipExisting"));
            var skipExisting = arguments.get("skipExisting", false);
            var systemId = arguments.get("systemId", -1);
            var details = SystemDetailsTable.DETAILS.details(systemId);
            var runs = arguments.containsKey("runs") 
                ? convert(split(",", arguments.get("runs")), run => int.Parse(run)) 
                : seq(arguments.integer("start"), arguments.integer("end"));
            var curvesDir = sto.STO.fromId(systemId).curvesDir();
            var markets = arguments.containsKey("markets") ? split(",", arguments.@string("markets")) : allMarkets(details);
            ThreadPool.SetMaxThreads(200, 200);
            each(runs, run => each(markets, market => {
                var directory = curvesDir.directory(new[] { details.siv().svimName("_", market, "") });
                var runFile = directory.file("run_" + run + ".bin");
                if (skipExisting && runFile.exists()) {
                    info("skipped " + runFile.path());
                    return;
                }
                var path_ = runFile.path();
                queueWorkItem(() => { readFromS3(systemId, market, run, path_); LogC.info("wrote " + path_); });
            }));
            waitForAllWorkItems(6000, 100);
            LogC.info("took " + now().Subtract(startTime).TotalSeconds + " seconds.");
        }

        static IEnumerable<int> seq(int first, int last) {
            var result = new List<int>();
            for(var i = first; i <= last; i++)
                result.Add(i);
            return result;
        }

        static List<string> allMarkets(SystemDetailsTable.SystemDetails details) {
            var result = new List<String>();
            List<Symbol> symbols;
            List<Portfolio> portfolios;
            STO.populateSymbolsPortfolios(details, out symbols, out portfolios);
            result.AddRange(convert(symbols, s => s.name));
            result.AddRange(convert(portfolios, p => p.name));
            result.Add("ALL");
            return result;
        }
    }
}