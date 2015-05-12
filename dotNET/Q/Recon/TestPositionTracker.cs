using System.Collections.Generic;
using System.Data;
using jms;
using NUnit.Framework;
using Q.Util;
using systemdb.data;
using O=Q.Util.Objects;

namespace Q.Recon {
    [TestFixture]
    public class TestPositionTracker : DbTestCase {
        const string BLID1 = "some bloomberg id";
        const string BLID2 = "some other bloomberg id";
        const string BLID3 = "some third bloomberg id";

        [Test]
        public void testStartsWithNoPositions() {
            O.freezeNow("2001/01/01 16:01:00");
            var rediBlid1 = new QTopic("some.redi.topic." + BLID1);
            var rediBlid2 = new QTopic("some.redi.topic." + BLID2);
            var rediBlid3 = new QTopic("some.redi.topic." + BLID3);
            var aimBlid1 = new QTopic("some.aim.topic." + BLID1);
            var aimBlid2 = new QTopic("some.aim.topic." + BLID2);
            var fakeGui = new FakePositionTrackerGui();
            var tracker = new PositionTracker(fakeGui, new QTopic("some.redi.topic.*"), new QTopic("some.aim.topic.*"));
            tracker.initialize();
            IsTrue(O.isEmpty(tracker.ids()));
            fakeGui.requireRowCount(0);
            rediBlid1.send(fields("REDI", BLID1, 5, 4, "2001/01/01 16:00:00"));
            fakeGui.requireRowCount(1);
            fakeGui.requireStatus(fakeGui.row(0), PositionTracker.Status.UNMATCHED);
            rediBlid1.send(fields("REDI", BLID1, 6, 4, "2001/01/01 16:00:01"));
            fakeGui.requireRowCount(1);
            fakeGui.requireStatus(fakeGui.row(0), PositionTracker.Status.UNMATCHED);

            aimBlid1.send(fields("AIM", BLID1, 6, 4, "2001/01/01 16:00:03"));
            waitMatches(6, () => tracker.buyCount("AIM",BLID1));
            waitMatches(4, () => tracker.sellCount("AIM",BLID1));
            fakeGui.requireRowCount(1);
            fakeGui.requireStatus(fakeGui.row(0), PositionTracker.Status.MATCHED);
            aimBlid1.send(fields("AIM", BLID1, 7, 5, "2001/01/01 16:00:04"));
            fakeGui.requireRowCount(1);
            fakeGui.requireStatus(fakeGui.row(0), PositionTracker.Status.UNMATCHED);
            
            waitMatches(0, () => tracker.skipped);

            rediBlid1.send(fields("REDI", BLID1, 7, 5, "2001/01/01 16:00:06"));
            aimBlid2.send(fields("AIM", BLID2, 8, 6, "2001/01/01 16:00:08"));
            fakeGui.requireRowCount(2);
            fakeGui.requireStatus(fakeGui.row(0), PositionTracker.Status.MATCHED);
            fakeGui.requireStatus(fakeGui.row(1), PositionTracker.Status.UNMATCHED);

            rediBlid2.send(fields("REDI", BLID2, 8, 5, "2001/01/01 16:00:09"));
            fakeGui.requireRowCount(2);
            fakeGui.requireStatus(fakeGui.row(0), PositionTracker.Status.MATCHED);
            fakeGui.requireStatus(fakeGui.row(1), PositionTracker.Status.UNMATCHED);

            rediBlid3.send(fields("REDI", BLID3, 8, 5, "2000/12/31 23:59:59"));
            waitMatches(1, () => tracker.skipped);
            fakeGui.requireRowCount(2);

            rediBlid2.send(fields("REDI", BLID2, 8, 6, "2001/01/01 16:00:10"));
            fakeGui.requireRowCount(2);
            fakeGui.requireStatus(fakeGui.row(0), PositionTracker.Status.MATCHED);
            fakeGui.requireStatus(fakeGui.row(1), PositionTracker.Status.MATCHED);
        }

        static Fields fields(string source, string bloombergId, int buys, int sells, string time) {
            var result = new Fields();
            result.put("SOURCE", source);
            result.put("BID", bloombergId);
            result.put("SHARESBOUGHT", buys);
            result.put("SHARESSOLD", sells);
            result.put("TIMESTAMP", time);
            return result;
        }

        internal class FakePositionTrackerGui : FakeGUI, PositionTrackerGUI {
            internal DataTable table;
            readonly Dictionary<DataRow, PositionTracker.Status> statuses = dictionary<DataRow, PositionTracker.Status>();
            public void requireRowCount(int expected) {
                waitMatches(expected, () => table.Rows.Count);
            }

            public void addRow(DataRow row) {
                table.Rows.Add(row);
            }

            public void setTable(DataTable newTable) {
                table = newTable;
            }

            public void updated() {
                doAllWork();
            }

            public void setStatus(DataRow row, PositionTracker.Status status) {
                if (statuses.ContainsKey(row)) statuses[row] = status;
                else statuses.Add(row, status);
            }

            public DataRow row(int index) {
                return table.Rows[index];
            }

            public void requireStatus(DataRow dataRow, PositionTracker.Status expected) {
                waitMatches(expected, () => {
                                          PositionTracker.Status actual;
                                          statuses.TryGetValue(dataRow, out actual);
                                          return actual;
                                      });
            }
        }
    }
}