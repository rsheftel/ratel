using System;
using System.Collections;
using System.Diagnostics;
using System.IO;
using System.Text.RegularExpressions;
using System.Threading;
using util;
using File=System.IO.File;
using O = Q.Util.Objects;

namespace Q.Util {
    public class LogC {
        static string lastMessage;
        static TextWriter errors = Console.Error;
        static TextWriter outFile = Console.Out;
        static string outFileName;
        static string errFileName;
        public static bool useJavaLog;
        //not lazy because lazy uses verbose logging, can't use produce because it's used too soon for O to be valid.
        static bool isVerbose = calcIsVerbose();
        static bool calcIsVerbose() {
            var verbose = Environment.GetEnvironmentVariable("LOG_VERBOSE") ?? "FALSE";
            return verbose.Length != 0 && verbose.Substring(0, 1).ToLower().Equals("t");
        }
        // param is used to force declarer to know that the result is the newValue
        public static void flipVerbose(out bool newValue) {
            isVerbose = !isVerbose;
            setVerboseLoggingForever(isVerbose);
            newValue = isVerbose;
        }

        public static void info(string s) {
            info(s, true);
        }

        public static void verbose(Producer<string> message) {
            if(isVerbose)
                info(message());
        }

        public static void info(string s, bool doProlog) {
            if (useJavaLog) { Log.info(s); return; }
            lastMessage = s;
            var prolog_ = doProlog ? prolog() : "";
            outFile.WriteLine(prolog_ + s);
            outFile.Flush();
        }

        static object prolog() {
            var thread = O.paren("c" + Thread.CurrentThread.ManagedThreadId);
            var reallyNow = DateTime.Now.ToString("yyyy/MM/dd HH:mm:ss");
            var frozenNow = Dates.nowFrozen() ? O.paren(O.ymdHuman(O.now())) : "";
            return thread + " " + reallyNow + frozenNow + ": ";
        }

        public static void infoOnce(string s) {
            if (lastMessage == s) return;
            info(s);
        }

        public static void info(string s, Exception e) {
            info(errMessage(s, e));
        }

        public static void err(string s, Exception e) {
            err(errMessage(s, e));
        }

        public static string errMessage(string s, Exception e) {
            var message = s;
            while (e != null) {
                message += " caused by: " + e.Message + "\r\n";
                var stack = e.StackTrace;
                if (stack != null && stack.Trim().Length != 0)
                    message += e.StackTrace + "\r\n";
                e = e.InnerException;
            }
            return message;
        }

        public static void err(string s) {
            if (useJavaLog) { Log.info(s); return; }
            errors.WriteLine(prolog() + s);
            errors.Flush();
        }

        public static void setErr(string s) {
            if (O.isEmpty(s)) {
                errors = Console.Error;
                errFileName = null;
                consoleOut("now logging errors to console");
                return;
            }
            if (s.Equals(errFileName)) {
                consoleOut("keeping already set error log file " + s);
                return;
            }
            if (!O.isEmpty(errFileName)) {
                errors.Flush();
                errors.Close();
            }
            errFileName = s;
            var file = new FileInfo(s);
            errors = file.Exists ? file.AppendText() : file.CreateText();
            consoleOut("now logging errors to " + s);

        }

        public static void consoleOut(string s) {
            Console.Out.WriteLine(prolog() + s);
        }

        public static void setOut(string source, string destination, bool doMakeOld) {
            if (O.isEmpty(destination)) {
                consoleOut("was logging to " + outFileName);
                outFile = Console.Out;
                outFileName = null;
                consoleOut("now logging to console");
                Log.setFile(@"C:\logs\c#." + Process.GetCurrentProcess().Id + ".java.log");
                return;
            }
            if (destination.Equals(outFileName)) return;
            if (!O.isEmpty(outFileName)) {
                outFile.Flush();
                outFile.Close();
            }
            outFileName = destination;
            if (doMakeOld) makeOld(destination);
            var file = new FileInfo(destination);
            try { outFile = file.Exists ? file.AppendText() : file.CreateText(); }
            catch(Exception e) {
                try { tryAnotherFile(destination, source); }
                catch (Exception e2) {
                    lastDitchErrorLog(@"C:\logs\logging_failure", e);
                    lastDitchErrorLog(@"C:\logs\logging_failure", e2);
                }
                return;
            }
            setJavaLog(destination, doMakeOld);
            consoleOut(source + ": now logging to " + destination);
        }

        static void tryAnotherFile(string destination, string source) {
            var match = Regex.Match(destination, @"(.*)\.([^\.]+)$");
            var extension = match.Groups[2].Value;
            var everythingElse = match.Groups[1].Value;
            Bomb.unless(match.Success, () => "log file must have an extension: " + destination);
            match = Regex.Match(everythingElse, @"(.*)\.(\d+)$");
            string nextDestination;
            if (match.Success) {
                var number = int.Parse(match.Groups[2].Value) + 1;
                Bomb.unless(number < 10, () => "log files 1-9 are locked, nowhere to log to!");
                var prefix = match.Groups[1].Value;
                nextDestination = O.join(".", O.list(prefix, "" + number, extension));
            }
            else
                nextDestination = everythingElse + ".2." + extension;
            setOut(source, nextDestination, true);
        }

        static void setJavaLog(string destination, bool doMakeOld) {
            destination = destination.Replace(".log", ".java.log");
            if (!destination.EndsWith(".java.log")) destination = destination + ".java.log";
            if (doMakeOld) makeOld(destination);
            Log.setFile(destination);
        }

        public static string errFile() {
            return errFileName;
        }

        [Obsolete("uses should never be checked in")] 
        public static void debug(string s) {
            info(s);
        }

        //[Obsolete("uses should never be checked in")] 
        public static void debugForAWhile(string s) {
            info(s);
        }

        [Obsolete("uses should never be checked in")] 
        public static void debug<T>(T t, string s) {
            var value = t is IEnumerable && !(t is string) ? Objects.toShortString(t as IEnumerable) : t + "";
            info(s + " => " + value);
        }

        [Obsolete("uses should never be checked in")] 
        public static void debugSafe(string s) {
            Console.Error.WriteLine(s);
        }
        
        [Obsolete("uses should never be checked in")] 
        public static void stack(string s) {
            info(s + "\r\n" + Environment.StackTrace);
        }

        public static void makeOld(string file) {
            try {
                if(File.Exists(file + ".oldest")) File.Delete(file + ".oldest");
                if(File.Exists(file + ".older")) File.Move(file + ".older", file + ".oldest");
                if(File.Exists(file + ".old")) File.Move(file + ".old", file + ".older");
                if(File.Exists(file)) File.Move(file, file + ".old");
            } catch (Exception e) {
                lastDitchErrorLog(file, e);
            }
        }

        public static void makeOldDir(string dir) {
            try {
                if(Directory.Exists(dir + ".oldest")) Directory.Delete(dir + ".oldest", true);
                if(Directory.Exists(dir + ".older")) Directory.Move(dir + ".older", dir + ".oldest");
                if(Directory.Exists(dir + ".old")) Directory.Move(dir + ".old", dir + ".older");
                if(Directory.Exists(dir)) Directory.Move(dir, dir + ".old");
            } catch (Exception e) {
                lastDitchErrorLog(dir, e);
            }
        }

        static void lastDitchErrorLog(string file, Exception e) {
            // bug if this fails we are truly skrood
            try {
                using (var appender = File.AppendText(file + ".error")) 
                    appender.WriteLine(DateTime.Now + errMessage("failed makeOld on " + file + "\r\n", e));
            } catch {
                consoleOut("god i wish this message was visible from rightedge.");
            }
        }

        [Obsolete("uses should never be checked in")] 
        public static void note(string s) {}
        
        [Obsolete("uses should never be checked in")] 
        public static void jDebug(string s) {
            Log.info(s);
        }

        public static void eventInfo(string message, string eventSource) {
            if (!EventLog.SourceExists(eventSource)) 
                EventLog.CreateEventSource(new EventSourceCreationData(eventSource, "Quantys"));
            EventLog.WriteEntry(eventSource, message, EventLogEntryType.Information);
        }

        public static void eventError(string message, Exception e, string eventSource) {
            if (!EventLog.SourceExists(eventSource)) 
                EventLog.CreateEventSource(new EventSourceCreationData(eventSource, "Quantys"));
            EventLog.WriteEntry(eventSource, errMessage(message, e), EventLogEntryType.Error);
        }

        public static void eventWarn(string message, string eventSource) {
            if (!EventLog.SourceExists(eventSource)) 
                EventLog.CreateEventSource(new EventSourceCreationData(eventSource, "Quantys"));
            EventLog.WriteEntry(eventSource, message, EventLogEntryType.Warning);
        }

        [Obsolete("verbose logging should not be checked in!")] 
        public static void setVerboseLogging(bool b) {
            info("setting verbose logging = " + b);
            Log.setVerboseLogging(b);
        }

        public static void setVerboseLoggingForever(bool b) {
            info("setting verbose logging to " + b);
            Log.setVerboseLoggingForever(b);
        }

        [Obsolete]
        public static void logGarbageCollections() {
            info("Max GC generation = " + GC.MaxGeneration);
            new Thread(() => {
                var lastCount = GC.CollectionCount(2);
                var lastTotalMemory = GC.GetTotalMemory(false);
                while(O.produce(() => true)) { 
                    var newTotalMemory = GC.GetTotalMemory(false);
                    if (lastCount == GC.CollectionCount(2)) continue;
                    lastCount = GC.CollectionCount(2);
                    info("garbage collected " + lastCount + ", memory diff = " + (lastTotalMemory - newTotalMemory));
                    lastTotalMemory = newTotalMemory;
                }
            }).Start();
        }

        public static void ignore(object o) {}
    }
}
