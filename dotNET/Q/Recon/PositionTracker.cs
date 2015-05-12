using System.Collections.Generic;
using System.Data;
using QTopic=jms.QTopic;
using systemdb.data;
using util;
using Channel=Q.Messaging.Channel;

namespace Q.Recon {
    public interface PositionTrackerGUI : QGUI {
        void setTable(DataTable table);
        void setStatus(DataRow row, PositionTracker.Status status);
    }

    public class PositionTracker : Util.Objects {
        public const string DEFAULT_RECONCILIATION_BROKER = "tcp://amqpositions:63636";
        public const string BLOOMBERGID_COL = "Bloomberg Id";
        public const string LASTUPDATED_COL = "Last Updated";

        readonly Dictionary<string, DataRow> rows = dictionary<string, DataRow>();

        class PositionInfo {
            internal readonly long bought;
            public readonly long sold;

            public PositionInfo(long bought, long sold) {
                this.bought = bought;
                this.sold = sold;
            } 
        }

        public enum Status {
            MATCHED, UNMATCHED
        }

        readonly IDictionary<string, Dictionary<string, PositionInfo>> positions = dictionary<string, Dictionary<string, PositionInfo>>();
        readonly PositionTrackerGUI gui;
        readonly QTopic rediTopic;
        readonly QTopic aimTopic;
        readonly DataTable table;
        internal int skipped;

        public PositionTracker(PositionTrackerGUI gui, QTopic rediTopic, QTopic aimTopic) {
            this.gui = gui;
            this.rediTopic = rediTopic;
            this.aimTopic = aimTopic;
            table = empty();
            gui.setTable(table);
        }

        public void initialize() {
            rediTopic.register(FieldsReceiver.receiver(new Channel.ActionListener(updatePositions)));
            aimTopic.register(FieldsReceiver.receiver(new Channel.ActionListener(updatePositions)));
        }

        void updatePositions(Fields fields) {
            if (date(fields.time("TIMESTAMP")) < date(Dates.midnight())) {
                skipped++;
                return;
            }
            var source = fields.text("SOURCE");
            var bloomberg = fields.text("BID");
            var sharesBought = fields.longg("SHARESBOUGHT");
            var sharesSold = fields.longg("SHARESSOLD");
            var position = new PositionInfo(sharesBought, sharesSold);
            lock (positions)
                if (positions.ContainsKey(bloomberg)) {
                    var sourceInfo = positions[bloomberg];
                    if (sourceInfo.ContainsKey(source)) sourceInfo[source] = position;
                    else sourceInfo.Add(source, position);
                } else {
                    var sourceInfo = dictionary<string, PositionInfo>();
                    sourceInfo.Add(source, position);
                    positions.Add(bloomberg, sourceInfo);
                }
            updateGui(source, bloomberg, position);
        }

        void updateGui(string source, string bloombergId, PositionInfo position_) {
            lock(rows) {
                if (!rows.ContainsKey(bloombergId)) {
                    var newRow = table.NewRow();
                    newRow[BLOOMBERGID_COL] = bloombergId;
                    newRow["REDI Bought"] = "unknown";
                    newRow["REDI Sold"] ="unknown";
                    newRow["REDI Net"] ="unknown";
                    newRow["AIM Bought"] = "unknown";
                    newRow["AIM Sold"] = "unknown";
                    newRow["AIM Net"] ="unknown";
                    newRow["Last Source"] = "unkown";
                    newRow[LASTUPDATED_COL] = ymdHuman(now());
                    table.Rows.Add(newRow);
                    rows.Add(bloombergId, newRow);
                }
                var row = rows[bloombergId];
                row[source + " Bought"] = position_.bought;
                row[source + " Sold"] = position_.sold;
                row[source + " Net"] = position_.bought - position_.sold;
                row["Last Source"] = source;
                row[LASTUPDATED_COL] = ymdHuman(now());
                gui.setStatus(row, reconciliationStatus(bloombergId));
            }
        }

        public Status reconciliationStatus(string bloombergId) {
            var status = Status.UNMATCHED;
            if (hasPosition("REDI", bloombergId) && hasPosition("AIM", bloombergId)) {
                var redi = position("REDI", bloombergId);
                var aim = position("AIM", bloombergId);
                if (redi.bought == aim.bought && redi.sold == aim.sold) status = Status.MATCHED;
            }
            return status;
        }

        static DataTable empty() {
            var table = new DataTable();
            table.Columns.Add(BLOOMBERGID_COL, typeof (string));
            table.Columns.Add("REDI Bought", typeof (string));
            table.Columns.Add("REDI Sold", typeof (string));
            table.Columns.Add("REDI Net", typeof (string));
            table.Columns.Add("AIM Bought", typeof (string));
            table.Columns.Add("AIM Sold", typeof (string));
            table.Columns.Add("AIM Net", typeof (string));
            table.Columns.Add("Last Source", typeof (string));
            table.Columns.Add(LASTUPDATED_COL, typeof (string));
            return table;
        }

        public IEnumerable<string> ids() {
            return list<string>();
        }

        public long buyCount(string source, string bloombergId) {
            lock (positions) return hasPosition(source, bloombergId) ? position(source, bloombergId).bought : -1;
        }

        public long sellCount(string source, string bloombergId) {
            lock(positions) return hasPosition(source, bloombergId) ? position(source, bloombergId).sold : -1;
        }

        PositionInfo position(string source, string bloombergId) {
            lock(positions) return positions[bloombergId][source];
        }

        bool hasPosition(string source, string bloombergId) {
            lock (positions) return positions.ContainsKey(bloombergId) && positions[bloombergId].ContainsKey(source);
        }
    }
}