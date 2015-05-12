using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Threading;
using jms;
using Q.Util;
using util;
using RE = RightEdge.Common;
using JTick = systemdb.data.Tick;
using Symbol=Q.Trading.Symbol;

namespace Q.Plugins {
    public class LiveDataStressTest : Objects, RE.IService, RE.ITickRetrieval {
        private RE.GotTickData tickListener;
        readonly Dictionary<Symbol, Thread> threads = new Dictionary<Symbol, Thread>();
        bool watching;
        #region IService Members

        public string ServiceName() {
            return "Quantys Live Data Stress Test";
        }

        public string Author() {
            return "Malbec Quantys Fund";
        }

        public string Description() {
            return "Random Time Series Data Provider";
        }

        public string CompanyName() {
            return "Malbec Quantys Fund";
        }

        public string Version() {
            return "0.1a";
        }

        public string id() {
            return "{655D7233-B5E1-49ef-9DB0-A2D8149E42C2}";
        }

        public bool NeedsServerAddress() {
            return false;
        }

        public bool NeedsPort() {
            return false;
        }

        public bool NeedsAuthentication() {
            return false;
        }

        public bool SupportsMultipleInstances() {
            return false;
        }

        public RE.IBarDataRetrieval GetBarDataInterface() {
            return null;
        }

        public RE.ITickRetrieval GetTickDataInterface() {
            return this;
        }

        public RE.IBroker GetBrokerInterface() {
            return null;
        }

        public bool HasCustomSettings() {
            return false;
        }

        public bool ShowCustomSettingsForm(ref RE.SerializableDictionary<string, string> settings) {
            return false;
        }

        public bool Initialize(RE.SerializableDictionary<string, string> settings) {
            return true;
        }

        public bool Connect(RE.ServiceConnectOptions connectOptions) {
            return true;
        }

        public bool Disconnect() {
            return true;
        }

        public string GetError() {
            return "";
        }

        public string ServerAddress {
            get { return ""; }
            set { }
        }

        public int Port {
            get { return 0; }
            set { }
        }

        public string UserName {
            get { return ""; }
            set { }
        }

        public string Password {
            get { return ""; }
            set { }
        }

        public bool BarDataAvailable {
            get { return false; }
        }

        public bool TickDataAvailable {
            get { return true; }
        }

        public bool BrokerFunctionsAvailable {
            get { return false; }
        }

        #endregion

        #region IDisposable Members

        public void Dispose() {
            StopWatching();
        }

        #endregion

        #region ITickRetrieval Members

        public bool SetWatchedSymbols(List<RE.Symbol> symbols) {
            if(isEmpty(symbols)) {
                Channel.closeResources();
                each(threads.Values, thread => thread.Abort());
                threads.Clear();
                return true;
            }
            foreach (var symbol in symbols) {
                var tempSymbol = symbol;
                var qSymbol = new Symbol(symbol);
                if (threads.ContainsKey(qSymbol)) continue;
                var rng = new Random();
                threads[qSymbol] = new Thread(() => {
                    var delay = 1000;
                    var time = date(Dates.todayAt("05:00:00"));
                    while(trueDat()) {
                        var tick = new RE.TickData {
                            price = rng.NextDouble() * 100,
                            size = (ulong) rng.Next(),
                            tickType = RE.TickType.Trade,
                            time = time
                        };
                        tickListener(tempSymbol, tick);
                        sleep(delay);
                        if(delay > 10)
                            delay -= 10;
                    }
                });
                threads[qSymbol].Start();
            }
            return true;
        }
        
        static bool trueDat() {
            return true;
        }

        public bool IsWatching() {
            return watching;
        }

        public bool StartWatching() {
            Directory.CreateDirectory(@"C:\logs");
            LogC.setOut("LiveData", @"C:\logs\QLiveData" + Process.GetCurrentProcess().Id + ".log", true);
            watching = true;
            return true;
        }

        public bool StopWatching() {
            watching = false;
            return true;
        }

        public RE.IService GetService() {
            return this;
        }

        public bool RealTimeDataAvailable {
            get { return true; }
        }

        public RE.GotTickData TickListener {
            set { tickListener = value; }
        }

        #endregion
    }
}