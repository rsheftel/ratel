using System;
using System.Collections.Generic;
using System.Xml;

namespace TradingScreenApiService.Util
{
    public static class MessageUtil
    {
        static readonly log4net.ILog _log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

        const string Equity = "Equity";
        const string Futures = "Futures";
        /// <summary>
        /// XML attribute to KVP mapping
        /// </summary>
        static readonly IDictionary<string, string> _equityFields = new Dictionary<string, string>();
        static readonly IDictionary<string, string> _futuresFields = new Dictionary<string, string>();

        static MessageUtil() {
            _equityFields["Instrument"] = "RIC";
            _equityFields["ExecutedQuantity"] = "ExecutedQuantity";
            _equityFields["ExecutedValue"] = "ExecutedValue";
            _equityFields["ETI"] = "ClearingBroker";
            _equityFields["TSId"] = "PlatformOrderId";
            _equityFields["Side"] = "Side";
            _equityFields["Exchange"] = "Exchange";
            _equityFields["HandlingComment"] = "Strategy";

            //_futuresFields["Instrument"] = "RIC";
            _futuresFields = _equityFields;

        }
        /// <summary>
        /// Extract the Equity and Futures orders from the XML document into key/value pairs.
        /// 
        /// This only extracts the fields that we require, not all the available data.
        /// </summary>
        /// <param name="xmlSource"></param>
        /// <param name="xmlInstanceId"></param>
        /// <returns></returns>
        public static IList<IDictionary<string, string>> ParseXmlToDictionary(string xmlSource, int xmlInstanceId) {
            if (xmlSource != null) {
                var xmlDoc = new XmlDocument(); 
                xmlDoc.LoadXml(xmlSource);
                var equityOrders = xmlDoc.GetElementsByTagName("EquityOrder");
                var equityOrderList = ExtractOrders(equityOrders, _equityFields, Equity);

                var futuresOrders = xmlDoc.GetElementsByTagName("FutureOrder");
                var futuresOrderList = ExtractOrders(futuresOrders, _futuresFields, Futures);

               var allOrders = new List<IDictionary<string, string>>();
               allOrders.AddRange(equityOrderList);
               allOrders.AddRange(futuresOrderList);

               return allOrders;
            }

            return new List<IDictionary<string, string>>();
        }

        static IList<IDictionary<string, string>> ExtractOrders(XmlNodeList orderNodeList, IEnumerable<KeyValuePair<string, string>> mappings, string securityType) {
            IList<IDictionary<string, string>> orderList = new List<IDictionary<string, string>>();

            foreach (XmlNode node in orderNodeList) {
                var order = new Dictionary<string, string>();
                _log.Info(node.OuterXml);
                // if State =='TradingComplete' the order is done, ignore (DFD record?)
                var orderState = node.Attributes["State"];
                //if (orderState.Value == "TradingComplete") {
                if (orderState.Value != "Executed") {
                    _log.Info("Skipping order with state of " + orderState.Value);
                    continue;
                }
                foreach (var mapping in mappings) {
                    var attribute = node.Attributes[mapping.Key];
                    var value = attribute.Value.Trim();
                    if ("0E-8" == value) {
                        value = "0";
                    }
                    order[mapping.Value] = value;
                }
                if (order.Count <= 0) continue;
                order["SecurityType"] = securityType;
                orderList.Add(order);
            }

            return orderList;
        }

        public static IDictionary<string, string> CreateTradeRecord(string symbol, string side, int quantity, double price, string strategy) {
            var tradeRecord = new Dictionary<string, string> {
                {"BID", symbol},
                {"Side", side},
                {"Quantity", Convert.ToString(quantity)},
                {"Price", Convert.ToString(price)},
                {"Strategy", strategy}
            };

            return tradeRecord;
        }

        public static bool IsEquityOrder(IDictionary<string, string> order) {
            return order["SecurityType"] == Equity;
        }

        public static bool IsFuturesOrder(IDictionary<string, string> order)
        {
            return order["SecurityType"] == Futures;
        }

        public static IDictionary<string, string >CreateTradeRecord(IDictionary<string, string> order) {
            var tradeRecord = new Dictionary<string, string> {
                {"BID", order["BID"]},
                {"Side", order["Side"]},
                {"Quantity", order["ExecutedQuantity"]},
                {"Price", order["ExecutedValue"]},
                {"Strategy", order["Strategy"]}
            };

            return tradeRecord;
        }
    }
}
