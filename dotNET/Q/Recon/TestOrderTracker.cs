using System.Collections.Generic;
using System.Data;
using db.clause;
using java.util;
using jms;
using NUnit.Framework;
using Q.Util;
using systemdb.data;
using systemdb.metadata;
using util;
using O=Q.Util.Objects;

namespace Q.Recon {
    [TestFixture]
    public class TestOrderTracker : DbTestCase {
        static Predicate populateCompleted;
        const string SYMBOL = "RE.TEST.TY.1C";
        const string SYSTEM = "NDayBreak-daily-1.0";

        [Test]
        public void testPopulation() {
            insertSimFilled(39, SYMBOL, "test");// NdayBreak
            insertSimFilled(39, SYMBOL, "test");
            insertSimFilled(252, SYMBOL, "test"); // cve

            var gui = startGui();
            AreEqual("ALL", gui.siv());
            
            IsTrue(gui.systems.Contains(SYSTEM));
            IsNotNull(gui.gridContents);
            
            gui.doAllWork();
            AreEqual(3, gui.tracker.orderTable().count());
        }

        [Test]
        public void testFiltering() {
            insertSimFilled(39, SYMBOL, "test");
            var ferret = insertSubmitted(39, SYMBOL, "test", "f2");
            LiveOrders.ORDERS.order(ferret).updateFill(12345, Dates.now());
            var gui = startGui();
            gui.waitMatches(2, () => gui.tracker.orderTable().count());
            publishFerretResponse(Dates.now(), "f1", "Accepted", "TICKET");
            publishFerretResponse(Dates.now(), "f2", "Filled", "REDI");
            gui.waitMatches("Filled", () => latestRow(gui)["status"]);
            gui.setStatusFilter("Not Ferret");
            gui.doAllWork();
            AreEqual(1, gui.tracker.orderTable().count());
        }

        [Test]
        public void testFerretOrderSimFilled() {
            O.freezeNow("2009/03/03");
            insertSubmitted(39, SYMBOL, "test", "f2");
            var id = insertSubmitted(39, SYMBOL, "test", "f3");
            insertSubmitted(39, SYMBOL, "test", "f4");
            insertSubmitted(39, SYMBOL, "test", "f5");
            var gui = startGui();
            gui.setMarket(SYMBOL);
            gui.wait(populateCompleted);
            var filledAt = Dates.date("1999/09/09 09:09:09");
            LiveOrders.ORDERS.order(id).updateFill(12345, filledAt);
            publish(id);
            var row = Util.Objects.nth(gui.tracker.orderTable().rows(), 3);
            AreEqual(id.ToString(), row["liveOrderId"]);
            gui.waitMatches("12345.000000", () => row["simFillPrice"]);
        }

        [Test]
        public void testLiveUpdating() {
            O.freezeNow("2009/03/03");
            // orders there on startup
            insertSimFilled(39, SYMBOL, "test");
            insertSubmitted(39, SYMBOL, "test", "f2");
            var gui = startGui();
            gui.setMarket(SYMBOL);
            gui.wait(populateCompleted);
            AreEqual(gui.tracker.orderTable().count(), 2);
            publishFerretResponse(Dates.now(), "f2", "Accepted", "DMA");
            gui.waitMatches("Market", () => latestRow(gui)["status"]);
            assertStatus(gui, OrderStatus.NO_ACTION_REQUIRED);
            // new order sim filled
            var id = insertSimFilled(39, SYMBOL, "test");
            publishAndWaitForCount(gui, id, 3);
            IsFalse(gui.statii.ContainsKey(latestRow(gui)));
            // new order submitted
            id = insertSubmitted(39, SYMBOL, "test", "f4");
            publishAndWaitForCount(gui, id, 4);
            AreEqual("f4", latestRow(gui)["ferretOrderId"]);
            AreEqual("Submitted", latestRow(gui)["status"]);
            publishFerretResponse(Dates.now(), "f4", "Accepted", "DMA");
            gui.waitMatches("Market", () => latestRow(gui)["status"]);
            assertStatus(gui, OrderStatus.NO_ACTION_REQUIRED);
            publishFerretResponse(Dates.now(), "f4", "Executing", "DMA");
            gui.waitMatches("Market", () => latestRow(gui)["status"]);
            assertStatus(gui, OrderStatus.NO_ACTION_REQUIRED);
            publishFerretResponse(Dates.now(), "f4", "Accepted", "TICKET");
            gui.waitMatches("Ticket", () => latestRow(gui)["status"]);
            assertStatus(gui, OrderStatus.ACTION_REQUIRED);
            publishFerretResponse(Dates.now(), "f4", "Executing", "TICKET");
            gui.waitMatches("Market", () => latestRow(gui)["status"]);
            assertStatus(gui, OrderStatus.NO_ACTION_REQUIRED);
            publishFerretResponse(Dates.now(), "f4", "New", "UNUSED");
            var stagedRow = latestRow(gui);
            gui.waitMatches("Stage", () => stagedRow["status"]);
            assertStatus(gui, OrderStatus.STAGE);
            publishFerretResponse(Dates.now(), "f4", "PlatformRejected", "UNUSED");
            gui.waitMatches("PlatformRejected", () => latestRow(gui)["status"]);
            assertStatus(gui, OrderStatus.FAILED);
            publishFerretResponse(Dates.now(), "f4", "Filled", "DMA");
            gui.waitMatches("Filled", () => latestRow(gui)["status"]);
            assertStatus(gui, OrderStatus.SIM_MISMATCH);
            // submitted order updated due to sim fill
            var filledAt = Dates.date("1999/09/09 09:09:09");
            LiveOrders.ORDERS.order(id).updateFill(12345, filledAt);
            publish(id);
            gui.waitMatches("12345.000000", () => latestRow(gui)["simFillPrice"]);
            assertStatus(gui, OrderStatus.NO_ACTION_REQUIRED);
        }

        [Test]
        public void testReleaseSelected() {
            var gui = startGui();
            FerretControl.setStatus("Ticket");
            insertSubmitted(39, SYMBOL, "test", "REL1");
            insertSubmitted(39, SYMBOL, "test", "REL2");
            var id3 = insertSubmitted(39, SYMBOL, "test", "REL3");
            publishAndWaitForCount(gui, id3, 3);
            publishFerretResponse(Dates.now(), "REL1", "New", "UNUSED");
            gui.waitMatches("Stage", () => gui.ferretStatus("REL1"));
            publishFerretResponse(Dates.now(), "REL2", "New", "UNUSED");
            gui.waitMatches("Stage", () => gui.ferretStatus("REL2"));
            var messages = new List<Fields>();
            FerretControl.onOutgoing(messages.Add);
            gui.tracker.releaseMaybe(gui.rows());
            gui.waitMatches(2, () => messages.Count);
            gui.hasMessage("Skipped.*1");
            changeFerretStatus(gui, "REL1", "Filled", "Filled");
            changeFerretStatus(gui, "REL2", "Filled", "Filled");
            gui.tracker.releaseMaybe(gui.rows());
            gui.hasMessage("None of the selected orders can be released. Please select a staged Ferret Order to release.");
        }

        [Test]
        public void testCancelSelected() { // any non-filled non-cancelled ferret order can be cancelled independent of ferret state
            var gui = startGui();
            FerretControl.setStatus("DMA");
            insertSubmitted(39, SYMBOL, "test", "TCAN1");
            insertSubmitted(39, SYMBOL, "test", "TCAN2");
            var id3 = insertSubmitted(39, SYMBOL, "test", "TCAN3");
            insertSimFilled(39, SYMBOL, "test");
            publishAndWaitForCount(gui, id3, 4);
            changeFerretStatus(gui, "TCAN1", "New", "Stage");
            changeFerretStatus(gui, "TCAN2", "Filled", "Filled");
            var messages = new List<Fields>();
            FerretControl.onOutgoing(messages.Add);
            gui.tracker.cancelMaybe(gui.rows());
            gui.waitMatches(2, () => messages.Count);
            var cancel = Util.Objects.first(messages);
            AreEqual("CancelOrder", cancel.text("MESSAGETYPE"));
            AreEqual("CCAN3", cancel.text("USERORDERID"));
            gui.hasMessage("Skipped.*2");
            changeFerretStatus(gui, "TCAN1", "Cancelled", "Cancelled");
            changeFerretStatus(gui, "TCAN3", "Cancelled", "Cancelled");
            gui.tracker.cancelMaybe(gui.rows());
            gui.hasMessage("None of the selected orders can be cancelled. Please select an open Ferret Order to cancel.");
        }

        static void changeFerretStatus(FakeOrderTrackerGUI gui, string ferretId, string status, string expectedFerretOrderStatus) {
            changeFerretStatus(gui, ferretId, status, expectedFerretOrderStatus, "UNSTATED_DESTINATION");
        }

        static void changeFerretStatus(FakeOrderTrackerGUI gui, string ferretId, string status, string expectedFerretOrderStatus, string destination) {
            publishFerretResponse(Dates.now(), ferretId, status, destination);
            gui.waitMatches(expectedFerretOrderStatus, () => gui.ferretStatus(ferretId));
        }

        [Test]
        public void testReleaseToMarket() {
            Dates.freezeNow("2009/09/09 09:09:09");
            var gui = startGui();
            FerretControl.setStatus("Ticket");
            var id = insertSubmitted(39, SYMBOL, "test", "FERREL");
            publishAndWaitForCount(gui, id, 1);
            gui.wait(populateCompleted);
            AreEqual("Submitted", latestRow(gui)["status"]);
            gui.requireMenu(latestRow(gui));
            changeFerretStatus(gui, "FERREL", "New", "Stage");
            gui.requireMenu(latestRow(gui));
            changeFerretStatus(gui, "FERREL", "Accepted", "Ticket", "TICKET");
            gui.requireMenu(latestRow(gui));
            FerretControl.setStatus("Stage");
            gui.wait(populateCompleted);
            changeFerretStatus(gui, "FERREL", "New", "Stage");
            gui.noMenu(latestRow(gui));
            FerretControl.setStatus("Ticket");
            gui.wait(populateCompleted);
            changeFerretStatus(gui, "FERREL", "New", "Stage");
            gui.requireMenu(latestRow(gui));

            Fields fields = null;
            FerretControl.onOutgoing(f => fields = f);
            gui.tracker.release(latestRow(gui));
            O.wait(() => fields != null);
            AreEqual("ReleaseStagedOrder", fields.text("MESSAGETYPE"));
            AreEqual("FERREL", fields.text("USERORDERID"));
            AreEqual("2009-09-09", fields.text("ORDERDATE"));
        }
        

        static void assertStatus(FakeOrderTrackerGUI gui, OrderStatus expected) {
            var row = latestRow(gui);
            Bomb.unless(gui.statii.ContainsKey(row), () => "latest row has not been colored.");
            var color = gui.statii[row];
            AreEqual(expected, color);
        }

        static void publishFerretResponse(Date orderDate, string ferretId, string status, string destination) {
            var updateResponse = FerretControl.incomingResponses(orderDate, ferretId);
            var fields = new Fields();
            fields.put("ORDERDATE", O.ymdHuman(orderDate));
            fields.put("STATUS", status);
            fields.put("DESTINATION", destination);
            fields.put("USERORDERID", ferretId);
            updateResponse.send(fields);
        }

        static DataRow latestRow(FakeOrderTrackerGUI gui) {
            return O.first(gui.tracker.orderTable().rows());
        }

        static void publishAndWaitForCount(FakeOrderTrackerGUI gui, int id, int count) {
            publish(id);
            gui.waitMatches(count, () => gui.tracker.orderTable().count());
        }

        static void publish(int id) {
            OrderTable.TOPIC.send(new Dictionary<string, object> {
                { "liveOrderId", id },
                { "timestamp", O.now() }
            });
        }

        [Test]
        public void testPrefix() {
            insertSimFilled(39, SYMBOL, "test");
            insertSimFilled(39, SYMBOL, OrderTable.DEFAULT_PREFIX);
            insertSimFilled(252, SYMBOL, OrderTable.DEFAULT_PREFIX);

            var gui = startGui();
            OrderTable.prefix = OrderTable.DEFAULT_PREFIX;
            
            gui.setMarket(SYMBOL);
            gui.doAllWork();

            AreEqual(2, gui.tracker.orderTable().count());

            OrderTable.prefix = "test";
            gui.tracker.populate();
            gui.doAllWork();
            AreEqual(1, gui.tracker.orderTable().count());

        }

        static FakeOrderTrackerGUI startGui() {
            OrderTable.prefix = "test";
            var gui = new FakeOrderTrackerGUI();  
            var populateComplete = false;
            var lockObj = new object();
            gui.tracker.orderTable().onPopulateComplete += () => { lock (lockObj) populateComplete = true; };
            populateCompleted = () => { lock(lockObj) { var result = populateComplete; populateComplete = false; return result;} };
            gui.setMarket(SYMBOL);
            var tracker = gui.tracker;
            tracker.initialize();
            gui.wait(populateCompleted);
            return gui;
        }

        [Test]
        public void testSystemSelectPopulatesPvAndMarketCombo() {
            O.freezeNow("2009/07/15");
            var gui = startGui();
            gui.tracker.systemUpdated();
            gui.doAllWork();
            gui.selectedSystem = SYSTEM;
            gui.tracker.systemUpdated();
            gui.wait(populateCompleted);
            IsTrue(gui.marketChoices.Contains("TY.1C"));
            IsTrue(gui.marketChoices.Contains("CD.1C"));
            IsFalse(gui.marketChoices.Contains("TRI.CNAIG5"));
            IsTrue(gui.pvChoices.Contains("BFBD30"));
            IsFalse(gui.pvChoices.Contains("CDXMV20"));
            gui.selectedPv = "BFBD30";
            gui.tracker.pvUpdated();
            gui.wait(populateCompleted);
            IsTrue(gui.marketChoices.Contains("TY.1C"));
            IsFalse(gui.marketChoices.Contains("CD.1C"));
        }

        [Test]
        public void testSubscribeButton() {
            var gui = startGui();
            var t = LiveOrderEmailsTable.ORDER_EMAILS;
            t.deleteAll(Clause.TRUE);
            AreEqual(0, t.emails("NDayBreak-daily-1.0", "BFBD30", "RE.TEST.TY.1C").size());
            gui.selectedSystem = "NDayBreak-daily-1.0";
            gui.selectedPv = "BFBD30";
            gui.setMarketChoices(O.list("ALL"));
            gui.tracker.subscribe();
            var addresses = t.emails("NDayBreak-daily-1.0", "BFBD30", "RE.TEST.TY.1C");
            AreEqual(1, addresses.size());
            var expected = "You have subscribed to NDayBreak-daily-1.0, BFBD30, RE.TEST.TY.1C " + 
                "to address " + addresses.get(0);
            gui.hasMessage(expected);
        }

        public static int insertSimFilled(int systemId, string market, string prefix) {
            return LiveOrders.ORDERS.insert(
                systemId, market, Dates.now(), null, "Enter", "short", 10000, new java.lang.Double(99.50123456789), "STOP(99.50)", 
                "an order", "my pc", prefix, null
            );
        }

        static int insertSubmitted(int systemId, string market, string prefix, string ferretId) {
            return LiveOrders.ORDERS.insert(
                systemId, market, null,  Dates.now(), "Enter", "short", 10000, null, "STOP(99.50)", 
                "an order", "my pc", prefix, ferretId
            );
        }

        public override void setUp() {
            base.setUp();
            FerretControl.setBroker(JMSTestCase.TEST_BROKER2);
            FerretControl.setStatus("Inactive");
        }
    }
}