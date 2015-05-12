using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Apache.NMS;
using FixExecutionAddin.util;

namespace FixExecutionAddin.nms
{
    public class NmsClientApp : INmsApplication, IConnectable
    {
        string clientName;
        readonly NmsSession nmsSession;
        readonly IDictionary<string, Order> orders = new Dictionary<string, Order>();
        readonly IList<IDictionary<string, string>> receivedMessages = new List<IDictionary<string, string>>();
        IList<string> updatedOrders = new List<string>();

        public NmsClientApp(string clientName)
        {
            this.clientName = clientName;
            nmsSession = new NmsSession(this);
        }

        public int OrderCacheCount {
            get { return orders.Count; }
        }
        public int ReceivedMessageCount {
            get { return receivedMessages.Count; }
        }

        public void Configure(string brokerUrl, string consumerQueue, string producerQueue)
        {
            nmsSession.BrokerUrl = brokerUrl;
            nmsSession.ConsumerQueue = consumerQueue;
            nmsSession.ProducerQueue = producerQueue;
        }

        public void Start()
        {
            nmsSession.Start();
        }

        public bool Connected()
        {
            return nmsSession.Connected;
        }

        public void Stop()
        {
            nmsSession.Stop();
        }

        public string SendOrder(Order order) {
            var textMessage = nmsSession.SendMessageResponse(order.ToDictionary());
            AddOrder(order);

            return textMessage.NMSMessageId;
        }

        /// <summary>
        /// Process inbound messages.
        /// 
        /// Take the message and turn them into Orders.  Ensure that our local Order cache is updated
        /// with the latest status.
        /// 
        /// </summary>
        /// <param name="message"></param>
        public void InboundApp(IMessage message)
        {
            try {
                var textMessage = message as ITextMessage;
                if (textMessage != null) {
                    var appMessage = MessageUtil.ExtractRecord(textMessage.Text);
                    MessageUtil.AddOriginalMessageID(textMessage.Properties["JmsOriginalMessageID"] as string, appMessage);
                    MessageUtil.AddMessageID(textMessage.NMSMessageId, appMessage);

                    // TODO We need to purge these at some time
                    lock (receivedMessages) {
                        receivedMessages.Add(appMessage);
                        //Console.WriteLine("Stored response for " + textMessage.Properties["JmsOriginalMessageID"]);
                    }
                    lock (orders) {
                        var receivedOrder = new Order(appMessage);
                        var clientOrderID = receivedOrder.ClientOrderID.ToUpper();
                        var cachedOrder = GetOrderByClientOrderID(clientOrderID);

                        // We already sent the order and it was accepted (persisted)
                        if (cachedOrder != null) {
                            if (cachedOrder.Status == "NEW" && receivedOrder.Status == "INVALID") {
                                //RemoveOrder(cachedOrder);
                                AddOrder(cachedOrder.MergeWith(receivedOrder));
                            } else {
                                AddOrder(cachedOrder.MergeWith(receivedOrder));
                            }
                        } else {
                            AddOrder(receivedOrder);
                        }
                        if (UpdatedOrder != null) {
                            UpdatedOrder(clientOrderID);
                        }
                        
                    }
                }
            } catch (Exception e) {
                Console.WriteLine(e);
            }
        }



        public void OutboundApp(IMessage message)
        {
            throw new NotImplementedException();
        }

        public IDictionary<string, string> GetResponseFor(string id)
        {
            return GetResponseFor(id, false);
        }

        public IDictionary<string, string> GetResponseFor(string id, bool remove)
        {
            lock (receivedMessages) {
                id = id.ToUpper();
                foreach (var message in receivedMessages) {
                    var originalMessageID = MessageUtil.GetOriginalMessageID(id, message);
                    if (String.IsNullOrEmpty(originalMessageID) || id != originalMessageID.ToUpper()) continue;
                    if (remove) {
                        receivedMessages.Remove(message);
                    }

                    return message;
                }
            }
            return null;
        }

        void RemoveOrder(Order orderToRemove)
        {
            lock (orders) {
                var id = orderToRemove.ClientOrderID.ToUpper();
                orders.Remove(id);
                updatedOrders.Remove(id);
                updatedOrders.Add(id);
            }
        }

        public void AddOrder(Order orderToAdd) {
            lock (orders) {
                var id = orderToAdd.ClientOrderID.ToUpper();
                orders[id] = orderToAdd;
                // keep track of the orders that have updated
                updatedOrders.Remove(id);
                updatedOrders.Add(id);
            }
        }

        public Order GetOrderByClientOrderID(string clientOrderID)
        {
            var id = clientOrderID.ToUpper();
            lock (orders) {
                return orders.ContainsKey(id) ? orders[id] : null;
            }
        }

        public IList<string> ConsumeUpdatedOrders() {
            lock(orders) {
                var ordersToReturn = updatedOrders;
                updatedOrders = new List<string>();
                return ordersToReturn;
            }
        }
        #region Events
        public delegate void UpdatedOrderEventHandler(string clientOrderID);

        public event UpdatedOrderEventHandler UpdatedOrder;
        #endregion

    }
}
