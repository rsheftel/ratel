using System;
using System.Collections.Generic;
using System.Data;
using NUnit.Framework;
using Q.Util;

namespace Q.Recon {
    class FakeOrderTrackerGUI : FakeGUI, OrderTrackerGUI {
        public List<string> systems = new List<string>();
        public string selectedMarket = "ALL";
        internal readonly OrderTracker tracker;
        public DataTable gridContents;

        public List<string> marketChoices = new List<string>();
        public List<string> pvChoices = new List<string>();
        public List<string> prefixChoices = new List<string>();
        public string selectedSystem = "ALL";
        public string selectedPv = "ALL";
        public Dictionary<DataRow, OrderStatus> statii = new Dictionary<DataRow, OrderStatus>();
        public string selectedStatusFilter = "ALL";
        public List<DataRow> rowsWithMenus = new List<DataRow>();

        public FakeOrderTrackerGUI() {
            tracker = new OrderTracker(this);
        }

        public void setSystemChoices(IEnumerable<string> sivs) {
            systems.Clear();
            systems.AddRange(sivs);
            selectedSystem = "ALL";
        }

        public string market() {
            return selectedMarket;
        }

        public string filter() {
            return selectedStatusFilter;
        }

        public void setStatus(DataRow row, OrderStatus newStatus) {
            lock(statii)
                statii[row] = newStatus;
        }

        public void addContextMenu(DataRow row) {
            rowsWithMenus.Add(row);
        }

        public void removeContextMenu(DataRow row) {
            rowsWithMenus.Remove(row);
        }

        public bool sivSelected() {
            return !siv().Equals("ALL");
        }

        public string siv() {
            return selectedSystem;
        }

        public void setOrderTable(DataTable table) {
            gridContents = table;
        }

        public void setMarketChoices(List<string> markets) {
            marketChoices.Clear();
            marketChoices.AddRange(markets);
        }

        public void setPvChoices(List<string> pvs) {
            pvChoices = pvs;
        }

        public string pv() {
            return selectedPv;
        }

        public bool pvSelected() {
            return !pv().Equals("ALL");
        }

        public void setMarket(string market) {
            selectedMarket = market;
            tracker.marketsUpdated();
        }

        public int count() {
            return tracker.orderTable().count();
        }

        public void setStatusFilter(string s) {
            selectedStatusFilter = s;
            tracker.filterUpdated();
        }

        public void noMenu(DataRow row) {
            Assert.IsFalse(rowsWithMenus.Contains(row));
        }

        public void requireMenu(DataRow row) {
            Assert.IsTrue(hasMenu(row));
        }

        public bool hasMenu(DataRow row) {
            return rowsWithMenus.Contains(row);
        }

        public string ferretStatus(string ferretId) {
            var matching = accept(rows(), row => row["ferretOrderId"].Equals(ferretId));
            return (string) the(matching)["status"];
        }

        public List<DataRow> rows() {
            return list<DataRow>(gridContents.Rows);
        }
    }

    public class FakeGUI : Objects, QGUI {
        readonly List<Action> workQueue = new List<Action>();
        readonly List<string> messagesReceived = new List<string>();
        YesNoCancel stagedAnswer = YesNoCancel.YES;

        public void runOnGuiThread(Action action) {
            workQueue.Add(action);
        }

        public void logAndAlert(string s, Exception e) {
                var message = LogC.errMessage("failed during simulation run\n", e);
                LogC.err(message);
                alertUser("failed during simulation. examine " + LogC.errFile() + "\n" + message);
        }

        public void doAllWork() {
            var items = copy(workQueue);
            workQueue.Clear();
            each(items, work => work());
        }

        public void alertUser(string message) {
            messagesReceived.Add(message);
        }
        
        public YesNoCancel askUser(string message) {
            alertUser(message);
            return stagedAnswer;
        }        
        
        public void stageAnswer(YesNoCancel answer) {
            stagedAnswer = answer;
        }

        public void hasMessage(params string[] expected) {
            Bomb.when(isEmpty(messagesReceived), () => "no message to check against " + toShortString(expected));
            QAsserts.Matches(list(expected), messagesReceived);
            messagesReceived.Clear();
        }        
        
        public void noMessage() {
            Bomb.unless(isEmpty(messagesReceived), () => "expected no message, but had:\n" + toShortString(messagesReceived));
        }

        public void waitMatches<T>(T expected, Producer<T> create) {
            DbTestCase.waitMatches(expected, () => { doAllWork(); return create(); });
        }

        public new void wait(Predicate isTrue) {
            wait(80, 50, ()=> { doAllWork(); return isTrue();});
        }
    }
}