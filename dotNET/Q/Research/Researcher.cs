using System;
using System.Collections.Generic;
using System.Threading;
using Q.Trading;
using Q.Trading.Results;
using Q.Util;
using systemdb.metadata;
using util;
using Market=systemdb.metadata.Market;
using Objects=Q.Util.Objects;

namespace Q.Research {
    public class Researcher : Objects {
        readonly ResearchGUI gui;
        Thread runThread;
        public Simulator.Simulator simulator;
        public SystemRunInfo runInfo;
        public readonly LazyDictionary<Collectible, List<PlotDefinition>> plots = new LazyDictionary<Collectible, List<PlotDefinition>>(s => new List<PlotDefinition>());
        public readonly List<Position> positions = new List<Position>();
        readonly LazyDictionary<Position, PnlContainer> positionsInfo = new LazyDictionary<Position, PnlContainer>(position => new PnlContainer());
        public readonly List<Trade> trades = new List<Trade>();


        public Researcher(ResearchGUI gui) {
            this.gui = gui;
        }

        T nullIfFails<T>(Producer<T> t, Producer<string> makeMessage) where T : class{
            try { return t(); }
            catch(Exception e) {
                var message = LogC.errMessage(makeMessage() , e);
                gui.alertUser(message);
                LogC.err(message);
                return null;
            }
        }

        public void run(bool goLive) {
            Bomb.unlessNull(runThread, () => "cannot run a system while another one is running");
            var markets = gui.markets();
            var parameters = nullIfFails(() => gui.parameters(), () => "Parameters contains an invalid value!");
            if(parameters == null) return;
            if (hasContent(gui.systemId()) && !gui.systemId().Equals(parameters.get<string>("systemId"))) {
                gui.alertUser("The system id in the System ID box and systemId in parameters do not match. did you forget to hit Load System?\nIf this was intentional, clear the Id box and run again.");
                return;
            }

            var startDate = gui.startDate();
            var endDate = gui.endDate();
            var slippageCalculator = gui.slippageCalculator();
            var runInNativeCurrency = gui.runInNativeCurrency();
            runThread = new Thread(() => {
                Log.setContext("runSystem");
                runSystem(() => new SystemRunInfo(markets, parameters, startDate, endDate, slippageCalculator, runInNativeCurrency), goLive);
            });
            runThread.Start();
        }

        void runSystem(Producer<SystemRunInfo> info, bool goLive) {
            try {
                gui.disableRunButton();
                runInfo = info();
                simulator = runInfo.newSimulator((symbol, definition) => plots.get(symbol).Add(definition));
                each(simulator.symbols, symbol => simulator.addCollectible(symbol));
                simulator.addNewTradeListener(addTrade);
                simulator.processBars();
            } catch (Exception e) {
                gui.logAndAlert("failed during simulation run", e);
                return;
            } finally {
                runThread = null;
                gui.enableRunButton();
            }
            gui.reportResults(this);
            if(goLive) simulator.goLive();
        }

        void addTrade(Position position, Trade trade) {
            if(position.isEntry(trade)) {
                positions.Add(position);
                positionsInfo.add(position);
                Action<Bar> onNewBar = bar => positionsInfo.get(position).add(simulator.pnlForPosition(position, 0.0));
                simulator.addNewBarListener(position.symbol, onNewBar);
                position.onPositionClosed += () => {
                    simulator.removeNewBarListener(position.symbol, onNewBar);
                    positionsInfo.get(position).add(position.pnl(true, simulator.runInNativeCurrency()));
                };
            }
            trades.Add(trade);
        }

        public bool runComplete() {
            return runThread == null;
        }

        public static void load(ResearchGUI gui, bool isStarting) {
            SystemRunInfo.load(gui, isStarting);
        }


        public static void save(ResearchGUI gui) {
            try { makeRunInfo(gui).save(gui.name()); } 
            catch (Exception e) {
                gui.logAndAlert("failed to save settings", e);
            }
        }

        static SystemRunInfo makeRunInfo(ResearchGUI gui) {
            return new SystemRunInfo(gui.markets(), gui.parameters(), gui.startDate(), gui.endDate(), gui.slippageCalculator(), gui.runInNativeCurrency());
        }

        public static void loadSystem(ResearchGUI gui) {
            try {
                var systemId = int.Parse(gui.systemId());
                var details = Parameters.details(systemId);
                Parameters parameters;
                if (details.hasPv()) {
                    parameters = new Parameters {
                        {"systemId", systemId},
                        {"RunMode", (double) RunMode.LIVE}
                    };
                    gui.setMarkets(convert(list<Market>(details.liveSystem().markets()), market => market.name()));
                } else if(hasContent(gui.runNumber())) {
                    parameters = new Parameters {
                        {"systemId", systemId},
                        {"RunNumber", int.Parse(gui.runNumber())},
                        {"RunMode", (double) RunMode.STO}
                    };
                    gui.setMarkets(list<string>(MsivBacktestTable.BACKTEST.markets(details.siv(), details.stoId())));
                } else {
                    parameters = new Parameters {
                        {"systemId", systemId},
                        {"RunMode", (double) RunMode.RIGHTEDGE}
                    };
                    each(list<string>(StrategyParameters.NAMES.names(details.siv().system())), name => parameters.Add(name, 0));
                    gui.setMarkets(list<string>(MsivBacktestTable.BACKTEST.markets(details.siv(), details.stoId())));
                }
                gui.setRunNumberEnabled(details.hasValidStoDir());
                gui.setRunInNativeCurrency(details.runInNativeCurrency());
                gui.setParameters(parameters);
            } catch (Exception e) {
                gui.logAndAlert("failed to load system", e);
            }
        }

        public PnlContainer positionInfo(Position position) {
            Bomb.unless(positionsInfo.has(position), () => "can't ask for positionInfo on unadded position!");
            return positionsInfo.get(position);
        }
    }

    public class PnlContainer : Objects {
        readonly List<double> pnl_ = new List<double>();
        double lastEquity;

        public void add(double currentEquity) {
            pnl_.Add(currentEquity - lastEquity);
            lastEquity = currentEquity;
        }

        public double pnl(int index) {
            return index < count() ? pnl_[index] : 0.0;
        }

        public double pnlFrom(int index) {
            return index < count() ? sum(pnl_.GetRange(index, pnl_.Count - index)) : 0.0;
        }

        public double pnlTo(int index) {
            return sum(pnl_.GetRange(0, Math.Min(count(), index + 1)));
        }

        public int count() {
            return pnl_.Count;
        }
    }
}