using System;
using System.Collections.Generic;
using System.Xml;

namespace TradingScreenApiService.Util
{
    public static class MessageUtil
    {
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

            _futuresFields["Instrument"] = "RIC";

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
                var equityOrderList = ExtractOrders(equityOrders, _equityFields, "Equity");

                var futuresOrders = xmlDoc.GetElementsByTagName("FutureOrder");
                var futuresOrderList = ExtractOrders(futuresOrders, _futuresFields, "Futures");

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

                // if State =='TradingComplete' the order is done, ignore (DFD record?)
                var orderState = node.Attributes["State"];
                if (orderState.Value == "TradingComplete") {
                    continue;
                }
                foreach (var mapping in mappings) {
                    var attribute = node.Attributes[mapping.Key];
                    order[mapping.Value] = attribute.Value;
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
    }
}
