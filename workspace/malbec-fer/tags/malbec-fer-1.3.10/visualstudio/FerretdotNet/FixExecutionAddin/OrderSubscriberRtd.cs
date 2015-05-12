using System;
using System.Collections.Generic;

using System.Runtime.InteropServices;
using System.Threading;
using FixExecutionAddin.Nms;
using FixExecutionAddin.Util;
using Microsoft.Office.Interop.Excel;

namespace FixExecutionAddin
{
    [ComVisible(true), ProgId("RtdOrder")]
    public class OrderSubscriberRtd : IRtdServer
    {
        private static readonly log4net.ILog _log = log4net.LogManager.GetLogger("Console");

        readonly AppConfiguration _config;

        IRTDUpdateEvent _xlRtdUpdateCallbackHandler;
        NmsClientApp _nmsClient;
        readonly IDictionary<string, int> _orderToTopicMapping = new Dictionary<string, int>();
        int _lastHeartbeat;

        //private readonly object lockObject = new object();
        public OrderSubscriberRtd() {
            _config = AppConfiguration.Load();
        }

        internal OrderSubscriberRtd(AppConfiguration config)
        {
            _config = config;
        }

        public int TopicCount {
            get {
                return _orderToTopicMapping.Count;
            }
        }
        public bool Connected {
            get { return _nmsClient.Connected(); }
        }

//        static OrderSubscriberRtd() {
//            var configurationFileName = System.Reflection.Assembly.GetExecutingAssembly().Location + ".config";
//            log4net.Config.XmlConfigurator.Configure(new System.IO.FileInfo(configurationFileName));
//        }

        #region RTD
        /// <summary>
        /// Startup the RTD server to listen to the Broker.
        /// </summary>
        /// <param name="callbackObject"></param>
        /// <returns></returns>
        public int ServerStart(IRTDUpdateEvent callbackObject) {
            try {
                _xlRtdUpdateCallbackHandler = callbackObject;
                _nmsClient = NmsClientFactory.GetClientFor(_config.BrokerUrl);

                if (!_nmsClient.Connected()) {
                    _nmsClient.Start();
                    // Sleep to give the connection time to startup
                    Thread.Sleep(500);
                }
                _nmsClient.UpdatedOrder += ReceivedOrder;

                _lastHeartbeat = 1;
                return 1;
            } catch(Exception e) {
                _log.Error(e);
                return -1;
            }
        }

        /// <summary>
        /// We have been added to a spreadsheet cell.
        /// </summary>
        /// <param name="topicId"></param>
        /// <param name="parameters">
        /// We are expecting one (1) parameter that represents the UserOrderID and OrderDate
        /// </param>
        /// <param name="getNewValues"></param>
        /// <returns></returns>
        public object ConnectData(int topicId, ref Array parameters, ref bool getNewValues) {
            try {
                getNewValues = true; // over-write any saved values in the spreadsheet
                var queryStr = (parameters.GetValue(0)).ToString();
                var queryParts = MessageUtil.ExtractRecord(queryStr);
                
                var queryOrder = new Order(queryParts);

                if (!queryOrder.CanCalculateCompositOrderId) {
                    return "#Error: UserOrderId/OrderDate do not meet requirements";
                }

                lock (_nmsClient) {
                    var cacheKey = queryOrder.CacheKey;
                    _orderToTopicMapping[cacheKey] = topicId;

                    var order = _nmsClient.GetOrderByKey(cacheKey);
                    if (order == null) {
                        // Give us a chance to connect, but don't waste too much time
                        if (!_nmsClient.Connected()) {
                            Thread.Sleep(1000);
                        }

                        if (_nmsClient.Connected()) {
                            // query the system for the order
                            _nmsClient.QueryOrder(queryOrder.userOrderId, (DateTime) queryOrder.orderDate);

                            return "Unknown order - sent query";
                        }
                        return "#Warn: Not connected to broker, try again";
                    }
                    return GetErrorOrStatus(order);
                }
            } catch (Exception e) {
                //_log.Error(e);
                return "#Error looking up order status:" + e.Message;
            }
        }

        /// <summary>
        /// Remove the topicId from the list of mapped CompositeOrderIds.
        /// </summary>
        /// <param name="topicId"></param>
        public void DisconnectData(int topicId)
        {
            try {
                lock (_nmsClient) {
                    foreach (var pair in _orderToTopicMapping) {
                        if (pair.Value != topicId) continue;
                        _orderToTopicMapping.Remove(pair);
                        return;
                    }
                }
            } catch (Exception e) {
                //_log.Error(e);
                Console.WriteLine(e);
            }
        }

        public Array RefreshData(ref int topicCount) {
            try {
                lock (_nmsClient) {
                    var updatedOrders = _nmsClient.ConsumeUpdatedOrders();
               
                    var updatedTopics = new object[2,updatedOrders.Count];
                    var i = 0;
                    foreach (var s in updatedOrders) {
                        var order = _nmsClient.GetOrderByKey(s);
                        updatedTopics[0, i] = _orderToTopicMapping[s];
                        updatedTopics[1, i] = GetErrorOrStatus(order);
                        i++;
                    }
                    topicCount = i;
                    return updatedTopics;
                }
            } catch(Exception e) {
                _log.Error(e);
                return new object[2, 0];
            }
        }

        static string GetErrorOrStatus(Order order) {
            if (order != null && string.IsNullOrEmpty(order.ErrorMessage)) {
               return order.Status;
            }
            return order != null ? order.ErrorMessage : "Failed to find order";
        }

        public int Heartbeat() {
            try {
                if (_lastHeartbeat != 1 && !_nmsClient.Connected()) {
                    return _lastHeartbeat;
                }
                if (_lastHeartbeat == 1 && !_nmsClient.Connected()){
                    // We will wait for two bad heart beats to restart
                    _lastHeartbeat = -1;
                    return 1;
                }
                // we are working find, reset the heartbeat value
                _lastHeartbeat = 1;

            } catch(Exception e) {
                _log.Error(e);
                // don't know what happened, but should restart
                _lastHeartbeat = -1;
            }
            return _lastHeartbeat;
        }

        /// <summary>
        /// Stop the nms client as we are shutting down.
        /// 
        /// If we are still publishing, the publishier will restart, so we are O.K.
        /// </summary>
        public void ServerTerminate() {
            try {
                _nmsClient.Stop();
            } catch (Exception e) {
                //_log.Error(e);
                Console.Write(e);
            }
        }
        #endregion

        #region Events
        void ReceivedOrder(string compositeOrderId) {
            lock (_nmsClient) {
                // We have the changed orders being tracked by the NmsClientApp, we could do it here instead.
                _xlRtdUpdateCallbackHandler.UpdateNotify();
            }
        }
        #endregion
    }
}
