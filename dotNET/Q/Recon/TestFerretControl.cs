using System.Collections.Generic;
using jms;
using NUnit.Framework;
using Q.Messaging;
using Q.Util;
using systemdb.data;

namespace Q.Recon {

    public class FakeFerretControlGui : FakeGUI, FerretControlGui {
        string lastStatus = "Unknown";
        bool dmaEnabled;
        bool ticketEnabled;
        bool stagedEnabled;

        internal string status() {
            return lastStatus;
        }

        public void setStatus(string newStatus) {
            lastStatus = newStatus;
        }

        public void setEnabled(bool dma, bool ticket, bool staged) {
            dmaEnabled = dma;
            ticketEnabled = ticket;
            stagedEnabled = staged;
        }

        public void requireEnabled(bool dma, bool ticket, bool staged) {
            waitMatches(dma, () => dmaEnabled);
            waitMatches(ticket, () => ticketEnabled);
            waitMatches(staged, () => stagedEnabled);
        }

    }

    [TestFixture]
    public class TestFerretPretendStatus : DbTestCase {
        [Test]
        public void testPretendStatus() {
            FerretControl.setStatus("Stage");
            AreEqual(FerretControl.status(), "Stage");
            FerretControl.setStatus("Foo");
            AreEqual(FerretControl.status(), "Foo");
            FerretControl.setStatus("Stage");
            AreEqual(FerretControl.status(), "Stage");
            FerretControl.setStatus("Blarg");
            AreEqual(FerretControl.status(), "Blarg");
            FerretControl.setStatus("Stage");
            AreEqual(FerretControl.status(), "Stage");
        }
    }

    [TestFixture]
    public class TestFerretControl : DbTestCase {
        Topic ferretStatus;

        [Test]
        public void testPopulation() {
            var gui = new FakeFerretControlGui();
            new FerretControl(gui, ferretStatus);
            gui.waitMatches("Unknown", gui.status);
            gui.requireEnabled(false, false, false);
            FerretControl.setStatus("Ticket");
            gui.requireEnabled(true, false, true);
            FerretControl.setStatus("Stage");
            gui.requireEnabled(false, true, false);
            FerretControl.setStatus("DMA");
            gui.requireEnabled(false, true, true);
            FerretControl.setStatus("Inactive");
            gui.requireEnabled(false, false, false);
            FerretControl.setStatus("Reject");
            gui.requireEnabled(false, false, false);
        }

        [Test]
        public void testModeChangeChallenges() {
            var gui = new FakeFerretControlGui();
            var control = new FerretControl(gui, ferretStatus);
            var messages = new List<Fields>();
            FerretControl.onOutgoing(messages.Add);
            control.setRandomSeed(12345);
            control.changeStatus("Stage");
            AreEqual("Stage", gui.status());
            gui.stageAnswer(YesNoCancel.YES);
            control.onTicketPressed();
            gui.hasMessage("Are you not not sure?");
            gui.waitMatches(1, () => messages.Count);
            messages.Clear();
            control.setReadonly(true);
            gui.stageAnswer(YesNoCancel.NO);
            control.onTicketPressed();
            gui.hasMessage("Are you not not sure?", "incorrect");
            control.setReadonly(false);

            control.changeStatus("Ticket");
            AreEqual("Ticket", gui.status());
            gui.stageAnswer(YesNoCancel.YES);
            control.onDMAPressed();
            gui.hasMessage("Are you not not not not sure?");
            gui.waitMatches(1, () => messages.Count);
            messages.Clear();
            control.setReadonly(true);
            control.onDMAPressed();
            gui.hasMessage("Are you not not not sure?", "incorrect");
            control.setReadonly(false);
            control.onStagePressed();
            gui.noMessage();
            gui.waitMatches(1, () => messages.Count);
            messages.Clear();
            control.changeStatus("DMA");
            control.onStagePressed();
            gui.noMessage();
            gui.waitMatches(1, () => messages.Count);
            messages.Clear();
            control.onTicketPressed();
            gui.noMessage();
            gui.waitMatches(1, () => messages.Count);
        }

        public override void setUp() {
            base.setUp();
            FerretControl.setBroker(JMSTestCase.TEST_BROKER2);
            ferretStatus = FerretControl.incomingStatus();
        }

        public override void tearDown() {
            OrderTable.prefix = OrderTable.DEFAULT_PREFIX;
            base.tearDown();
        }
    }
}
