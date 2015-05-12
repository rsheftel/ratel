using System;
using System.Collections.Generic;
using System.Data;
using NUnit.Framework;
using Q.Messaging;
using Q.Simulator;
using Q.Trading;
using Q.Util;
using systemdb.data;
using systemdb.metadata;
using util;
using Bar=Q.Trading.Bar;
using O=Q.Util.Objects;
using Symbol=Q.Trading.Symbol;

namespace Q.Recon {
    [TestFixture]
    public class TestStatusTracker : DbTestCase {

        FakeStatusTrackerGUI gui;
        static readonly Symbol SYMBOL = new Symbol("TY.1C");
        LiveSystem system;

        public override void setUp() {
            base.setUp();
            system = new LiveSystem(new Siv("TestSystem1", "daily", "1.0"), new Pv("Pv"));
            system.populateDetailsIfNeeded(false);
            system.addLiveMarket("TY.1C", "2008/08/01", null);
            gui = null;
        }

        [Test]
        public void testRestartMenuPopulation() {
            O.freezeNow("2008/08/08 11:00:00");
            startGui();
            IsEmpty(gui.launchers);
            LiveLauncher.publishHeartbeatFrom("jeffsmachine");
            awaitLaunchers("[jeffsmachine]");
            LiveLauncher.publishHeartbeatFrom("ericsmachine");
            awaitLaunchers("[ericsmachine, jeffsmachine]");
            LiveLauncher.publishHeartbeatFrom("jeffsmachine");
            LiveLauncher.publishHeartbeatFrom("ericsmachine");
            awaitLaunchers("[ericsmachine, jeffsmachine]");
        }

        void awaitLaunchers(string hosts) {
            waitMatches(hosts, () => O.toShortString(O.sort(gui.launchers.Keys)));
        }

        [Test]
        public void testHeartbeatStartsInCorrectStatus() {
            O.freezeNow("2008/08/08 11:00:00");
            startGui();
            AreEqual(SystemStatus.UNKNOWN, gui.heartbeatStatus(system));
            message("2008/08/08 10:59:30", "2008/08/08 10:00:00");
            waitForHeartbeatStatus(SystemStatus.RED);
            message("2008/08/08 10:59:50", "2008/08/08 10:00:00");
            waitForHeartbeatStatus(SystemStatus.YELLOW);
            
            message("2008/08/08 11:00:00", "2008/08/08 10:00:00");
            waitForHeartbeatStatus(SystemStatus.GREEN);
        }

        [Test]
        public void testHeartBeat() {
            StatusTracker.LATE_MILLIS = 500;
            StatusTracker.CRASHED_MILLIS = 500;
            O.freezeNow("2008/08/08 11:00:00");
            startGui();
            message("2008/08/08 11:00:00", "2008/08/08 10:00:00");
            waitForHeartbeatStatus(SystemStatus.GREEN);
            O.advanceNow(500);
            waitForHeartbeatStatus(SystemStatus.YELLOW);
            O.advanceNow(500);
            waitForHeartbeatStatus(SystemStatus.RED);
        }

        StatusTracker startGui() {
            gui = new FakeStatusTrackerGUI(O.list(system));
            var tracker = gui.tracker;
            tracker.initialize();
            gui.doAllWork();
            IsNotNull(gui.table);
            HasCount(1, gui.table.Rows);
            return tracker;
        }

        [Test]
        public void testTick() {
            O.freezeNow("2008/08/08 11:00:00");
            StatusTracker.LATE_MILLIS = 500;
            StatusTracker.CRASHED_MILLIS = 500;
            var tracker = startGui();
            AreEqual(SystemStatus.UNKNOWN, gui.tickStatus(system));
            message("2008/08/08 11:00:00", "2008/08/08 10:00:00");
            waitForTickStatus(SystemStatus.GREEN);
            O.advanceNow(1000);

            Log.setFile(@"C:\foo.log");
            SYMBOL.publish(new Bar(0,0,0,0, date("2008/08/08 11:00:01")));
            O.wait(() => tracker.tickTimers[system].running());
            O.advanceNow(500);
            waitForTickStatus(SystemStatus.YELLOW);
            O.advanceNow(500);
            waitForTickStatus(SystemStatus.RED);
            O.advanceNow(59000);
            message("2008/08/08 11:01:00", "2008/08/08 11:00:01");
            waitForTickStatus(SystemStatus.GREEN);
            // lastTickProcessed should be the time on the tick
        }

        [Test]
        public void testIntermingledTicks() {
            O.freezeNow("2008/08/08 11:00:00");
            StatusTracker.LATE_MILLIS = 1000;
            StatusTracker.CRASHED_MILLIS = 1000;
            var tracker = startGui();
            message("2008/08/08 11:01:00", "2008/08/08 10:00:00");
            waitForTickStatus(SystemStatus.GREEN);
            SYMBOL.publish(new Bar(0,0,0,0, date("2008/08/08 11:00:00")));
            O.wait(() => tracker.tickTimers[system].running());
            O.advanceNow(1000);
            waitForTickStatus(SystemStatus.YELLOW);
            SYMBOL.publish(new Bar(0,0,0,0, date("2008/08/08 11:00:01")));
            O.wait(() => tracker.plans[system].Count == 2);
            O.advanceNow(1000);
            waitForTickStatus(SystemStatus.RED);
            message("2008/08/08 11:01:00", "2008/08/08 11:00:00");
            waitForTickStatus(SystemStatus.YELLOW);
            O.advanceNow(1000);
            waitForTickStatus(SystemStatus.RED);
            SYMBOL.publish(new Bar(0,0,0,0, date("2008/08/08 11:00:03")));
            message("2008/08/08 11:01:00", "2008/08/08 11:00:03");
            waitForTickStatus(SystemStatus.GREEN);
        }

        [Test]
        public void testCanPublishRestart() {
            LogC.useJavaLog = true;
            FerretControl.onOutgoing(fields => O.queueWorkItem(()=> FerretControl.setStatus(fields.text("FERRETSTATE"))));
            system.populateDetailsIfNeeded(false);
            system.populateTagIfNeeded("QF.Example", false);
            FerretControl.setStatus("Ticket");
            var tracker = startGui();
            var restartRequests = LiveLauncher.restartTopic();
            Fields received = null;
            restartRequests.subscribe(fields => { received = fields; });
            tracker.restart(O.hostname(), system.id());
            O.wait(() => received != null);
            AreEqual(system.id(), (int) received.longg("SystemId"));
            AreEqual(O.hostname(), received.text("Hostname"));
            gui.noMessage();
            
            system.setAutoExecuteTrades(true);
            gui.stageAnswer(YesNoCancel.NO);
            restartRequests.setReadonly(true);
            tracker.restart(O.hostname(), system.id());
            gui.hasMessage("This will put Ferret into Stage mode.  Are you sure you want to do this?");

            gui.stageAnswer(YesNoCancel.YES);
            restartRequests.setReadonly(false);
            received = null;
            tracker.restart(O.hostname(), system.id());
            gui.hasMessage("This will put Ferret into Stage mode.  Are you sure you want to do this?");
            waitMatches("Stage", FerretControl.status);
            O.wait(() => received != null);
            
            received = null;
            tracker.restart(O.hostname(), system.id());
            gui.noMessage();
            O.wait(() => received != null);

        }

        [Test]
        public void testCrashedDoesNotRevertToGreenOnTickPublish() {
            O.freezeNow("2008/08/08 11:00:00");
            StatusTracker.LATE_MILLIS = 1000;
            StatusTracker.CRASHED_MILLIS = 1000;
            startGui();
            message("2008/08/08 11:01:00", "2008/08/08 11:01:00");
            waitForTickStatus(SystemStatus.GREEN);
            tickPublishedAt("2008/08/08 11:02:00");
            O.sleep(100);
            O.freezeNow("2008/08/08 11:05:00");
            waitForTickStatus(SystemStatus.RED);
            tickPublishedAt("2008/08/08 11:05:01");
            O.sleep(100);
            O.freezeNow("2008/08/08 11:05:01");
            IsTrue(tickStatusMatches(SystemStatus.RED));
        }

        static void tickPublishedAt(string time) {
            SYMBOL.publish(new Bar(0,0,0,0, date(time)));
        }

        [Test]
        public void testProcessBeforeReceivingPublish() {
            O.freezeNow("2008/08/08 11:00:00");
            StatusTracker.LATE_MILLIS = 1000;
            StatusTracker.CRASHED_MILLIS = 1000;
            var tracker = startGui();
            message("2008/08/08 11:00:00", "2008/08/08 11:00:03");
            waitForTickStatus(SystemStatus.GREEN);
            SYMBOL.publish(new Bar(0,0,0,0, date("2008/08/08 11:00:03")));
            O.sleep(100);
            IsFalse(tracker.tickTimers[system].running());
            
        }

        void waitForHeartbeatStatus(SystemStatus status) {
            waitMatches(status, () => { 
                                    gui.doAllWork();
                                    return gui.heartbeatStatus(system); 
                                });
        }

        void waitForTickStatus(SystemStatus status) {
            O.wait(100, 50, () => tickStatusMatches(status));
        }

        bool tickStatusMatches(SystemStatus expected) {
            gui.doAllWork();
            return expected == gui.tickStatus(system); 
        }

        void message(string heartbeatTime, string tickProcessedTime) {
            var topic = new Topic(system.topicName(OrderTable.prefix, SystemHeartbeat.SUFFIX));
            topic.send(new Dictionary<string, object> {
                {"hostname", "somewhere"},
                {"ticksReceived", 1},
                {"lastTickProcessed", tickProcessedTime},
                {"timestamp", heartbeatTime}
            });
        }
    }

    class FakeStatusTrackerGUI : FakeGUI, StatusTrackerGUI {
        internal DataTable table;
        readonly Dictionary<LiveSystem, SystemStatus> beatStatuses = new Dictionary<LiveSystem, SystemStatus>();
        readonly Dictionary<LiveSystem, SystemStatus> tickStatuses = new Dictionary<LiveSystem, SystemStatus>();
        internal readonly StatusTracker tracker;
        internal readonly Dictionary<string, DateTime> launchers = new Dictionary<string, DateTime>();

        public FakeStatusTrackerGUI(List<LiveSystem> systems) {
            tracker = new StatusTracker(this, systems);
        }

        public void setStatusTable(DataTable newTable) {
            table = newTable;
        }

        public void setHeartbeatStatus(DataRow row, SystemStatus status) {
            beatStatuses[tracker.liveSystem(row)] = status;
        }

        public void setTickStatus(DataRow row, SystemStatus status) {
            tickStatuses[tracker.liveSystem(row)] = status;
        }

        public void launcherAvailable(string host, DateTime staleAt) {
            if (!launchers.ContainsKey(host)) launchers.Add(host, staleAt);
            launchers[host] = staleAt;
        }

        public SystemStatus heartbeatStatus(LiveSystem system) {
            return Bomb.missing(beatStatuses, system);
        }

        public SystemStatus tickStatus(LiveSystem system) {
            return Bomb.missing(tickStatuses, system);
        }
    }

    public enum SystemStatus {
        UNKNOWN, GREEN, RED, YELLOW
    }
}