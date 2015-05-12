using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.ServiceProcess;
using file;
using Q.Messaging;
using Q.Util;
using systemdb.data;
using O=Q.Util.Objects;

namespace Q.Local {
    public class Bootstrap : ServiceBase {
                        
        static readonly LazyDictionary<string, List<Process>> processes = new LazyDictionary<string, List<Process>>(k => new List<Process>());
        public const string LOCAL_CLOUD_BROKER = "tcp://amqlocalcloud:62500";
        static readonly Topic STARTUP;
        static readonly Topic SHUTDOWN;
        public Heartbeat heart;

        static Bootstrap() {
            var mountDisks = Process.Start(@"\\nysrv58\netlogon\fftwlogin.bat");
            if(mountDisks != null)
                mountDisks.WaitForExit();
            else throw new Exception("error running process ");
            LogC.eventInfo("mount disks returned " + mountDisks.ExitCode, "QRun");
            STARTUP = new Topic("LocalSTO.Start", LOCAL_CLOUD_BROKER, false);
            SHUTDOWN = new Topic("LocalSTO.Stop", LOCAL_CLOUD_BROKER, false);

        }

        public Bootstrap() {
            ServiceName = "Tornado";
            CanStop = true;
            CanPauseAndContinue = false;
            AutoLog = true;
        }

        protected override void OnStart(string[] args) {
            LogC.useJavaLog = true;
            LogC.setOut("launcher", @"C:\logs\Bootstrap.log", true);
            LogC.setVerboseLoggingForever(true);
            STARTUP.subscribe(startProcesses);
            SHUTDOWN.subscribe(stopProcesses);
            heart = new Heartbeat(LOCAL_CLOUD_BROKER, "Tornado.heartbeat.bootstrap", 3000);
            heart.initiate();
            var juggernet = new QDirectory("T:\\JuggerNET\\");
            if (!juggernet.exists()) 
                LogC.info("can't access network drives, try running on a different system or as a different user(change service logon to use FFTW\\<username>) " + juggernet.path() + " running as " + O.username());
        }

        static void stopProcesses(Fields fields) {
            var mainDir = fields.text("mainDir");
            var command = fields.text("command");
            stopProcesses(mainDir + command);
        }

        static void stopProcesses(string key) {
            O.each(O.copy(processes.get(key)), process => {
                if(!process.HasExited) {
                    LogC.info("killing pid "  + process.Id);
                    process.Kill();
                }
                processes.get(key).Remove(process);
            });
            LogC.info("all processes killed for " + key);
        }

        static void startProcesses(Fields fields) {
            var nProcs = fields.integer("numProcs");
            var mainDir = fields.text("mainDir");
            var command = fields.text("command");
            Environment.SetEnvironmentVariable("MAIN", mainDir);
            O.zeroTo(nProcs, i => {
                LogC.info("starting " + mainDir + " " + command + " -serverIndex " + i);
                try {
                    var exe = new QFile(mainDir + @"\dotNET\QRun\bin\Release\QRun.exe");
                    Bomb.unless(exe.exists(), () => "no exe found at " + exe.path());
                    var process = Process.Start(exe.path(), command  + " -serverIndex " + i);
                    processes.get(mainDir + command).Add(process);
                } catch (Exception e) {
                    LogC.err("failure to launch!", e);
                    throw Bomb.toss("failure to launch!", e);
                }
            });
        }

        protected override void OnStop() {
            O.each(processes.keys(), stopProcesses);
        }

        public static void sendStartCommand(int numProcs, string mainDir, string command) {
            var fields = new Fields();
            fields.put("numProcs", numProcs);
            fields.put("mainDir", mainDir);
            fields.put("command", command);
            STARTUP.send(fields);
        }

        public static void sendStopCommand(string mainDir, string command) {
            var fields = new Fields();
            fields.put("mainDir", mainDir);
            fields.put("command", command);
            SHUTDOWN.send(fields);
        }
    }
}
