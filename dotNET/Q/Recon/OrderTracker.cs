using System.Collections.Generic;
using System.Data;
using db;
using systemdb.metadata;
using util;
using Market=systemdb.metadata.Market;

namespace Q.Recon {
    public class OrderTracker : Util.Objects {
        readonly OrderTrackerGUI gui;
        readonly OrderTable table;
        public OrderTracker(OrderTrackerGUI gui) {
            table = new OrderTable(gui);
            this.gui = gui;
        }

        public void initialize() {
            gui.setOrderTable(table.dataTable());
            initializeSystems();
            initializeMarkets();
            initializePvs();
            populate();
            OrderTable.TOPIC.subscribe(fields => gui.runOnGuiThread(() => table.update(fields)));
            FerretControl.incomingStatus().subscribe(fields => gui.runOnGuiThread(table.populate));
        }

        public static void Main(string[] args) {
            OrderTable.TOPIC.send(new Dictionary<string, object> {{"liveOrderId", "41916"}, {"timestamp", "5/7/2009 9:07:38 AM"}});
        }

        void initializeSystems() {
            gui.setSystemChoices(convert(list<Siv>(MsivTable.MSIVS.allSivs()), s => s.name()));
        }

        internal OrderTable orderTable() {
            return table;
        }

        public void marketsUpdated() {
            populate();
        }

        public void populate() {
            gui.runOnGuiThread(table.populate);
        }

        public void systemUpdated() {
            initializePvs();
            initializeMarkets();
            populate();
        }

        void initializePvs() {
            if (!gui.sivSelected()) {
                gui.setPvChoices(new List<string>());
                return;
            }
            var pvs = sort(convert(allLiveSystems(), ls => ls.pv().name()));
            gui.setPvChoices(pvs);
        }

        public void pvUpdated() {
            initializeMarkets();
            populate();
        }

        void initializeMarkets() {
            if (!gui.sivSelected()) {
                gui.setMarketChoices(new List<string>());
                return;
            }
            var marketObjs = collect(liveSystems(), ls => list<Market>(ls.markets()));
            var markets = sort(unique(convert(marketObjs, m => m.name())));
            gui.setMarketChoices(markets);
        }

        List<LiveSystem> liveSystems() {
            return gui.pvSelected() ? list(new LiveSystem(siv(), pv())) : allLiveSystems();
        }

        List<LiveSystem> allLiveSystems() {
            return list<LiveSystem>(siv().liveSystems());
        }

        Pv pv() {
            return new Pv(gui.pv());
        }

        Siv siv() {
            return Siv.fromSivName(gui.siv(), "-");
        }

        public DataView tableView() {
            return orderTable().dataTable().DefaultView;
        }

        public void subscribe() {
            var email = username() + "@malbecpartners.com";
            var siv = gui.siv();
            var pv = gui.pv();
            var market = gui.market();
            LiveOrderEmailsTable.ORDER_EMAILS.insert(siv, pv, market, email);
            Db.commit();
            var subscription = commaSep(siv, pv, market);
            gui.alertUser("You have subscribed to " + subscription + " to address " + email);
        }

        public void prefixUpdated() {
            populate();
        }

        public OrderStatus status(DataRow row) {
            return OrderTable.orderStatus(row);
        }

        public void filterUpdated() {
            populate();
        }

        public void release(DataRow row) {
            FerretControl.release((string) row["ferretOrderId"], Dates.date((string) row["submittedTime"]));
        }

        public void releaseMaybe(IEnumerable<DataRow> rows) {
            var skipped = 0;
            var count = 0;
            each(rows, row => { if(isFerretStaged(row)) release(row); else skipped++; count++;});
            if (skipped == count) {
                gui.alertUser("None of the selected orders can be released. Please select a staged Ferret Order to release.");
                return;
            }
            if(skipped > 0) gui.alertUser("Skipped " + skipped + " orders (they are not in Stage status)");
        }

       public static bool isFerretStaged(DataRow row) {
            return row["status"].Equals("Stage");
        }

        public void cancelMaybe(IEnumerable<DataRow> rows) {
            var skipped = 0;
            var count = 0;
            each(rows, row => { if(isCancellable(row)) cancel(row); else skipped++; count++;});
            if (skipped == count) {
                gui.alertUser("None of the selected orders can be cancelled. Please select an open Ferret Order to cancel.");
                return;
            }
            if(skipped > 0) gui.alertUser("Skipped " + skipped + " orders (they are filled, cancelled or not Ferret)");
        }

        static void cancel(DataRow row) {
            FerretControl.cancel((string) row["ferretOrderId"], Dates.date((string) row["submittedTime"]));
        }

        static bool isCancellable(DataRow row) {
            return hasContent(row, "ferretOrderId") && !OrderTable.NOT_CANCELLABLE.Contains((string) row["status"]);
        }
    }
}
