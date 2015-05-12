using System;
using System.Collections.Generic;
using System.Data;
using amazon.monitor;
using java.util;
using Q.Messaging;
using Q.Recon;
using Q.Util;
using systemdb.data;
using util;

namespace Q.Amazon {
    public interface CloudSTOTrackerGUI: QGUI {
        void setInstanceTable(DataTable table);
        int systemId();
        void setStatus(DataRow row, SystemStatus status);
        void setTotals(int red, int green);
        void setInstanceCount(int numInstances);
        void setSummary(long complete, long total, double rpm, DateTime completionTime);
    }

    public class CloudSTOTracker : Util.Objects {
        public const string RUNS_PER_MIN = "runsPerMin";
        public const string INSTANCE_ID = "instanceId";
        public const string NUM_RED = "numRed";
        public const string NUM_GREEN = "numGreen";
        public const string COMPLETED = "completed";
        public const string LAST_COMPLETED = "lastCompleted";
        readonly CloudSTOTrackerGUI gui;
        public readonly DataTable table;
        readonly Dictionary<DataRow, string> instances = new Dictionary<DataRow, string>();
        readonly Dictionary<DataRow, Fields> lastMessage = new Dictionary<DataRow, Fields>();
        readonly Timers<DataRow> timers = new Timers<DataRow>();
        Topic systemTopic;

        public CloudSTOTracker(CloudSTOTrackerGUI gui) {
            this.gui = gui;
            table = new DataTable();
            table.Columns.Add(INSTANCE_ID);
            table.Columns.Add(NUM_RED);
            table.Columns.Add(NUM_GREEN);
            table.Columns.Add(COMPLETED);
            table.Columns.Add(RUNS_PER_MIN);
            table.Columns.Add(LAST_COMPLETED);
            gui.setInstanceTable(table);
        }

        public void initialize() {
            
        }

        public void systemIdUpdated() {
            gui.runOnGuiThread(populate);
        }

        void populate() {
            var systemId = gui.systemId();
            clear();
            systemTopic = new Topic(CloudMonitor.topic(systemId));
            systemTopic.subscribe(fields => populate(systemId, fields));
            new Topic(CloudMonitor.progressTopic(systemId)).subscribe(fields => updateSummary(systemId, fields));
        }

        void updateSummary(int systemId, Fields fields) {
            if(systemId != gui.systemId()) return;
            var complete = fields.longg("RunsComplete");
            var total = fields.longg("TotalRuns");
            var rpm = fields.numeric("RunsPerMinute");
            var completionTime = rpm == 0 || total == complete ? SQL_MIN_DATE : now().AddMinutes((total - complete) / rpm);
            gui.runOnGuiThread(() => 
                gui.setSummary(complete, total, rpm, completionTime)
            );
        }

        void populate(int systemId, Map fields) {
            if(systemId != gui.systemId()) return;
            var commaSep = (string) fields.get("Instances");
            var alive = list(convert(split(",", commaSep), toTrim => toTrim.Trim()));
            each(instances, (row, id) => { if (!alive.Contains(id)) gui.runOnGuiThread(() => deleteRow(row)); });
            each(alive, id => { if (!instances.ContainsValue(id)) gui.runOnGuiThread(() => addRow(id)); });
            
        }

        void deleteRow(DataRow row) {
            table.Rows.Remove(row);
            instances.Remove(row);
            gui.setInstanceCount(table.Rows.Count);
        }

        void addRow(string id) {
            var row = table.NewRow();
            row[INSTANCE_ID] = id;
            var topic = CloudMonitor.instanceTopic(id);
            new Topic(topic).subscribe(fields => gui.runOnGuiThread(() => updateRow(row, fields)));
            table.Rows.Add(row);
            instances[row] = id;
            gui.setInstanceCount(table.Rows.Count);
        }

        void updateRow(DataRow row, Fields fields) {
            lastMessage[row] = fields;
            var numRed = fields.longg("NumRed");
            var numGreen = fields.longg("NumGreen");
            row[NUM_RED] = numRed;
            row[NUM_GREEN] = numGreen;
            row[COMPLETED] = fields.longg("Completed");
            row[RUNS_PER_MIN] = Strings.nDecimals(2, fields.numeric("RunsPerMinute"));
            row[LAST_COMPLETED] = fields.text("LastCompleted");
            var redTime = date(fields.time("RedTime"));
            var current = status(row);
            if (current.Equals(SystemStatus.GREEN)) 
                timers.replace(row, redTime, () => gui.runOnGuiThread(() => gui.setStatus(row, SystemStatus.YELLOW)));
            gui.setStatus(row, current);
            var red = 0;
            var green = 0;
            foreach (DataRow r in table.Rows) {
                if(r.IsNull(NUM_RED)) continue;
                red += int.Parse((string) r[NUM_RED]);
                green += int.Parse((string) r[NUM_GREEN]);
            }
            gui.setTotals(red, green);

        }

        public string instance(DataRow row) {
            return instances[row];
        }

        public SystemStatus status(DataRow row) {
            if(!lastMessage.ContainsKey(row)) return SystemStatus.UNKNOWN;
            var redTime = date(lastMessage[row].time("RedTime"));
            var numRed = lastMessage[row].longg("NumRed");
            if (numRed == 0 && redTime.CompareTo(now()) > 0) return SystemStatus.GREEN;
            return numRed == 0 ? SystemStatus.YELLOW : SystemStatus.RED;
        }

        public void clear() {
            table.Rows.Clear();
            instances.Clear();
            gui.setTotals(0, 0);
            gui.setInstanceCount(0);
            gui.setSummary(0, 0, 0, SQL_MIN_DATE);
        }

        public void kill(string instanceId) {
            var instancesCopy = list<string>(instances.Values);
            instancesCopy.Remove(instanceId);
            systemTopic.send("Instances", join(",", instancesCopy));
        }
    }
}
