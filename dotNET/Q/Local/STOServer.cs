using System.Collections.Generic;
using Q.Messaging;
using Q.Simulator;
using Q.Trading.Results;
using Q.Util;
using systemdb.data;
using systemdb.metadata;
using util;
using Portfolio=Q.Trading.Results.Portfolio;
using Symbol=Q.Trading.Symbol;

namespace Q.Local {
    public class STOServer : Util.Objects {
        readonly int systemId;
        readonly SystemDetailsTable.SystemDetails details;
        readonly List<Symbol> symbols;
        readonly List<Portfolio> portfolios;
        readonly SystemDbBarLoader loader;
        public readonly Heartbeat heart;

        public STOServer(int systemId, int serverIndex) {
            this.systemId = systemId;
            details = SystemDetailsTable.DETAILS.details(systemId);
            STO.populateSymbolsPortfolios(details, out symbols, out portfolios);
            loader = STO.loader(details, symbols);
            heart = new Heartbeat(Bootstrap.LOCAL_CLOUD_BROKER, "Tornado.heartbeat.server." + systemId + "." + serverIndex, 3000, fields => {
                fields.put("ServerIndex", serverIndex); 
                fields.put("SystemId", systemId);
            });
        }

        MetricResults metrics(Fields fields) {
            var simulator = STO.run(systemId, symbols, portfolios, fields.integer("RunNumber"), loader, false, false);
            simulator.bridge.statistics().writeSTOFiles(false);
            return simulator.metrics();
        }

        public static void Main(string[] args) {
            LogC.setVerboseLoggingForever(true);
            var arguments = Arguments.arguments(args, jStrings("systemId", "queue", "serverIndex"));
            var systemId = arguments.integer("systemId");
            var serverIndex = arguments.integer("serverIndex");
            LogC.setOut("STOServer", Systematic.logsDir().file("STOServer_" + systemId +"-" + serverIndex + ".log").path(), true);
            LogC.useJavaLog = true;

            var server = new STOServer(systemId, serverIndex);
            var queue = new Queue(arguments.@string("queue"), Bootstrap.LOCAL_CLOUD_BROKER);
            server.subscribe(queue);
            server.heart.initiate();
            sleepForever();
        }

        static void sleepForever() {
            while(trueDat()) {
                sleep(int.MaxValue);
            }
        }

        public void subscribe(Queue queue) {            
            queue.subscribe(fields => {
                                var response = new Fields();
                                response.put("Metrics", Strings.toBase64(serialize(metrics(fields))));
                                return response;
                            });
        }
    }
}