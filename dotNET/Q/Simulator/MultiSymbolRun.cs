using System.Collections.Generic;
using amazon;
using Q.Trading;
using Q.Util;
using systemdb.metadata;
using util;

namespace Q.Simulator {
    public class MultiSymbolRun : Util.Objects {
        public static void Main(string[] inArgs) {
            var args = Arguments.arguments(inArgs, jStrings("symbols", "run", "systemId", "noShutdown", "useS3"));
            var id = args.integer("systemId");
            var run = args.integer("run");
            if(args.get("useS3", false)) {
                // populate param cache before switching to s3 mode
                new Parameters {{ "systemId", id}, {"RunNumber", run}, {"RunMode", (double) RunMode.STO}}.load();
                LogC.info("using S3 Cache - " + id);
                S3Cache.setDefaultSqsDbMode(true);
                S3Cache.setS3Cache(new EC2Runner("" + id).s3Cache());
            }
            var shutdown = !args.get("noShutdown", false);
            var details = SystemDetailsTable.DETAILS.details(id);

            List<Symbol> symbols;
            List<Trading.Results.Portfolio> portfolios;
            if (args.containsKey("symbols")) {
                var names = split(",", args.@string("symbols"));
                symbols = list(STO.symbols(names));
                portfolios = list<Trading.Results.Portfolio>();
            } else 
                STO.populateSymbolsPortfolios(details, out symbols, out portfolios);

            var data = STO.loader(details, symbols);
            STO.run(details.id(), symbols, portfolios, run, data, false, shutdown);
        }
    }
}