using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using Codemesh.JuggerNET;
using O=Q.Util.Objects;
using TraceLevel=Codemesh.JuggerNET.TraceLevel;

namespace Q.Util {
    [JuggerNETInit]
    public class InitJVM {
        static readonly string TRACE_FILE_NAME = @"c:\logs\Juggernet." + Process.GetCurrentProcess().Id + ".log";
        public static bool bypass() { return true; }
        static readonly bool bypassJavaDebugging = bypass();

        public static void Init() {
            try {
                Directory.CreateDirectory(@"C:\logs");
                LogC.makeOld(TRACE_FILE_NAME);
            } 
            catch {
                LogC.info("Cannot makeOld logfile (might be locked): " + TRACE_FILE_NAME, false);
            }
            JvmLoader.RegisterConfigurationHook(ConfigMethod);
        }

        public static void ConfigMethod(IJvmLoader loader, int when) {
            if (when != (int) When.XMOG_BEFORE_LOADING) return;
            var tempDir = createTempDirIfNeeded();
            var heapSize = 128;
            var heapEnv = Environment.GetEnvironmentVariable("JAVA_HEAP_MB");
            if(!string.IsNullOrEmpty(heapEnv)) heapSize = int.Parse(heapEnv);
            loader.MaximumHeapSizeInMB = heapSize;
            loader.ClassPath = ".";
            foreach(var file in javaLibJars()) {
                var tempName = tempDir + "\\" + file.Name + "." + Path.GetRandomFileName();
                file.CopyTo(tempName);
                loader.AppendToClassPath(tempName);
            }
            //loader.SetTraceLevel(TraceFacility.TraceAll, TraceLevel.TraceVerbose);
            loader.SetTraceLevel(TraceFacility.TraceAll, TraceLevel.TraceError);
            loader.TraceFile = TRACE_FILE_NAME;
            loader.JvmPath = Env.javaHome(@"jre\bin\client\jvm.dll");
            loader.LibraryPath = Environment.GetEnvironmentVariable("PATH") + ";" + Env.svn(@"Java\systematic");
            loader.DashXOption["ss"] = "2M";
            if(bypassJavaDebugging) return;
            loader.Debug = true;
            loader.Run = "jdwp:transport=dt_socket,server=y,suspend=n,address=5044";
            loader.NoAgent = true;
            loader.DashDOption[ "java.compiler" ] = "NONE";
        }

        static string createTempDirIfNeeded() {
            var tempDir = Path.GetTempPath() + "QInitJvmJARs";
            var dir = Directory.CreateDirectory(tempDir);
            var yesterday = DateTime.Now.Subtract(new TimeSpan(1, 0, 0, 0));
            foreach(var file in dir.GetFiles()) {
                try {
                    if(file.LastWriteTime.CompareTo(yesterday) < 0)
                        file.Delete();
                } catch {
                    doNothing();
                }
            }
            return tempDir;
        }

        static void doNothing() {}

        static FileInfo[] javaLibJars() {
            var qJars = new DirectoryInfo(Env.svn(@"Java\systematic\lib")).GetFiles("*.jar");
            var fJars = new DirectoryInfo(@"C:\WINDOWS\system32\lib").GetFiles("*.jar");
            var l = new List<FileInfo>(qJars);
            l.AddRange(fJars);
            return l.ToArray();
        }
    }
}