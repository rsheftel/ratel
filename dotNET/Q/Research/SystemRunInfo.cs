using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using file;
using Q.Simulator;
using Q.Trading;
using Q.Trading.Results;
using Q.Util;
using systemdb.data;
using Market=systemdb.metadata.Market;
using Symbol=Q.Trading.Symbol;

namespace Q.Research {
    [Serializable]
    public class SystemRunInfo : Objects {
        readonly List<string> markets;
        public readonly Parameters parameters;
        public readonly DateTime? startDate;
        public readonly DateTime? endDate;
        public readonly string slippageCalculatorName;
        public readonly bool runInNativeCurrency;
        [NonSerialized] readonly Type slippageCalculator;
        [NonSerialized] readonly Lazy<List<Symbol>> symbols_;

        public SystemRunInfo(SerializationInfo info, StreamingContext context) {
            slippageCalculator = calculator(slippageCalculatorName);
        }

        public SystemRunInfo(List<string> markets, Parameters parameters, DateTime? startDate, DateTime? endDate, string slippageCalculatorName, bool runInNativeCurrency) {
            this.markets = markets;
            this.parameters = parameters;
            this.startDate = startDate;
            this.endDate = endDate;
            this.slippageCalculatorName = slippageCalculatorName;
            this.runInNativeCurrency = runInNativeCurrency;
            slippageCalculator = calculator(slippageCalculatorName);
            symbols_ = new Lazy<List<Symbol>>(() => list(convert(markets, name => symbol(name))));
        }

        static Type calculator(string name) {
            return hasContent(name) ? Bomb.ifNull(Type.GetType(name), () => "no such type " + name) : null;
        }

        Symbol symbol(string name) {
            var result = new Symbol(new Market(name));
            if(slippageCalculator != null) result.overrideSlippageCalculator(slippageCalculator);
            return result;
        }

        public IEnumerable<Symbol> symbols() {
            return symbols_.initializedValue();
        }

        public Simulator.Simulator newSimulator(Action<Collectible, PlotDefinition> addPlot) {
            var args = new SystemArguments(symbols(), parameters) {runInNativeCurrency = runInNativeCurrency};
            args.newPlotRequest += addPlot;
            return new Simulator.Simulator(args, loader(args.interval()), "UNUSEDHOPEFULLY");
        }

        BarLoader loader(Interval interval) {
            if (!startDate.HasValue) return new SystemDbBarLoader(interval, symbols());
            return endDate.HasValue 
                ? new SystemDbBarLoader(interval, symbols(), startDate.Value, endDate.Value) 
                : new SystemDbBarLoader(interval, symbols(), startDate.Value);
        }

        public void save(string name) {
            file(name).overwrite(serialize(this));
        }

        static QFile file(string name) {
            return new QFile(@"C:\logs\NORAD." + name.Trim() + ".settings.bin");
        }

        public static void load(ResearchGUI gui, bool isStarting) {
            if (isEmpty(gui.name())) {
                if (!isStarting) gui.alertUser("Fill in the Name box to continue."); 
                return;
            }
            var settings = file(gui.name());
            if (!settings.exists())  { gui.alertUser("File missing:" + settings.path()); return; }
            LogC.verbose(() => "loading gui from file " + settings.path());
            var serialized = settings.bytes();
            var runInfo = (SystemRunInfo) deserialize(serialized);
            runInfo.loadOnto(gui);
        }

        public void loadOnto(ResearchGUI gui) {
            LogC.info("loading " + this + " onto gui");
            if(parameters.has("systemId"))
                gui.setSystemId(parameters.get<string>("systemId"));
            if(parameters.has("RunNumber")) {
                gui.setRunNumberEnabled(true);
                gui.setRunNumber(parameters.get<string>("RunNumber"));
            }
            gui.setMarkets(markets);
            gui.setParameters(parameters);
            gui.setStartDate(startDate);
            gui.setEndDate(endDate);
            gui.setSlippageCalculator(slippageCalculatorName);
            gui.setRunInNativeCurrency(runInNativeCurrency);
        }
    }
}