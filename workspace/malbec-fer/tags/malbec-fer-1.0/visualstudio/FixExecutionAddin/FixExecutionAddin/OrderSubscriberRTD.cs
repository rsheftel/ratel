using System;
using System.Collections.Generic;

using System.Runtime.InteropServices;
using System.Threading;
using FixExecutionAddin.nms;
using Microsoft.Office.Interop.Excel;

namespace FixExecutionAddin
{
    [ComVisible(true), ProgId("RtdOrder")]
    public class OrderSubscriberRTD : IRtdServer
    {
        IRTDUpdateEvent xlRTDUpdateCallbackHandler;
        NmsClientApp nmsClient;
        readonly IDictionary<string, int> orderToTopicMapping = new Dictionary<string, int>();
        int lastHeartbeat;

        #region RTD
        /// <summary>
        /// Startup the RTD server to listen to the Broker.
        /// </summary>
        /// <param name="callbackObject"></param>
        /// <returns></returns>
        int IRtdServer.ServerStart(IRTDUpdateEvent callbackObject) {
            try {
                xlRTDUpdateCallbackHandler = callbackObject;
                nmsClient = NmsClientFactory.Instance.getClientFor(Configuration.BrokerUrl);

                if (!nmsClient.Connected()) {
                    // Sleep to give the connection time to startup
                    Thread.Sleep(500);
                }
                nmsClient.UpdatedOrder += ReceivedOrder;

                lastHeartbeat = 1;
                return 1;
            } catch(Exception e) {
                Console.WriteLine(e);
                return -1;
            }
        }

        /// <summary>
        /// We have been added to a spreadsheet cell.
        /// </summary>
        /// <param name="topicID"></param>
        /// <param name="parameters">
        /// We are only expecting one (1) parameter, the ClientOrderID.
        /// </param>
        /// <param name="getNewValues"></param>
        /// <returns></returns>
        object IRtdServer.ConnectData(int topicID, ref Array parameters, ref bool getNewValues) {
            try {
                getNewValues = true; // over-write any saved values in the spreadsheet
                var clientOrderID = (parameters.GetValue(0)).ToString().ToUpper();

                lock (orderToTopicMapping) {
                    orderToTopicMapping[clientOrderID] = topicID;
                }

                var order = nmsClient.GetOrderByClientOrderID(clientOrderID);
                return order != null ? order.Status : "Unknown order";
            } catch (Exception e) {
                Console.WriteLine(e);
                return "#Error looking up order status:" + e.Message;
            }

        }

        /// <summary>
        /// Remove the topicID from the list of mapped clientOrderIDs.
        /// </summary>
        /// <param name="topicID"></param>
        void IRtdServer.DisconnectData(int topicID)
        {
            try {
                lock (orderToTopicMapping) {
                    foreach (var pair in orderToTopicMapping) {
                        if (pair.Value != topicID) continue;
                        orderToTopicMapping.Remove(pair);
                        return;
                    }
                }
            } catch (Exception e) {
                Console.WriteLine(e);
            }
        }

        Array IRtdServer.RefreshData(ref int topicCount) {
            try {
                var updatedOrders = nmsClient.ConsumeUpdatedOrders();

                lock (orderToTopicMapping) {
                    var updatedTopics = new object[2,updatedOrders.Count];
                    var i = 0;
                    foreach (var s in updatedOrders) {
                        var order = nmsClient.GetOrderByClientOrderID(s);
                        updatedTopics[0, i] = orderToTopicMapping[s];
                        if (order != null && string.IsNullOrEmpty(order.ErrorMessage)) {
                            updatedTopics[1, i] = order.Status;
                        } else {
                            updatedTopics[1, i] = order != null ? order.ErrorMessage : "Failed to find order";
                        }
                        i++;
                    }
                    topicCount = i;
                    return updatedTopics;
                }
            } catch(Exception e) {
                Console.WriteLine(e);
                return new object[2, 0];
            }
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
                Console.WriteLine(e);
                // don't know what happened, but should restart
                lastHeartbeat = -1;
            }
            return lastHeartbeat;
        }

        void IRtdServer.ServerTerminate() {
            // We could shutdown, but that would prevent publishing.  Is that what we want?
            // Do nothing for now
        }
        #endregion

        #region Events
        public void ReceivedOrder(string clientOrderID) {
            // We have the changed orders being tracked by the NmsClientApp, we could do it here instead.
            xlRTDUpdateCallbackHandler.UpdateNotify();
        }
        #endregion
    }
}
