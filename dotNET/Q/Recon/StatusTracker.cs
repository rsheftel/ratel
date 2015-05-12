using System;
using System.Collections.Generic;
using System.Data;
using java.util;
using Q.Messaging;
using Q.Simulator;
using Q.Trading;
using Q.Util;
using systemdb.data;
using JMarket = systemdb.metadata.Market;
using systemdb.metadata;
using Symbol=Q.Trading.Symbol;

namespace Q.Recon {
    public class StatusTracker : Objects {
        readonly StatusTrackerGUI gui;
        readonly DataTable table = empty();
        readonly Dictionary<DataRow, LiveSystem> systems = new Dictionary<DataRow, LiveSystem>();
        readonly Dictionary<string, int> columns = new Dictionary<string, int>();
        readonly Dictionary<LiveSystem, SystemStatus> beatStatuses = new Dictionary<LiveSystem, SystemStatus>();
        readonly Dictionary<LiveSystem, SystemStatus> tickStatuses = new Dictionary<LiveSystem, SystemStatus>();
        internal class StatusSequences : Dictionary<LiveSystem, TimerSequence<SystemStatus>>{ }
        internal readonly StatusSequences tickTimers = new StatusSequences();
        readonly StatusSequences heartbeatTimers = new StatusSequences();
        readonly List<LiveSystem> liveSystems;
        public static int LATE_MILLIS = 10 * 1000;
        public static int CRASHED_MILLIS = 20 * 1000;
        internal readonly Dictionary<LiveSystem, List<Plan>> plans = new Dictionary<LiveSystem, List<Plan>>();
        readonly Dictionary<LiveSystem, DateTime> lastTickProcessed = new Dictionary<LiveSystem, DateTime>();

        public StatusTracker(StatusTrackerGUI gui, List<LiveSystem> systems) {
            this.gui = gui;
            liveSystems = systems;
            foreach(DataColumn column in table.Columns)
                columns[column.ColumnName] = column.Ordinal;
            gui.setStatusTable(table);
        }

        static DataTable empty() {
            var table = new DataTable();
            table.Columns.Add("system", typeof (string));
            table.Columns.Add("pv", typeof (string));
            table.Columns.Add("id", typeof (int));
            table.Columns.Add("hostname", typeof (string));
            table.Columns.Add("ticks", typeof (int));
            table.Columns.Add("lastTick", typeof (string));
            table.Columns.Add("lastBeat", typeof (string));
            return table;
        }

        void addRow(LiveSystem system) {
            var row = table.NewRow();
            systems.Add(row, system);
            insertUnknownRow(row, system);
            plans[system] = new List<Plan>();
            
            setHeartbeatStatus(row, system, SystemStatus.UNKNOWN);
            setTickStatus(row, system, SystemStatus.UNKNOWN);

            tickTimers[system] = statusFades(status => setTickStatus(row, system, status));
            heartbeatTimers[system] = statusFades(status => setHeartbeatStatus(row, system, status));
            
            lastTickProcessed[system] = DateTime.MinValue;
            
            subscribeSystemHeartbeat(row, system);
            var symbols = convert(list<JMarket>(system.markets()), m => new Symbol(m));
            each(symbols, symbol => {
                try {
                    symbol.subscribe(bar => onTickPublished(row, system, bar.time));
                } catch(Exception ex) {
                    LogC.err("exception caught subscribing to tick data for " + symbol + ", " + system, ex);
                    gui.alertUser("exception caught susbcribing to data for " + symbol + ", " + system + ".\nSkipping... see log for details.");
                }
            });
        }

        static TimerSequence<SystemStatus> statusFades(Action<SystemStatus> action) {
            var sequence = new TimerSequence<SystemStatus>(action);
            sequence.add(SystemStatus.GREEN, LATE_MILLIS);
            sequence.add(SystemStatus.YELLOW, CRASHED_MILLIS);
            sequence.add(SystemStatus.RED);
            return sequence;
        }

        void onTickPublished(DataRow row, LiveSystem system, DateTime time) {
            if(time.CompareTo(lastTickProcessed[system]) <= 0) return;
            lock(plans[system]) {
                if (isEmpty(plans[system]) || time > last(plans[system]).tickPublished) 
                    plans[system].Add(new Plan(now(), time));
                if(plans[system].Count == 1 && !row["lastBeat"].Equals("unknown"))
                    tickTimers[system].startAsOf(now());
            }
        }

        void subscribeSystemHeartbeat(DataRow row, LiveSystem system) {
            var topic = new Topic(system.topicName(OrderTable.prefix, SystemHeartbeat.SUFFIX));
            topic.subscribe(fields => gui.runOnGuiThread(() => {
                                                             row["hostname"] = fields.get("hostname");
                                                             row["ticks"] = fields.longg("ticksReceived");
                                                             var lastTicked = ymdHuman(fields.time("lastTickProcessed"));
                                                             row["lastTick"] = lastTicked.Substring(0, 4).Equals("0001") ? "no tick" : lastTicked;
                                                             row["lastBeat"] = ymdHuman(fields.time("timestamp"));
                                                             onHeartbeat(system, fields.time("timestamp"));
                                                             onTickProcessed(row, system, fields.time("lastTickProcessed"));
                                                         }));
        }

        void insertUnknownRow(DataRow row, LiveSystem system) {
            row.ItemArray = new object[] {
                system.siv().sivName("-"),
                system.pv().name(),
                system.id(),
                "unknown",
                -1,
                "unknown", // "2008/10/09 15:55:07",
                "unknown", // "2008/10/09 15:55:07",
            };
            table.Rows.InsertAt(row, 0);
        }

        void setHeartbeatStatus(DataRow row, LiveSystem system, SystemStatus status) {
            beatStatuses[system] = status;
            gui.setHeartbeatStatus(row, beatStatuses[system]);
        }

        void onHeartbeat(LiveSystem system, Date jDate) {
            heartbeatTimers[system].startAsOf(date(jDate));
        }

        void onTickProcessed(DataRow row, LiveSystem system, Date jLastProcessed) {
            lock(plans[system]) {
                var time = date(jLastProcessed);
                if(lastTickProcessed[system].CompareTo(time) < 0) lastTickProcessed[system] = time;
                for (var i = 0; i < plans[system].Count; i++) {
                    if (first(plans[system]).tickPublished > time) break;
                    plans[system].RemoveAt(0);
                }
                if (isEmpty(plans[system])) {
                    tickTimers[system].stop();
                    setTickStatus(row, system, SystemStatus.GREEN);
                    return;
                }
                var nextPlanTime = first(plans[system]).tickTime;
                tickTimers[system].startAsOf(nextPlanTime);
            }
        }

        void setTickStatus(DataRow row, LiveSystem system, SystemStatus status) {
            tickStatuses[system] = status;
            gui.setTickStatus(row, tickStatuses[system]);
        }

        public void initialize() {
            addAllSystemRows();
            subscribeLauncherHeartbeat();
        }

        void subscribeLauncherHeartbeat() {
            LiveLauncher.subscribeHeartbeat((host, staleAt) => gui.launcherAvailable(host, staleAt));
        }

        void addAllSystemRows() {
            each(sort(liveSystems, (a, b) => b.name().CompareTo(a.name())), addRow);
        }

        public static List<LiveSystem> allLiveSystems() {
            return list<LiveSystem>(MsivLiveHistory.LIVE.liveSystems());
        }

        public int index(string column) {
            return Bomb.missing(columns, column);
        }

        public LiveSystem liveSystem(DataRow row) {
            return systems[row];
        }

        public SystemStatus status(DataRow row) {
            return beatStatuses[systems[row]];
        }

        internal class Plan {
            public readonly DateTime tickTime;
            public readonly DateTime tickPublished;

            public Plan(DateTime tickTime, DateTime tickPublished) {
                this.tickTime = tickTime;
                this.tickPublished = tickPublished;
            }

            public override string ToString() {
                return tickTime + ", " + tickPublished;
            }
        }

        public void restart(string hostname, int id) {
            if (LiveSystem.isAutoExecute(id) && !FerretControl.status().Equals("Stage")) {
                var answer = gui.askUser("This will put Ferret into Stage mode.  Are you sure you want to do this?");
                if (answer != YesNoCancel.YES) return;
                FerretControl.requestFerretChange("Stage");
                wait(()=> FerretControl.status().Equals("Stage"));
            }
            var fields = new Fields();
            fields.put("SystemId", id);
            fields.put("Hostname", hostname);
            fields.put("Timestamp", ymdHuman(now()));
            LiveLauncher.restartTopic().send(fields);
        }

        public static void kill(int id) {
            LiveLauncher.killTopic().send("SystemId", id + "");
        }
    }
}