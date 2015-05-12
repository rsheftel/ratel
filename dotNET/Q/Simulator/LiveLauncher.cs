using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.ServiceProcess;
using System.Threading;
using mail;
using NUnit.Framework;
using Q.Messaging;
using Q.Recon;
using Q.Util;
using systemdb.data;
using systemdb.metadata;
using util;
using O=Q.Util.Objects;

namespace Q.Simulator {
    public class LiveLauncher : ServiceBase {
        readonly Dictionary<string, Process> processes = new Dictionary<string, Process>();
        static readonly string QRUN = Systematic.mainDir().file(@"dotNET/QRun/bin/Debug/QRun.exe").path();
        public const long BEAT_WAIT_MILLIS = 3000;
        Timer heartbeatTimer;
        bool inTestMode;
        readonly List<string> fakeStarts = new List<string>();

        public LiveLauncher() {
            ServiceName = "Tomahawk";
            CanStop = true;
            CanPauseAndContinue = false;
            AutoLog = true;
        }

        protected override void OnStart(string[] args) {
            run();
        }

        internal void run() {
            loadSharedDrives();
            LogC.useJavaLog = true;
            LogC.setOut("launcher", @"C:\logs\LiveLauncher.log", true);
            try {
                O.wait(() => !FerretControl.status().Equals("Unknown"));
            } catch (Exception e) {
                Email.problem("Cannot start Tomahawk - Ferret is DOWN?", "Cannot get Ferret status.  Is it down?").sendTo("live");
                Bomb.toss("Cannot get Ferret status", e);
            }
            Console.CancelKeyPress += (sender, unused) => O.eachValue(processes, killIfNeeded);

            killTopic().subscribe(kill);
            restartTopic().subscribe(restart);
            initiateHeartbeat();
        }

        static void loadSharedDrives() {
            O.runCommand("cmd", @"/c \\nysrv57\NETLOGON\fftwlogin.bat");
        }

        protected override void OnStop() {
            if(heartbeatTimer != null) {
                heartbeatTimer.Dispose();
                heartbeatTimer = null;
            }
            O.eachValue(processes, killIfNeeded);
            processes.Clear();
        }

        public static Topic restartTopic() {
            return new Topic(OrderTable.prefix + ".RESTART", false);
        }

        public static Topic killTopic() {
            return new Topic(OrderTable.prefix + ".KILL", false);
        }

        internal void start(string systemName) {
            LogC.info("starting " + systemName);
            
            if (BloombergTagsTable.TAGS.anyAutoExecute(systemName) && !FerretControl.status().Equals("Stage")) {
                Email.problem("Can't start autoex system " + systemName, "Ferret is not in Stage mode!").sendTo(Systematic.failureAddress());
                return;
            }
            if(inTestMode) { fakeStarts.Add(systemName); return; }
            processes[systemName] = Process.Start(QRUN, "Q.Simulator.Live -system " + systemName + " -prefix " + OrderTable.prefix);
        }

        public void restart(Fields fields) {
            var systemName = system(fields);
            kill(systemName);
            var isMe = fields.text("Hostname").Equals(O.hostname());
            if(!isMe) return;
            start(systemName);
        }

        static string system(Fields fields) {
            return fields.text("System");
        }

        void kill(Fields fields) {
            kill(system(fields));
        }

        void kill(string systemName) {
            if (processes.ContainsKey(systemName)) killIfNeeded(processes[systemName]);
        }

        void killIfNeeded(Process process) {
            LogC.info("killing " + O.the(O.accept(processes, entry => entry.Value.Equals(process))).Key);
            if(!process.HasExited) process.Kill();
        }

        void initiateHeartbeat() {
            O.timerManager().everyMillis(BEAT_WAIT_MILLIS, () => publishHeartbeatFrom(O.hostname()), out heartbeatTimer);
        }

        public static void subscribeHeartbeat(Action<string, DateTime> hostAlive) {
             launcherHeartbeatTopic().subscribe(fields => 
                 hostAlive(fields.text("Hostname"), O.date(fields.time("Timestamp"))));
        }

        public static void publishHeartbeatFrom(string host) {
            var fields = new Fields();
            fields.put("Hostname", host);
            fields.put("Timestamp", O.ymdHuman(O.now()));
            launcherHeartbeatTopic().send(fields);
        }

        static Topic launcherHeartbeatTopic() {
            return new Topic("TOMAHAWK.launcherHeartbeat");
        }

        public void beInTestMode() {
            inTestMode = true;
        }

        public void requireStarted(string system) {
            Assert.Contains(system, fakeStarts);
        }

        public void noneStarted() {
            Assert.IsEmpty(fakeStarts);
        }
    }
}
