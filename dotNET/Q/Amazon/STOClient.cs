using System;
using System.Collections.Generic;
using System.Threading;
using amazon;
using amazon.monitor;
using Q.Messaging;
using Q.Simulator;
using Q.Trading;
using Q.Trading.Results;
using Q.Util;
using systemdb.metadata;
using util;
using Portfolio=Q.Trading.Results.Portfolio;
using Symbol=Q.Trading.Symbol;

namespace Q.Amazon {

    public class STOClient : Util.Objects {
        
        public static void Main(string[] args) {
            ThreadPool.SetMaxThreads(50, 50);
            STORunner stoRunner = null;
            var arguments = Arguments.arguments(args, jStrings("numInstances", "id", "start", "end", "maxRunHours", "DO_NOT_USE_THIS_OPTION_UNLESS_YOU_ARE_JERIC"));
            var numInstances = arguments.integer("numInstances");
            var systemId = arguments.integer("id");
            var start = arguments.get("start", 1);
            var end = arguments.containsKey("end") ? arguments.integer("end") : (int?) null;
            var maxRunMillis = numInstances == 0 ? 0 : (long) (arguments.numeric("maxRunHours") * 3600 * 1000);
            var noKill = arguments.get("DO_NOT_USE_THIS_OPTION_UNLESS_YOU_ARE_JERIC", false);
            try {
                stoRunner = new STORunner(numInstances, systemId, start, end, noKill);
                stoRunner.createDirectories();
                var dbServer = stoRunner.startDbServer();
                stoRunner.initialize(maxRunMillis);
                stoRunner.enqueueRequests();
                stoRunner.processResults();
                dbServer.stopServer();
                stoRunner.copyMetrics();
            } catch(Exception e) {
                if (stoRunner != null) stoRunner.killRun(LogC.errMessage("caught exception in Main: ", e));
            }
            Environment.Exit(0);
        }


        internal class STORunner : Util.Objects {
            readonly SystemDetailsTable.SystemDetails details;
            readonly int numInstances;
            readonly int systemId;
            readonly int start;
            readonly bool noKill;
            readonly int end;
            readonly EC2Runner runner;
            readonly List<Symbol> symbols;
            readonly List<Portfolio> portfolios;
            readonly List<int> existingRuns;
            readonly object runsQueuedLock = new object();
            readonly UnhandledExceptionEventHandler unhandledExceptionProcessor;
            int runsQueued;
            Timer shutdownTimer;
            readonly DateTime startTime = now();
            readonly List<DateTime> completionTimes = new List<DateTime>();
            double runsPerMinute;
            readonly STOMetricsWriter writer;

            public STORunner(int numInstances, int systemId, int start, int? end, bool noKill) {
                this.numInstances = numInstances;
                this.systemId = systemId;
                this.start = start;
                this.noKill = noKill;
                details = SystemDetailsTable.DETAILS.details(systemId);
                this.end = end.HasValue ? end.Value : details.lastRunNumber();
                writer = new STOMetricsWriter(details, false);
                STO.populateSymbolsPortfolios(details, out symbols, out portfolios);
                runner = new EC2Runner(systemId + "-" + Dates.yyyyMmDdHhMmSsNoSeparator(Dates.now()));
                LogC.info("checking for completed runs");
                existingRuns = completedRuns();  
                var allRunsComplete = true;
                for(var i = this.start; i <= this.end; i++)
                    if(!existingRuns.Contains(i)) {
                        allRunsComplete = false;
                        break;
                    }
                if(allRunsComplete) this.numInstances = 0;
                unhandledExceptionProcessor = ((sender, args) => killRun(LogC.errMessage("unhandled exception", (Exception) args.ExceptionObject)));
            }

            
            List<int> completedRuns() {
                try {
                    var keys = list<MetaBucket.Key>(runner.bucket().keys("metrics."));
                    return list(convert(keys, key => int.Parse(key.keyName())));
                } catch(Exception e) {
                    if(e.Message.Contains("does not exist")) return new List<int>();
                    throw Bomb.toss("error getting keys: ", e);
                }
            }

            public void enqueueRequests() {
                LogC.info("enqueueing requests " + runner.requestQueueName());
                zeroTo(end - start + 1, i => ThreadPool.QueueUserWorkItem(o => enqueueRun(start + i)));
                wait(int.MaxValue, 1000, () => runsQueued == end - start + 1);
                LogC.info("done enqueueing requests " + runner.requestQueueName());
            }

            internal void enqueueRun(int runNumber) {
                var runParameters = parameters(runNumber);
                var request = new STORequest(symbols, portfolios, runParameters);

                if (numInstances > 0 && !existingRuns.Contains(runNumber)) runner.request(request.java());
                else runner.responseQueue().send(request.response("LOCAL", now(), now(), processId()));

                lock(runsQueuedLock) runsQueued++;
                if(runsQueued % 100 == 0)
                    info("queued message for run " + (start + runsQueued - 1));
                
            }

            Parameters parameters(int runNumber) {
                return parameters(systemId, runNumber);
            }

            internal static Parameters parameters(int id, int runNumber) {
                var result = new Parameters {
                    {"systemId", id},
                    {"RunMode", (int) RunMode.STO},
                    {"RunNumber", runNumber}
                };
                result.flipToCloudSTO();
                return result;
            }

            public void initialize(long maxRunMillis) {
                Console.CancelKeyPress += (sender, args) => killRun("ctrl-c pressed");
                AppDomain.CurrentDomain.UnhandledException += unhandledExceptionProcessor;
                if(numInstances > 0) {
                    timerManager().inMillis(maxRunMillis, shutdownGracefully, out shutdownTimer);
                    LogC.info("starting instances and loading jars, dlls to S3");
                    runner.startInstances(numInstances, "c1.xlarge", "Q.Amazon.STOServer", 9);
                    info("upload complete");
                } else LogC.info("skipping shutdown timer for 0 instances.");
                if(existingRuns.Count == 0 && numInstances > 0)
                    runOneLocal();
                else // cache parameters before going multi-thread
                    parameters(start);
            }

            void shutdownGracefully() {
                killRun("maxRunTime exceeded.  Started at: " + startTime);
                doNothing(shutdownTimer);
                Environment.Exit(-1);
            }

            void runOneLocal() {
                LogC.info("running run " + start + " local to populate S3 cache");
                S3Cache.saveAllQueryResultsToS3(true);
                var s3Cache = runner.s3Cache();
                s3Cache.createBucket();
                S3Cache.setS3Cache(s3Cache);
                var runParameters = parameters(start);
                var symbolRanges = dictionary(symbols, symbol => MsivBacktestTable.BACKTEST.range(systemId, symbol.name));
                var data = new SystemDbBarLoader(details.interval(), symbols, symbolRanges);
                var simulator = new Simulator.Simulator(new SystemArguments(symbols, portfolios, runParameters), data, "QUEDGE");
                simulator.processBars();
                simulator.shutdown();
                S3Cache.saveAllQueryResultsToS3(false);
            }

            public void processResults() {
                AppDomain.CurrentDomain.UnhandledException -= unhandledExceptionProcessor;
                Log.doNotDebugSqlForever();
                var responseQ = runner.responseQueue();
                var completed = new Dictionary<int, bool>();
                var totalRuns = end - start + 1;
                var threads = new List<Thread>();
                for(var i = 0; i < 10; i++)
                    threads.Add(new Thread(o => {
                        while(completed.Count < totalRuns) {
                            var messages = responseQ.messages(1000, 100, 10, null);
                            if (isEmpty(list<Message>(messages))) {
                                sleep(100);
                                continue;
                            }
                            each<Message>(messages, message => process(message, completed, totalRuns));
                        }
                    }));
                each(threads, thread => thread.Priority = ThreadPriority.AboveNormal);
                each(threads, thread => thread.Start());
                each(threads, thread => thread.Join());
                stopInstances();
                wait(600, 60000, () => completed.Count == totalRuns);
                waitForAllWorkItems(600, 60000);
                writer.closeFiles();
            }

            void stopInstances() {
                if(noKill) LogC.info("NOT STOPPING INSTANCES - IF YOU ARE NOT JERIC");
                else runner.stopInstances();
            }

            void process(Message message, Dictionary<int, bool> completed, int totalRuns) {
                var response = (STOResponse) message.@object();
                runner.received(response);
                var completedRun = response.runNumber();
                var runTimeMillis = response.runTimeMillis();
                var completionTime = response.completedAt();
                var isLocal = response.instanceId().Equals("LOCAL");
                message.delete();
                // Letting message get GC'd, rather than sitting in the thread pool really helps with the Java heap space.
                queueWorkItem(() => {
                    lock(completed) {
                        if (completed.ContainsKey(completedRun)) return;
                        completed[completedRun] = true;
                        if(!isLocal) {
                            completionTimes.Add(date(completionTime));
                            updateRunsPerMinute(runTimeMillis);
                        }
                    }
                    LogC.info("completed run " + completedRun + paren(completed.Count + "/" + totalRuns));
                    new Topic(CloudMonitor.progressTopic(systemId)).send(new Dictionary<string, object> {
                        {"RunsComplete", completed.Count},
                        {"TotalRuns", totalRuns},
                        {"RunsPerMinute", runsPerMinute},
                    });
                    writeResults(completedRun);
                });
            }

            void updateRunsPerMinute(long lastRunTimeMillis) {
                var lastCompletionTime = last(completionTimes);
                var windowStart = lastCompletionTime.AddMilliseconds(-3 * lastRunTimeMillis);
                completionTimes.Sort();
                while(completionTimes[0].CompareTo(windowStart) < 0) completionTimes.RemoveAt(0);
                var minutes = last(completionTimes).Subtract(first(completionTimes)).TotalMinutes;
                runsPerMinute = safeDivide(0, completionTimes.Count, minutes);
            }

            void writeResults(int run) {
                var results = MetricFiles.readFromS3(systemId, run);
                writer.writeResults(run, results);
            }


            public SqsDbServer startDbServer() {
                return runner.s3Cache().startDbServer();
            }

            public void killRun(string message) {
                LogC.info("Run killed: " + message);
                LogC.info("stopping " + numInstances + " instances.");
                stopInstances();
            }

            public void createDirectories() {
                writer.createDirectories();
            }

            public void copyMetrics() {
                writer.copyMetrics();
            }
        }
    }
}
