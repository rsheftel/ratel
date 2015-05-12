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
    public class OrderSubscriberRTD : IRtdServer
    {
        private static readonly log4net.ILog log = log4net.LogManager.GetLogger("Console");

        IRTDUpdateEvent xlRTDUpdateCallbackHandler;
        NmsClientApp nmsClient;
        readonly IDictionary<string, int> orderToTopicMapping = new Dictionary<string, int>();
        int lastHeartbeat;

        //private readonly object lockObject = new object();

        static OrderSubscriberRTD() {
            var configurationFileName = System.Reflection.Assembly.GetExecutingAssembly().Location + ".config";
            log4net.Config.XmlConfigurator.Configure(new System.IO.FileInfo(configurationFileName));
        }

        #region RTD
        /// <summary>
        /// Startup the RTD server to listen to the Broker.
        /// </summary>
        /// <param name="callbackObject"></param>
        /// <returns></returns>
        int IRtdServer.ServerStart(IRTDUpdateEvent callbackObject) {
            try {
                xlRTDUpdateCallbackHandler = callbackObject;
                nmsClient = NmsClientFactory.GetClientFor(Configuration.BrokerUrl);

                if (!nmsClient.Connected()) {
                    nmsClient.Start();
                    // Sleep to give the connection time to startup
                    Thread.Sleep(500);
                }
                nmsClient.UpdatedOrder += ReceivedOrder;

                lastHeartbeat = 1;
                return 1;
            } catch(Exception e) {
                log.Error(e);
                return -1;
            }
        }

        /// <summary>
        /// We have been added to a spreadsheet cell.
        /// </summary>
        /// <param name="topicID"></param>
        /// <param name="parameters">
        /// We are expecting one (1) parameter that represents the UserOrderID and OrderDate
        /// </param>
        /// <param name="getNewValues"></param>
        /// <returns></returns>
        object IRtdServer.ConnectData(int topicID, ref Array parameters, ref bool getNewValues) {
            try {
                getNewValues = true; // over-write any saved values in the spreadsheet
                var queryStr = (parameters.GetValue(0)).ToString();
                var queryParts = MessageUtil.ExtractRecord(queryStr);
                
                var queryOrder = new Order(queryParts);

                if (!queryOrder.CanCalculateCompositOrderId) {
                    return "#Error: UserOrderId/OrderDate do not meet requirements";
                }

                lock (nmsClient) {
                    var cacheKey = queryOrder.CacheKey;
                    orderToTopicMapping[cacheKey] = topicID;

                    var order = nmsClient.GetOrderByKey(cacheKey);
                    if (order == null) {
                        // Give us a chance to connect, but don't waste too much time
                        if (!nmsClient.Connected()) {
                            Thread.Sleep(1000);
                        }

                        if (nmsClient.Connected()) {
                            // query the system for the order
                            nmsClient.QueryOrder(queryOrder.userOrderId, (DateTime) queryOrder.orderDate);

                            return "Unknown order - sent query";
                        }
                        return "#Warn: Not connected to broker, try again";
                    }
                    return GetErrorOrStatus(order);
                }
            } catch (Exception e) {
                //log.Error(e);
                return "#Error looking up order status:" + e.Message;
            }
        }

        /// <summary>
        /// Remove the topicID from the list of mapped CompositeOrderIds.
        /// </summary>
        /// <param name="topicID"></param>
        void IRtdServer.DisconnectData(int topicID)
        {
            try {
                lock (nmsClient) {
                    foreach (var pair in orderToTopicMapping) {
                        if (pair.Value != topicID) continue;
                        orderToTopicMapping.Remove(pair);
                        return;
                    }
                }
            } catch (Exception e) {
                //log.Error(e);
                Console.WriteLine(e);
            }
        }

        Array IRtdServer.RefreshData(ref int topicCount) {
            try {
                lock (nmsClient) {
                    var updatedOrders = nmsClient.ConsumeUpdatedOrders();
               
                    var updatedTopics = new object[2,updatedOrders.Count];
                    var i = 0;
                    foreach (var s in updatedOrders) {
                        var order = nmsClient.GetOrderByKey(s);
                        updatedTopics[0, i] = orderToTopicMapping[s];
                        updatedTopics[1, i] = GetErrorOrStatus(order);
                        i++;
                    }
                    topicCount = i;
                    return updatedTopics;
                }
            } catch(Exception e) {
                log.Error(e);
                return new object[2, 0];
            }
        }

        static string GetErrorOrStatus(Order order) {
            if (order != null && string.IsNullOrEmpty(order.ErrorMessage)) {
               return order.Status;
            }
            return order != null ? order.ErrorMessage : "Failed to find order";
        }

        int IRtdServer.Heartbeat() {
            try {
                if (lastHeartbeat != 1 && !nmsClient.Connected()) {
                    return lastHeartbeat;
                }
                if (lastHeartbeat == 1 && !nmsClient.Connected()){
                    // We will wait for two bad heart beats to restart
                    lastHeartbeat = -1;
                    return 1;
                }
                // we are working find, reset the heartbeat value
                lastHeartbeat = 1;

            } catch(Exception e) {
                log.Error(e);
                // don't know what happened, but should restart
                lastHeartbeat = -1;
            }
            return lastHeartbeat;
        }

        /// <summary>
        /// Stop the nms client as we are shutting down.
        /// 
        /// If we are still publishing, the publishier will restart, so we are O.K.
        /// </summary>
        void IRtdServer.ServerTerminate() {
            try {
                nmsClient.Stop();
            } catch (Exception e) {
                //log.Error(e);
                Console.Write(e);
            }
        }
        #endregion

        #region Events
        public void ReceivedOrder(string compositeOrderId) {
            lock (nmsClient) {
                // We have the changed orders being tracked by the NmsClientApp, we could do it here instead.
                xlRTDUpdateCallbackHandler.UpdateNotify();
            }
        }
        #endregion
    }
}
