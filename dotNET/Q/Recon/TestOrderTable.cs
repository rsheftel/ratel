using NUnit.Framework;
using Q.Util;
using util;
using O=Q.Util.Objects;

namespace Q.Recon {
    [TestFixture]
    public class TestOrderTable : DbTestCase {
        [Test]
        public void testCanPopulateDataTable() {
            var now = "2008/07/05 12:31:45";
            Dates.freezeNow(now);

            addOrders();
            var gui = new FakeOrderTrackerGUI();
            var orderTable = gui.tracker.orderTable();
            gui.setMarket("RE.TEST.TY.1C");
            gui.doAllWork();
            var row = O.first(orderTable.rows());

            AreEqual("NDayBreak-daily-1.0", row["system"]);
            AreEqual("BFBD30", row["pv"]);
            AreEqual("RE.TEST.TY.1C", row["symbol"]);
            AreEqual(now, row["simFillTime"]);
            AreEqual("Enter short", row["enterExit"]);
            AreEqual("10,000", row["size"]);
            AreEqual("99.501235", row["simFillPrice"]);
            AreEqual("STOP(99.50)", row["details"]);
            AreEqual("an order", row["description"]);
            AreEqual("my pc", row["hostname"]);
        }

        static void addOrders() {
            TestOrderTracker.insertSimFilled(252, "RE.TEST.TY.1C", OrderTable.prefix);
            TestOrderTracker.insertSimFilled(356, "RE.TEST.TY.1C", OrderTable.prefix);
            TestOrderTracker.insertSimFilled(39, "RE.TEST.TY.1C", OrderTable.prefix);

        }

        [Test]
        public void testFilterBasedOnSiv() {
            addOrders();
            var gui = new FakeOrderTrackerGUI();
            var orderTable = gui.tracker.orderTable();
            gui.selectedSystem = "NDayBreak-daily-1.0";
            gui.setMarket("RE.TEST.TY.1C");
            gui.doAllWork();
            AreEqual(2, orderTable.count());
            O.each(orderTable.rows(), row => AreEqual("NDayBreak-daily-1.0", row["system"]));
        }

        [Test]
        public void testFilterBasedOnPv() {
            addOrders();
            var gui = new FakeOrderTrackerGUI();
            var orderTable = gui.tracker.orderTable();
            gui.selectedSystem = "NDayBreak-daily-1.0";
            gui.setMarket("RE.TEST.TY.1C");
            gui.selectedPv = "BFBD30";
            gui.tracker.pvUpdated();
            gui.doAllWork();
            AreEqual(1, orderTable.count());
            O.each(orderTable.rows(), row => AreEqual("BFBD30", row["pv"]));
        }

        [Test]
        public void testUpdates() {
            addOrders();
            var gui = new FakeOrderTrackerGUI();
            gui.tracker.initialize();
            gui.setMarket("RE.TEST.TY.1C");
            gui.doAllWork();
            AreEqual(3, gui.count());
            addOrders();
            fireOrdersReceived();
            O.wait(() => { gui.doAllWork(); return gui.count() == 6;});
            fireOrdersReceived();
            O.sleep(100);
            gui.doAllWork();
        }

        public static void fireOrdersReceived() {
            OrderTable.TOPIC.send("liveOrderId", "-1");
        }
    }
}