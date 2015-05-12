using System;
using System.Collections.Generic;
using jms;
using Q.Messaging;
using Q.Trading;
using Q.Util;
using systemdb.data;
using systemdb.metadata;
using util;
using Channel=Q.Messaging.Channel;
using Random=System.Random;
using Date= java.util.Date;

namespace Q.Recon {

    public interface FerretControlGui : QGUI {
        void setStatus(string newStatus);
        void setEnabled(bool dma, bool ticket, bool staged);
    }

    public class FerretControl : Util.Objects {        
        public static readonly List<string> RELEASEABLE = list("Ticket", "DMA");
        readonly FerretControlGui gui;
        Random random = new Random();
        string lastStatus = "Unknown";

        public FerretControl(FerretControlGui gui, Channel status) {
            this.gui = gui;
            status.subscribe(fields => changeStatus(fields.text("FERRETSTATE")));
        }

        internal void setRandomSeed(int seed) {
            random = new Random(seed);
        }

        internal void changeStatus(string newStatus) {
            gui.setStatus(newStatus);
            if(newStatus.Equals("Inactive")) gui.setEnabled(false, false, false);
            else if(newStatus.Equals("Reject")) gui.setEnabled(false, false, false);
            else if(newStatus.Equals("Stage")) gui.setEnabled(false, true, false);
            else if(newStatus.Equals("Ticket")) gui.setEnabled(true, false, true);
            else if(newStatus.Equals("DMA")) gui.setEnabled(false, true, true);
            else gui.setEnabled(false, false, true); // even if we don't know what state it's in, we can still bring it back to safe mode.
            lastStatus = newStatus;
        }

        public void onTicketPressed() {
            onButtonPressed("Ticket");
        }

        public void onButtonPressed(string state) {
            if ("DMA".Equals(state) || ("Ticket".Equals(state) && (lastStatus.Equals("Stage")))) {
                var numNots = random.Next(2, 5);
                var answer = gui.askUser("Are you " + join(" ", nCopies(numNots, "not")) + " sure?");
                if (answer == YesNoCancel.CANCEL) return;
                var correct = numNots % 2 == 0 ? YesNoCancel.YES : YesNoCancel.NO;
                if (answer != correct) {
                    gui.alertUser("incorrect answer!  please try again when you sober up.");
                    return;
                }
            }
            requestFerretChange(state);
        }

        public static void requestFerretChange(string state) {
            var fields = new Fields();
            fields.put("MESSAGETYPE", "FerretMode");
            fields.put("FERRETSTATE", state);
            outgoing().send(fields);
        }

        public void onDMAPressed() {
            onButtonPressed("DMA");
        }

        static string broker_ = "tcp://amqfersrv:61600";
        static readonly Lazy<QQueue> outgoing_ = new Lazy<QQueue>(() => new QQueue("FER.Command", broker_));
        static readonly Lazy<Topic> incomingStatus_ = new Lazy<Topic>(() => { var topic = new Topic("FER.State", broker()); topic.subscribeIfNeeded(); return topic; });

        internal static void setBroker(string newBroker) {
            broker_ = newBroker;
        }
        static string broker() {
            return broker_;
        }
        public static Topic incomingStatus() {
             return incomingStatus_;
        }

        public static QQueue outgoing() {
            return outgoing_;   
        }

        public static Topic incomingResponses(Date date, string id) {
            return new Topic("FER.Order.Response." + Dates.asLong(date) + "." + id, broker_);
        }

        public static void onIncomingOrderResponse(LiveOrders.LiveOrder o, Action<Fields> action) {
            incomingResponses(o.submittedTime(), o.ferretOrderId()).subscribe(action);
        }

        public static void onOutgoing(Action<Fields> action) {
            outgoing().register(Channel.ActionListener.receiver(action));
        }

        public void setReadonly(bool beReadonly) {
            outgoing().setReadonly(beReadonly);
        }

        public void onStagePressed() {
            onButtonPressed("Stage");
        }

        public static void clearLazys() {
            outgoing_.clear();
            incomingStatus_.clear();
        }

        public static void setStatus(string mode) {
            var topic = incomingStatus();
            topic.send("FERRETSTATE", mode);
            DbTestCase.waitMatches(mode, status);
        }

        public static string status() {
            var topic = incomingStatus();
            return topic.has("FERRETSTATE") ? topic.get<string>("FERRETSTATE") : "Unknown";
        }

        public static bool canRelease() {
            return RELEASEABLE.Contains(status());
        }

        public static void release(string ferretId, Date orderDate) {
            var fields = new Fields();
            fields.put("MESSAGETYPE", "ReleaseStagedOrder");
            fields.put("ORDERDATE", ferretDate(Dates.midnight(orderDate)));
            fields.put("USERORDERID", ferretId);
            outgoing().send(fields);
        }

        public static string ferretDate(Date date) {
            return Dates.yyyyMmDd(date).Replace('/', '-');
        }

        public static void setUpForTest() {
              setBroker(JMSTestCase.TEST_BROKER2);
              clearLazys();
        }

        public static void cancel(string ferretId, Date orderDate) {
            new Order.OrderSubmission(ferretId, orderDate).sendCancelToFerret();
        }
    }
}