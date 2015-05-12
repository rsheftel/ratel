using System;
using System.Collections.Generic;
using System.IO;
using amazon;
using mail;
using Q.Simulator;
using Q.Trading;
using Q.Util;
using systemdb.metadata;
using util.web;

namespace Q.Amazon {
    public class STOServer : Objects {
        static List<Symbol> symbols;
        static SystemDbBarLoader barData;

        public static void Main(string[] args) {
            Directory.CreateDirectory(@"E:\logs");
            LogC.setOut("STOServer", @"E:\logs\STOServer." + processId() + ".log", true);
            LogC.setErr(@"E:\logs\STOServer." + processId() + ".error.log");
            AppDomain.CurrentDomain.UnhandledException += handleUncaughtExceptions;
            S3Cache.setDefaultSqsDbMode(true);
            if(!(hostname().StartsWith("NY") || hostname().StartsWith("LN")))
                QHttpClient.turnOffProxy();
            var runner = EC2Runner.fromUserData();
            S3Cache.setS3Cache(runner.s3Cache());
            var lastRunTime = 120;
            while(trueDat()) {
                info("lastRunTime: " + lastRunTime);
                STORequest request = null;
                try {
                    lastRunTime = processNextMessage(runner, lastRunTime, out request);
                } catch(Exception e) {
                    LogC.err("failed " + request, e);
                    LogC.info("failed " + request, e);
                    continue;
                } 
                info("finished " + request);
            }
        }

        static int processNextMessage(EC2Runner runner, int lastRunTime, out STORequest request) {
            var message = runner.nextMessage(Math.Min(7200, 2 * lastRunTime));
            var start = now();
            request = new STORequest(message.@object());
            info("received " + request);
            if(symbols == null || barData == null) cacheSymbolData(request);
            else request.requireMatch(symbols);
            var simulator = new Simulator.Simulator(new SystemArguments(symbols, request.portfolios, request.parameters), barData, "QUEDGE");
            simulator.processBars();
            simulator.shutdown();
            lastRunTime = Math.Max((int) now().Subtract(start).TotalSeconds, 10);
            runner.responseQueue().send(request.response(EC2Runner.instanceId(), start, now(), processId()));
            message.delete(); 
            return lastRunTime;
        }

        static void handleUncaughtExceptions(object sender, UnhandledExceptionEventArgs e) {
            var ex = (Exception) e.ExceptionObject;
            Email.problem("STOServer crashed (" + hostname() + ")", LogC.errMessage("uncaught exception ", ex)).sendTo("us");
        }

        static void cacheSymbolData(STORequest request) {
            symbols = request.symbols;
            var systemId = request.parameters.get<int>("systemId");
            var symbolRanges = dictionary(symbols, symbol => MsivBacktestTable.BACKTEST.range(systemId, symbol.name));
            barData = new SystemDbBarLoader(SystemDetailsTable.DETAILS.details(systemId).interval(), symbols, symbolRanges);
        }

    }
}