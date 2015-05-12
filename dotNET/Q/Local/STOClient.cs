using System;
using System.Collections.Generic;
using file;
using Q.Amazon;
using Q.Messaging;
using Q.Trading.Results;
using Q.Util;
using systemdb.data;
using systemdb.metadata;
using util;
using Exception=java.lang.Exception;

namespace Q.Local {
    public class STOClient : Util.Objects {
        public static readonly QDirectory DLL_CACHE = new QDirectory(@"U:\LocalCloud\DLLs");
        readonly Queue queue;
        static int runsLeft;
        readonly STOMetricsWriter writer;
        readonly object runsLeftLock = new object();
        public readonly Heartbeat heart;

        public STOClient(SystemDetailsTable.SystemDetails details, Queue queue) {
            this.queue = queue;
            writer = new STOMetricsWriter(details, true);
            heart = new Heartbeat(Bootstrap.LOCAL_CLOUD_BROKER, "Tornado.heartbeat.client." + details.id(), 3000);
            heart.initiate();
        }

        public void metrics(int runNumber, Action<MetricResults> onResults) {
            metrics(runNumber, onResults, true);
        }

        void metrics(int runNumber, Action<MetricResults> onResults, bool doRetry) {
            var request = new Fields();
            request.put("RunNumber", runNumber);
            queue.response(request, response => {
                try {
                    onResults((MetricResults) deserialize(Strings.fromBase64(response.text("Metrics"))));
                } catch(Exception e) {
                    LogC.err("exception processing results for run " + runNumber + ", resubmitting...", e);
                    if (doRetry) metrics(runNumber, onResults, false);
                    else throw Bomb.toss("retry failed, bailing out...", e);
                }
            });
        }

        public static void Main(string[] args) {
            var arguments = Arguments.arguments(args, jStrings("id", "start", "end", "debug"));
            var systemId = arguments.integer("id");
            var start = arguments.get("start", 1);
            var debug = arguments.get("debug", false);
            var details = SystemDetailsTable.DETAILS.details(systemId);
            var end = arguments.containsKey("end") ? arguments.integer("end") : details.lastRunNumber();
            var mainDir = copyDllsAndJars(systemId, debug);
            var queueName = "LocalSTO.Requests." + mainDir.name();
            var command = typeof(STOServer).FullName + " -systemId " + systemId + " -queue " + queueName;
            var queue = new Queue(queueName, Bootstrap.LOCAL_CLOUD_BROKER);
            Bootstrap.sendStartCommand(3, mainDir.path(), command);
            var stoClient = new STOClient(details, queue);
            stoClient.createDirectories();
            var runsComplete = stoClient.completedRuns();
            runsLeft = end - start + 1;
            try {
                zeroTo(runsLeft, i => {
                    var runNumber = start + i;
                    if(runsComplete.Contains(runNumber)) runsLeft--;
                    else stoClient.metrics(runNumber, results => stoClient.writeResultsToFile(runNumber, results));
                });
                wait(int.MaxValue, 1000, () => runsLeft == 0);
                stoClient.closeAndCopyFiles();
            } finally {
                Bootstrap.sendStopCommand(mainDir.path(), command);
            }

        }

        void closeAndCopyFiles() {
            writer.closeFiles();
            writer.copyMetrics();
        }

        void createDirectories() {
            writer.createDirectories();
        }

        void writeResultsToFile(int run, IDictionary<string, Dictionary<string, double>> results) {
            try {
                writer.writeResults(run, results);
                lock(runsLeftLock) {
                    runsLeft--;
                    LogC.info("completed run " + run + ". " + runsLeft + " left.");
                }
            } catch(Exception e) {
                LogC.info(LogC.errMessage("exception thrown in writeResults", e));
                throw;
            }
        }

        static QDirectory copyDllsAndJars(int systemId, bool debug) {
            var dirName = "" + systemId;
            var mainDir = DLL_CACHE.directory(new[] {dirName});
            var i = 0;
            while(mainDir.exists()) mainDir = DLL_CACHE.directory(new[] {dirName + "-" + i++});
            copyDirectory(mainDir, Systematic.JAVA_LIB_PARTS, debug);
            copyDirectory(mainDir, Systematic.QRUN_PARTS, debug);
            return mainDir;
        }

        static void copyDirectory(QDirectory destination, string[] parts, bool debug) {
            var fromParts = new string[parts.Length];
            zeroTo(parts.Length, i => fromParts[i] = debug ? parts[i].Replace("Release", "Debug") : parts[i]);
            var fromDir = Systematic.mainDir().directory(fromParts);
            var toDir = destination.directory(parts);
            toDir.destroyIfExists();
            fromDir.copy(toDir);
        }

        List<int> completedRuns() {
            return writer.completedRuns();
        }
    }
}
