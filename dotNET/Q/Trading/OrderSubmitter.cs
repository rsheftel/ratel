using Q.Recon;
using Q.Util;
using systemdb.data;
using systemdb.metadata;

namespace Q.Trading {
    public class OrderSubmitter : Objects {
        readonly LiveSystem liveSystem;
        readonly string topicPrefix;
        bool isLive;
        
        readonly Lazy<string> tag;
        readonly Lazy<bool> ferretApproved;

        public OrderSubmitter(LiveSystem liveSystem, string topicPrefix) {
            this.liveSystem = liveSystem;
            this.topicPrefix = topicPrefix;
            tag = new Lazy<string>(liveSystem.bloombergTag);
            ferretApproved = new Lazy<bool>(liveSystem.autoExecuteTrades);
        }

        void submitAllOrders(System system) {
            each(system.allOrders(), tryOrderSubmission);
        }

        public void orderPlaced(Order order) {
            if (isLive) tryOrderSubmission(order);
        }

        public void goLive(System system) {
            isLive = true;
            submitAllOrders(system);
        }

        void tryOrderSubmission(Order order) {
            if (LiveTradeMonitor.inNoPublishMode() || !order.canSubmitToFerret()) return;
            if(ferretApproved)
                writeOrderToFerret(order);
            else info("skipping order submission - system is not enabled for ferret.");
        }

        void writeOrderToFerret(Order order) {
            var fields = new Fields();
            order.addToFerret(fields, this);
            fields.put("STRATEGY", tag.initializedValue());
            var liveId = order.submittedInsertDb(liveSystem, topicPrefix);
            order.ferretSubmission.setLiveOrderId(liveId);
            FerretControl.outgoing().send(fields);
        }
    }
}