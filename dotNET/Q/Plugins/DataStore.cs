using System;
using System.Collections.Generic;
using System.IO;
using java.util;
using Q.Util;
using RE=RightEdge.Common;
using util;
using JBar = systemdb.data.Bar;
using JSymbol = systemdb.data.Symbol;

namespace Q.Plugins {
    public class DataStore : Objects, RE.IBarDataStorage {

        static bool initialized;

        public DataStore() {
            LogC.info("In DataStore constructor");
        }

        static DataStore() {
            LogC.info("In DataStore static");
            initialize();
        }

        public static void initialize() {
            if (initialized) return;
            Directory.CreateDirectory(@"C:\logs");
            LogC.setOut("DataStore", @"C:\logs\QDataStore" + processId() + ".log", true);
            initialized = true;
        }

        #region IBarDataStorage Members

        public List<RE.BarData> LoadBars(RE.SymbolFreq symbolFreq, DateTime startDateTime, DateTime endDateTime) {
            var symbol = jSymbol(symbolFreq);
            var range = range_(startDateTime, endDateTime);
            Log.info("CALLBACK: LoadBars : range " + range + " for symbol " + symbol);
            return barDatas(symbol.bars(range));
        }

        static List<RE.BarData> barDatas(List bars) {
            return list(convert(list<JBar>(bars), delegate(JBar j) {
                var bar = new RE.BarData(
                    date(j.date()),
                    j.open(),
                    j.close(),
                    j.high(),
                    j.low()
                );
                if (j.volume() != null)
                    bar.Volume = (ulong) j.volume().longValue();
                if (j.openInterest() != null)
                    bar.OpenInterest = j.openInterest().intValue();
                return bar;
            }));
        }

        static JSymbol jSymbol(RE.SymbolFreq protoSymbol) {
            Bomb.unless(protoSymbol.Frequency.Equals(1440), () => "only daily bars are currently supported");
            return new JSymbol(protoSymbol.Symbol.Name, protoSymbol.Symbol.SymbolInformation.ContractSize);
        }

        public List<RE.BarData> LoadLastBars(RE.SymbolFreq symbolFreq, int barCount) {
            var symbol = jSymbol(symbolFreq);
            Log.info("CALLBACK: LoadLastBars : count " + barCount + " for symbol " + symbol);
            return barDatas(symbol.lastBars(barCount));
        }

        public int GetBarCount(RE.SymbolFreq symbolFreq) {
            var symbol = jSymbol(symbolFreq);
            Log.info("CALLBACK: GetBarCount : for symbol " + symbol);
            return symbol.barCount();
        }

        public DateTime GetLastBarDate(RE.SymbolFreq symbolFreq) {
            var symbol = jSymbol(symbolFreq);
            Log.info("CALLBACK: GetLastBarDate : for symbol " + symbol);
            return date(symbol.lastBarDate());
        }

        public DateTime GetFirstBarDate(RE.SymbolFreq symbolFreq) {
            var symbol = jSymbol(symbolFreq);
            Log.info("CALLBACK: GetFirstBarDate : for symbol " + symbol);
            return date(symbol.firstBarDate());
        }

        public int SaveBars(RE.SymbolFreq symbol, List<RE.BarData> bars) {
            throw new NotImplementedException();
        }

        public bool DeleteBars(RE.SymbolFreq symbol) {
            throw new NotImplementedException();
        }

        public int SaveTicks(RE.Symbol symbol, List<RE.TickData> ticks) {
            throw new NotImplementedException();
        }

        public void SaveTick(RE.Symbol symbol, RE.TickData tick) {
            throw new NotImplementedException();
        }

        public int UpdateTicks(RE.Symbol symbol, List<RE.TickData> newTicks) {
            throw new NotImplementedException();
        }

        public List<RE.TickData> LoadTicks(RE.Symbol symbol, DateTime startDate) {
            throw new NotImplementedException();
        }

        public List<RE.TickData> LoadTicks(RE.Symbol symbol, DateTime startDate, DateTime endDate) {
            throw new NotImplementedException();
        }

        public bool DeleteTicks(RE.Symbol symbol) {
            throw new NotImplementedException();
        }

        public bool RequiresSetup() { return false; }
        public bool IsProperlyConfigured() { return true; }
        public void DoSettings() {}
        public void ForceDefaultSettings() {}
        public string LastError() { return "No errors."; }
        public string GetName() { return "Quantys SystemDB"; }
        public string GetDescription() { return "Quantys SystemDB"; }
        public string CompanyName() { return "Malbec Quantys Fund"; }
        public string Version() { return "2.0"; }
        public string id() { return "{EA810604-CFCB-4a65-A174-7815098CE564}"; }
        public void Flush() {}
        #endregion
    }
}