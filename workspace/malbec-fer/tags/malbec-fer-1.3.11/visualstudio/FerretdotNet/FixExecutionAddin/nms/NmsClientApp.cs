using System;
using System.Collections.Generic;
using System.Globalization;
using Apache.NMS;
using FixExecutionAddin.Util;

namespace FixExecutionAddin.Nms
{
    #region Delegates
    public delegate void UpdatedOrderEventHandler(string compositeOrderId);
    #endregion

    public class NmsClientApp : INmsApplication, IConnectable
    {
        private static readonly log4net.ILog _log = log4net.LogManager.GetLogger("Console");

        string _clientName;
        readonly NmsSession _nmsSession;
        readonly IDictionary<string, Order> _orders = new Dictionary<string, Order>();
        readonly IList<IDictionary<string, string>> _receivedMessages = new List<IDictionary<string, string>>();
        IList<string> _updatedOrders = new List<string>();

        public NmsClientApp(string clientName)
        {
            _clientName = clientName;
            _nmsSession = new NmsSession(this);
        }

        public int OrderCacheCount {
            get { return _orders.Count; }
        }
        public int ReceivedMessageCount {
            get { return _receivedMessages.Count; }
        }

        public void Configure(string brokerUrl, string producerQueue)
        {
            _nmsSession.BrokerUrl = brokerUrl;
            _nmsSession.ProducerQueue = producerQueue;
        }

        public void Start()
        {
            _nmsSession.Start();
        }

        public bool Connected()
        {
            return _nmsSession.Connected;
        }

        public void Stop()
        {
            _nmsSession.Stop();
        }

        
        public string SendOrder(Order order) {
            var orderDictionary = order.ToDictionary();

            AddOrder(order);
            MessageUtil.SetNewOrder(orderDictionary);
            return _nmsSession.SendMessageResponse(orderDictionary).NMSMessageId;
        }

        public string SendCancel(Order cancelOrder) {
            var orderDictionary = cancelOrder.ToDictionary();
            AddOrder(cancelOrder);
            MessageUtil.SetCancelOrder(orderDictionary);

            return _nmsSession.SendMessageResponse(orderDictionary).NMSMessageId;
        }

        public string SendReplace(Order cancelOrder)
        {
            var orderDictionary = cancelOrder.ToDictionary();
            AddOrder(cancelOrder);
            MessageUtil.SetReplaceOrder(orderDictionary);

            return _nmsSession.SendMessageResponse(orderDictionary).NMSMessageId;
        }

        public string QueryOrder(string userOrderId, DateTime orderDate) {
            var orderDictionary = new Dictionary<string, string>();

            MessageUtil.SetQueryOrder(orderDictionary);
            orderDictionary["UserOrderId"] = userOrderId;
            orderDictionary["OrderDate"] =  string.Format(@"{0:yyyy-MM-dd}", orderDate);
            MessageUtil.SetClientUserId(orderDictionary);
            MessageUtil.SetClientHostname(orderDictionary);

            _nmsSession.SubscribeToTopic(string.Format(@"{0:yyyyMMdd}", orderDate));
            var textMessage = _nmsSession.SendMessageResponse(orderDictionary);

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
                    MessageUtil.AddOriginalMessageId(textMessage.Properties["JmsOriginalMessageID"] as string, appMessage);
                    MessageUtil.AddMessageId(textMessage.NMSMessageId, appMessage);

                    // TODO We need to purge these at some time
                    lock (_receivedMessages) {
                        _receivedMessages.Add(appMessage);
                        Console.WriteLine("Stored response for " + textMessage.Properties["JmsOriginalMessageID"]);
                    }

                    lock (this) {
                        var receivedOrder = new Order(appMessage);
                        var cacheKey = receivedOrder.CacheKey.ToUpper(CultureInfo.InvariantCulture);
                        var cachedOrder = GetOrderByKey(cacheKey);

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
                        if (UpdatedOrder != null && cacheKey != null) {
                            UpdatedOrder(cacheKey);
                        }
                        _log.Info("Updating order:" + cacheKey + " to " + receivedOrder.Status);
                    }
                 
                }
            } catch (Exception e) {
               _log.Error("Problem processing inbound message", e);
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
            lock (_receivedMessages) {
                id = id.ToUpper(CultureInfo.InvariantCulture);
                foreach (var message in _receivedMessages) {
                    var originalMessageId = MessageUtil.GetOriginalMessageId(id, message);
                    if (String.IsNullOrEmpty(originalMessageId) || id != originalMessageId.ToUpper(CultureInfo.InvariantCulture)) continue;
                    if (remove) {
                        _receivedMessages.Remove(message);
                    }

                    return message;
                }
            }
            return null;
        }

        public void AddOrder(Order orderToAdd) {
            lock (this) {
                var id = orderToAdd.CacheKey.ToUpper(CultureInfo.InvariantCulture);
                _orders[id] = orderToAdd;
                // keep track of the orders that have updated
                _updatedOrders.Remove(id);
                _updatedOrders.Add(id);
            }
        }

        public Order GetOrderByKey(string compositeOrderId)
        {
            var id = compositeOrderId.ToUpper(CultureInfo.InvariantCulture);
            lock (this) {
                return _orders.ContainsKey(id) ? _orders[id] : null;
            }
        }

        public IList<string> ConsumeUpdatedOrders() {
            lock(this) {
                var ordersToReturn = _updatedOrders;
                _updatedOrders = new List<string>();
                return ordersToReturn;
            }
        }

        #region Events
        public event UpdatedOrderEventHandler UpdatedOrder;
        #endregion
    }
}
