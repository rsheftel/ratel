using System;
using System.Collections.Generic;
using System.Data;
using System.Threading;
using amazon.monitor;
using NUnit.Framework;
using Q.Messaging;
using Q.Recon;
using Q.Util;
using O=Q.Util.Objects;

namespace Q.Amazon {
    [TestFixture]
    public class TestCloudSTOTracker : DbTestCase {
        [Test]
        public void testSomething() {
            var gui = startGui();
            var systemId = 1234;
            var topic = new Topic(CloudMonitor.topic(systemId));
            var instance = "i-123456";
            gui.setSystemId(systemId);
            gui.doAllWork();
            topic.send("Instances", instance);
            gui.noMessage();
            hasRedGreen(0, 0, gui);
            requireCount(gui, 1);
            hasField(gui, 0, CloudSTOTracker.INSTANCE_ID, instance);
            var redTime = O.now().Add(new TimeSpan(0, 0, 0, 2));
            publish(instance, redTime, 0, 3, 10, 2.5, date("2009/02/02 10:00:01"));
            hasField(gui, 0, CloudSTOTracker.NUM_GREEN, "3");
            hasField(gui, 0, CloudSTOTracker.NUM_RED, "0");
            hasField(gui, 0, CloudSTOTracker.COMPLETED, "10");
            hasField(gui, 0, CloudSTOTracker.RUNS_PER_MIN, "2.50");
            hasField(gui, 0, CloudSTOTracker.LAST_COMPLETED, "2009/02/02 10:00:01");
            hasRedGreen(0, 3, gui);
            AreEqual(SystemStatus.GREEN, gui.status(instance));
            O.wait(() => {gui.doAllWork(); return gui.status(instance).Equals(SystemStatus.YELLOW); });
            hasRedGreen(0, 3, gui);
            publish(instance, O.SQL_MAX_DATE, 1, 2, 15, 5.5, date("2009/02/02 10:00:02"));
            O.wait(() => {gui.doAllWork(); return gui.status(instance).Equals(SystemStatus.RED); });
            hasRedGreen(1, 2, gui);
        }

        [Test]
        public void testSummary() {
            var gui = startGui();
            var systemId = 1234;
            var topic = new Topic(CloudMonitor.progressTopic(systemId));
            gui.setSystemId(systemId);
            gui.doAllWork();
            O.freezeNow("2008/02/03 02:00:00");
            topic.send(new Dictionary<string, object> {
                {"RunsComplete", 1000},
                {"TotalRuns", 10000},
                {"RunsPerMinute", 10},
            });
            O.wait(() => gui.runsComplete() == 1000);
            AreEqual(date("2008/02/03 17:00:00"), gui.completionTime());
            AreEqual(10000, gui.totalRuns());
        }

        static void hasField(FakeCloudSTOTrackerGUI gui, int rowIndex, string field, string value) {
            try {
                O.wait(() => {
                    gui.doAllWork(); 
                    return gui.tracker.table.Rows[rowIndex][field].Equals(value);
                });
            } catch (Exception e) {
                throw Bomb.toss("field " + field + " never matched " + value + " on row " + rowIndex + ", was " + gui.tracker.table.Rows[rowIndex][field], e);
            }
        }

        [Test]
        public void testAddRemoveInstances() {
            var gui = startGui();
            var systemId = 1234;
            var topic = new Topic(CloudMonitor.topic(systemId));
            new Thread(o => { O.sleep(1000); topic.send("Instances", "i-123456"); }).Start();
            gui.setSystemId(systemId);
            gui.doAllWork();
            gui.noMessage();
            requireCount(gui, 1);
            topic.send("Instances", "i-123456,i-98765");
            requireCount(gui, 2);
            gui.tracker.kill("i-123456");
            requireCount(gui, 1);
        }

        static void requireCount(FakeCloudSTOTrackerGUI gui, int count) {
            Objects.wait(() => { gui.doAllWork(); return gui.tracker.table.Rows.Count == count; });
        }

        static void hasRedGreen(int expectedRed, int expectedGreen, FakeCloudSTOTrackerGUI gui) {
            AreEqual(expectedRed, gui.totalRed());
            AreEqual(expectedGreen, gui.totalGreen());
        }

        static void publish(string instance, DateTime redTime, int numRed, int numGreen, int total, double runsPerMinute, DateTime lastCompletion) {
            new Topic(CloudMonitor.instanceTopic(instance)).send(new Dictionary<string, object> {
                {"NumRed", numRed}, 
                {"NumGreen", numGreen}, 
                {"Completed", total}, 
                {"RunsPerMinute", runsPerMinute}, 
                {"LastCompleted", O.ymdHuman(lastCompletion)}, 
                {"RedTime", O.ymdHuman(redTime)}
            });
        }

        [Test]
        public void testSystemIdNotPublishing() {
            var gui = startGui();
            var systemId = 1234;
            gui.setSystemId(systemId);
            gui.doAllWork();
            gui.noMessage();
        }

        static FakeCloudSTOTrackerGUI startGui() {
            var gui = new FakeCloudSTOTrackerGUI();
            gui.doAllWork();
            return gui;
        }
    }

    internal class FakeCloudSTOTrackerGUI : FakeGUI, CloudSTOTrackerGUI {
        internal readonly CloudSTOTracker tracker;
        int systemId_;
        readonly Dictionary<string, SystemStatus> statuses = new Dictionary<string, SystemStatus>();
        int totalRed_;
        int totalGreen_;
        long runsComplete_;
        DateTime completionTime_;
        long totalRuns_;

        public FakeCloudSTOTrackerGUI() {
            tracker = new CloudSTOTracker(this);
        }

        public void setInstanceTable(DataTable newTable) {}

        public int systemId() {
            return systemId_;
        }

        public void setStatus(DataRow row, SystemStatus status) {
            statuses[tracker.instance(row)] = status;
        }

        public void setTotals(int red, int green) {
            totalRed_ = red;
            totalGreen_ = green;
        }

        public void setInstanceCount(int numInstances) {
        }

        public void setSummary(long complete, long total, double rpm, DateTime completionTime) {
            runsComplete_ = complete;
            totalRuns_ = total;
            completionTime_ = completionTime;
        }

        public void setSystemId(int id) {
            systemId_ = id;
            tracker.systemIdUpdated();
        }

        public object status(string instance) {
            return statuses[instance];
        }

        public int totalRed() {
            return totalRed_;
        }

        public int totalGreen() {
            return totalGreen_;
        }

        public long runsComplete() {
            doAllWork();
            return runsComplete_;
        }

        public DateTime completionTime() {
            return completionTime_;
        }

        public long totalRuns() {
            return totalRuns_;
        }
    }
}
