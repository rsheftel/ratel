using System;
using System.Collections.Generic;
using file;
using Q.Recon;
using Q.Trading.Results;
using Q.Util;
using systemdb.data;
using systemdb.metadata;

namespace Q.Trading {
    public class SystemArguments : Objects {
        public readonly List<Symbol> symbols;
        public readonly List<Portfolio> portfolios = new List<Portfolio>();
        public readonly Parameters parameters;
        public readonly int leadBars;
        public bool runInNativeCurrency { get; set; }

        public event Action<Collectible, PlotDefinition> newPlotRequest;

        public SystemArguments(Symbol symbol, Parameters parameters) : this(list(symbol), parameters) {}
        public SystemArguments(IEnumerable<Symbol> symbols, Parameters parameters) : this(symbols, new List<Portfolio>(), parameters) {}
        public SystemArguments(IEnumerable<Symbol> symbols, IEnumerable<Portfolio> portfolios, Parameters parameters) : this(symbols, portfolios, parameters, parameters.leadBars()) {}

        public SystemArguments(
            IEnumerable<Symbol> symbols, 
            IEnumerable<Portfolio> portfolios,
            Parameters parameters, 
            int leadBars
        ) {
            this.symbols = list(symbols);
            this.portfolios.AddRange(portfolios);
            this.parameters = parameters;
            this.leadBars = leadBars;
            newPlotRequest += doNothing;
            var details = SystemDetailsTable.DETAILS.details(parameters.get("systemId", -1));
            runInNativeCurrency = details.runInNativeCurrency();
        }

        

        public SystemArguments with(Symbol symbol) {
            return new SystemArguments(list(symbol), portfolios, parameters, leadBars);
        }

        public QREBridge<S> bridge<S>() where S : System {
            return (QREBridge<S>) bridgeBase();
        }

        public QREBridgeBase bridgeBase() {
            return bridgeBase(OrderTable.prefix);
        }

        public QREBridgeBase bridgeBase(string topicPrefix) {
            var systemType = Type.GetType(parameters.systemClassName(), true, false);
            if(systemType.IsSubclassOf(typeof(SymbolSystem)))
                systemType = typeof (IndependentSymbolSystems<>).MakeGenericType(systemType);
            var bridgeType = typeof (QREBridge<>).MakeGenericType(systemType);
            var c = bridgeType.GetConstructor(new[] { typeof(SystemArguments), typeof(string) });
            return (QREBridgeBase) c.Invoke(new object[] {this, topicPrefix});
        }

        public void logSystemCreation() {  
            parameters.logSystemCreation(this);
        }

        public void forSymbols(Action<Symbol> run) {
            each(symbols, run);
        }        
        
        public void fireNewPlotRequest(Collectible collectible, PlotDefinition plot) {
            newPlotRequest(collectible, plot);
        }

        public int systemId() {
            return parameters.get("systemId", -1);
        }

        public RunMode runMode() {
            return (RunMode) parameters.get("RunMode", (int) RunMode.RIGHTEDGE);
        }

        public void setLogFile(QDirectory dir) {
            dir.createIfMissing();
            switch (runMode()) {
                case RunMode.LIVE:
                    LogC.setOut("System", dir.file("Q_" + liveSystem().name() + ".log").path(), true);
                    break;
                case RunMode.RIGHTEDGE:
                    LogC.setOut("System", dir.file("Q.log").path(), true);
                    break;
                case RunMode.STO:
                    LogC.setOut("System", dir.file("Q_" + siv().name() + ".log").path(), true);
                    break;
                default:
                    Bomb.toss("unhandled run mode setting log file!");
                    break;
            }
        }

        public LiveSystem liveSystem() {
            return SystemDetailsTable.liveSystem(systemId());
        }

        public Siv siv() {
            return SystemDetailsTable.siv(systemId());
        }

        public string curveDir(string marketName) {
            return parameters.curveFilePath(siv(), marketName);
        }

        public Interval interval() {
            return Interval.lookup(siv().interval());
        }
    }
}