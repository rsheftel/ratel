using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.ServiceProcess;
using amazon;
using file;
using Q.Util;
using O=Q.Util.Objects;

namespace Q.Amazon {
    public class Bootstrap : ServiceBase {
        static readonly List<Process> processes = new List<Process>();

        public Bootstrap() {
            ServiceName = "AmazonBootstrap";
            CanStop = true;
            CanPauseAndContinue = false;
            AutoLog = true;
        }


        protected override void OnStart(string[] args) {
            var timeSync = Process.Start(@"C:\WINDOWS\system32\w32tm.exe", "/resync");
            if(timeSync == null) throw Bomb.toss("unable to start time sync process");
            timeSync.WaitForExit();
            var userData = EC2Runner.userData();
            var nProcs = int.Parse((string) userData.get("numProcs"));
            var command = (string) userData.get("command");
            new EC2Runner((string) userData.get("requestQueue")).downloadJarsAndQRunFromS3(new QDirectory("E:/svn"));
            Environment.SetEnvironmentVariable("MAIN", @"E:\svn");
            O.zeroTo(nProcs, i => startProcess(command));
        }

        protected override void OnStop() {
            O.each(processes, process => process.Kill());
        }

        static void startProcess(string command) {
            processes.Add(Process.Start(@"E:\svn\dotNET\QRun\bin\Release\QRun.exe", command));
        }
    }
}
