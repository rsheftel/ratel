using System;
using System.Collections.Generic;
using System.Data;
using Q.Messaging;
using Q.Util;
using systemdb.data;
using systemdb.metadata;
using util;

namespace Q.Recon {
    public class OrderTable : Util.Objects {
        public static readonly Topic TOPIC = new Topic("OrderTracker.orderAdded");
        // all these should be disjoint and cover every possible ferret status. 
        static readonly List<string> NO_ACTION_REQUIRED = list("Filled", "Market", "Cancelled");
        static readonly List<string> ACTION_REQUIRED = list("Ticket", "Submitted");
        static readonly List<string> STAGED = list("Stage");
        static readonly List<string> FAILED = list("FerretRejected", "PlatformRejected", "Unknown", "CancelOrReplaceFailed");

        // overlapping set
        public static readonly List<string> NOT_CANCELLABLE = list("Filled", "Cancelled");

        public const string DEFAULT_PREFIX = "TOMAHAWK";
        public static string prefix = DEFAULT_PREFIX;

        public event Action onPopulateComplete;

        readonly OrderTrackerGUI gui;
        readonly DataTable table;
        readonly List<LiveOrders.LiveOrder> allOrders = new List<LiveOrders.LiveOrder>();
        int lastMaxOrder;
        readonly Dictionary<int, DataRow> liveOrderRows = new Dictionary<int, DataRow>();

        public OrderTable(OrderTrackerGUI gui) {
            this.gui = gui;
            onPopulateComplete += doNothing;
            table = empty();
            updateOrders(); 
        }

        public void populate() {
            liveOrderRows.Clear();
            lock (table) table.Clear();
            lastMaxOrder = 0;
            update();
            onPopulateComplete();
        }

        public void update(Fields fields) {
            var id = fields.integer("liveOrderId");
            if (liveOrderRows.ContainsKey(id)) {
                var orderRow = liveOrderRows[id];
                var order = LiveOrders.ORDERS.order(id);
                lock(table) {
                    orderRow["simFillPrice"] = priceString(order);
                    orderRow["simFillTime"] = simFillTimeString(order);
                    var status = orderStatus(orderRow);
                    gui.setStatus(orderRow, status);
                }
                
            }
            update();
        }

        void update() {
            updateOrders();
            var orders = matchingOrders();
            each(reverse(orders), addOrderRow);
        }
        
        static DataTable empty() {
            var table = new DataTable();
            table.Columns.Add("system", typeof (string));
            table.Columns.Add("pv", typeof (string));
            table.Columns.Add("symbol", typeof (string));
            table.Columns.Add("enterExit", typeof (string));
            table.Columns.Add("size", typeof (string));
            table.Columns.Add("details", typeof (string));
            table.Columns.Add("description", typeof (string));
            table.Columns.Add("status", typeof (string));
            table.Columns.Add("submittedTime", typeof (string));
            table.Columns.Add("simFillTime", typeof (string));
            table.Columns.Add("simFillPrice", typeof (string));
            table.Columns.Add("hostname", typeof (string));
            table.Columns.Add("ferretOrderId", typeof (string));
            table.Columns.Add("liveOrderId", typeof (string));
            return table;
        }

        void addOrderRow(LiveOrders.LiveOrder o) {
            var row = table.NewRow();
            lock(table) {
                var system = o.liveSystem();
                row["system"] = o.sivName();
                row["pv"] = system.pv().name();
                row["symbol"] = o.market();
                row["simFillTime"] = simFillTimeString(o);
                row["enterExit"] = o.entryExit() + " " + o.positionDirection();
                row["size"] = o.size().ToString("N0");
                row["simFillPrice"] = priceString(o);
                row["details"] = o.orderDetails();
                row["description"] = o.description();
                row["hostname"] = o.hostname();
                row["ferretOrderId"] = o.ferretOrderId();
                row["status"] = o.isFerret() ? "Submitted" : "";
                row["submittedTime"] = submittedTimeString(o);
                row["liveOrderId"] = "" + o.id();
                liveOrderRows.Add(o.id(), row);
                table.Rows.InsertAt(row, 0);
            }
            if (FerretControl.canRelease()) gui.addContextMenu(row);
            else gui.removeContextMenu(row);
            if(o.isFerret()) {
                FerretControl.onIncomingOrderResponse(o, fields => updateStatus(row, fields));
            }
        }

        private void updateStatus(DataRow row, Fields fields) {
            if (row.RowState == DataRowState.Detached) return;
            var status_ = statusString(fields);
            lock(table) {
                row["status"] = status_;
                gui.setStatus(row, orderStatus(row));
            }
        }

        public static OrderStatus orderStatus(DataRow row) {
            if (!hasContent(row, "status")) return OrderStatus.NOT_FERRET;
            var statusString = (string) row["status"];
            var isMismatch = statusString.Equals("Filled") != (hasContent(statusString) && hasContent(row, "simFillTime"));
            return isMismatch ? OrderStatus.SIM_MISMATCH : orderStatus(statusString);
        }

        static OrderStatus orderStatus( string status_) {
            if(isEmpty(status_)) return OrderStatus.NOT_FERRET;
            if (NO_ACTION_REQUIRED.Contains(status_)) return OrderStatus.NO_ACTION_REQUIRED;
            if (ACTION_REQUIRED.Contains(status_)) return OrderStatus.ACTION_REQUIRED;
            if (STAGED.Contains(status_)) return OrderStatus.STAGE;
            if (FAILED.Contains(status_)) return OrderStatus.FAILED;
            throw Bomb.toss("no color for " + status_);
        }

        static String statusString(Fields fields) {
            var ferretStatus = fields.text("STATUS");
            if(ferretStatus.Equals("Filled")) return "Filled";
            if(ferretStatus.Equals("PlatformRejected")) return "PlatformRejected";
            if(list("Invalid", "Duplicate", "FailedInsert", "FerretRejected").Contains(ferretStatus)) return "FerretRejected";
            if(ferretStatus.Equals("Unknown")) return "Unknown";
            if(ferretStatus.Equals("New")) return "Stage";
            if(list("Executing", "PendingCancel", "CancelRequested", "CancelReplaceRequested", "Replaced").Contains(ferretStatus)) return "Market";
            if(list("Expired", "Cancelled").Contains(ferretStatus)) return "Cancelled";
            if(list("CancelRequestFailed", "CancelReplaceRequestFailed").Contains(ferretStatus)) return "CancelOrReplaceFailed";
            Bomb.unless(list("Accepted", "Sent", "PendingNew").Contains(ferretStatus), () => "unknown status from Ferret: " + ferretStatus);
            return fields.hasValue("DESTINATION", "TICKET") ? "Ticket" : "Market";
        }


        static string submittedTimeString(LiveOrders.LiveOrder o) {
            return o.isFerret() ? ymdHuman(date(o.submittedTime())) : "";
        }

        static string simFillTimeString(LiveOrders.LiveOrder o) {
            return o.simFillTime() == null ? "" :  ymdHuman(date(o.simFillTime()));
        }

        static string priceString(LiveOrders.LiveOrder o) {
            return o.price() == null ? "" : Strings.nDecimals(6, o.price().doubleValue());
        }

        List<LiveOrders.LiveOrder> matchingOrders() {
            var result = accept(allOrders, o => o.id() > lastMaxOrder);
            if(isEmpty(result)) return result;
            lastMaxOrder = first(result).id();
            var market = gui.market();
            if(!market.Equals("ALL"))
                result = accept(result, order => market.Equals(order.market()));
            if (gui.sivSelected())
                result = accept(result, order => gui.siv().Equals(order.liveSystem().siv().sivName("-")));
            if (gui.pvSelected())
                result = accept(result, order => gui.pv().Equals(order.liveSystem().pv().name()));
            if (!prefix.Equals("ALL"))
                result = accept(result, order => prefix.Equals(order.prefix()));
            if (!gui.filter().Equals("ALL")) result = accept(result, matchesStatusFilter);
            return result;
        }

        bool matchesStatusFilter(LiveOrders.LiveOrder o) {
            var filter = gui.filter();
            if (filter.Equals("ALL")) return true;
            if (filter.Equals("Not Ferret")) return !o.isFerret();
            if (filter.Equals("Ferret")) return o.isFerret();
            throw Bomb.toss("unknown filter " + filter);
        }

        void updateOrders() {
            var id = isEmpty(allOrders) ? LiveOrders.ORDERS.maxIdBeforeToday() : first(allOrders).id();
            var current = list<LiveOrders.LiveOrder>(LiveOrders.ORDERS.ordersAfter(id));
            allOrders.InsertRange(0, current);
        }

        public static Topic topic() {
            return TOPIC;
        }

        public DataTable dataTable() {
            return table;
        }

        public int count() {
            lock(table)
                return table.Rows.Count;
        }

        public List<DataRow> rows() {
            lock(table)
                return convert<DataRow, DataRow>(table.Rows, r => r);
        }
    }
}
