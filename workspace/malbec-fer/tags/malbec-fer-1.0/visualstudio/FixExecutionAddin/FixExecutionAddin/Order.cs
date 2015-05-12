using System;
using System.Collections.Generic;

namespace FixExecutionAddin {
    public class Order {
        const decimal NOT_SET = -1;
        
        internal string exchange;
        internal decimal limitPrice;

        internal string orderType;
        internal string platform;
        internal decimal quantity;
        internal string replyTo;

        internal string securityIDSource;

        internal string securityType;
        internal string side;
        internal decimal stopPrice;
        internal string symbol;
        internal string timeInForce;
        readonly string account;
        readonly string strategy;

        internal string clientHostname;
        internal string clientOrderID;
        internal DateTime orderDate;
        readonly string status;
        internal DateTime transactionTime;
        internal string errorMsg;


        /// <summary>
        /// Any fields that we don't know about will be passed in here.
        /// This should allow us to add functionality on the server without forcing a client upgrade.
        /// </summary>
        readonly IDictionary<string, string> unknownFields = new Dictionary<string, string>();

        public Order() {
            quantity = -1;
            limitPrice = -1;
            stopPrice = -1;
            securityIDSource = "5";
            orderDate = DateTime.Now;
            transactionTime = DateTime.Now;
            timeInForce = "DAY";
            status = "NEW";
            clientHostname = Environment.MachineName;
        }

        internal Order(IEnumerable<KeyValuePair<string, string>> fields) : this() {

            foreach (var entry in fields) {
                var key = entry.Key.ToUpper();

                switch (key) {
                    case "TIMEINFORCE":
                        timeInForce = upperCaseOrNull(entry.Value);
                        break;
                    case "ORDERTYPE":
                        orderType = upperCaseOrNull(entry.Value);
                        break;
                    case "SIDE":
                        side = upperCaseOrNull(entry.Value);
                        break;
                    case "LIMITPRICE":
                        limitPrice = Convert.ToDecimal(entry.Value);
                        break;
                    case "STOPPRICE":
                        stopPrice = Convert.ToDecimal(entry.Value);
                        break;
                    case "CLIENTORDERID":
                        ClientOrderID = upperCaseOrNull(entry.Value);
                        break;
                    case "QUANTITY":
                        quantity = Convert.ToDecimal(entry.Value);
                        break;
                    case "SYMBOL":
                        symbol = upperCaseOrNull(entry.Value);
                        break;
                    case "PLATFORM":
                        platform = upperCaseOrNull(entry.Value);
                        break;
                    case "EXCHANGE":
                        exchange = upperCaseOrNull(entry.Value);
                        break;
                    case "SECURITYTYPE":
                        securityType = upperCaseOrNull(entry.Value);
                        break;
                    case "SECURITYIDSOURCE":
                        securityIDSource = upperCaseOrNull(entry.Value);
                        break;
                    case "REPLYTO":
                        replyTo = entry.Value;
                        break;
                    case "TRANSACTIONTIME":
                        transactionTime = Convert.ToDateTime(entry.Value);
                        break;
                    case "ORDERDATE":
                        orderDate = Convert.ToDateTime(entry.Value);
                        break;
                    case "CLIENTHOSTNAME":
                        clientHostname = entry.Value;
                        break;
                    case "STATUS":
                        status = entry.Value;
                        break;
                    case "ACCOUNT":
                        account = entry.Value;
                        break;
                    case "STRATEGY":
                        strategy = entry.Value;
                        break;
                    default:
                        if (key.StartsWith("ERROR_") && string.IsNullOrEmpty(errorMsg)) {
                            errorMsg = entry.Value;
                        } else if (key.StartsWith("ERROR_")) {
                            errorMsg = errorMsg +" " + entry.Value;
                        } else {
                            unknownFields[key] = entry.Value;
                        }
                        break;
                }
            }
        }
        #region properties
        public string ErrorMessage {
            get {
                return string.IsNullOrEmpty(errorMsg) ? status : errorMsg;
            }
        }

        public string Status
        {
            get
            {
                return status;
            }
        }

        public string ClientOrderID {
            get { return clientOrderID; }
            set {
                if (String.IsNullOrEmpty(value)) clientOrderID = null;
                else {
                    var orginalLength = value.Length;

                    value = value.Replace(" ", "");
                    value = value.Replace("%", "");
                    var maxLength = Math.Min(16, value.Length);
                    ModifiedClientID = maxLength != orginalLength;
                    clientOrderID = value.Substring(0, maxLength);
                }
            }
        }
        public bool ModifiedClientID { get; set; }
        #endregion


        public IDictionary<string, string> ToDictionary() {
            var fields = new Dictionary<string, string> {
                {"TRANSACTIONTIME", transactionTime.ToString()},
                {"ORDERDATE", orderDate.ToString()}
            };

            if (timeInForce != null) fields.Add("TIMEINFORCE", timeInForce);
            if (orderType != null) fields.Add("ORDERTYPE", orderType);
            if (side != null) fields.Add("SIDE", side);
            if (limitPrice != NOT_SET) fields.Add("LIMITPRICE", limitPrice.ToString());
            if (stopPrice != NOT_SET) fields.Add("STOPPRICE", stopPrice.ToString());
            if (clientOrderID != null) fields.Add("CLIENTORDERID", clientOrderID);
            if (quantity != NOT_SET) fields.Add("QUANTITY", quantity.ToString());
            if (symbol != null) fields.Add("SYMBOL", symbol);
            if (platform != null) fields.Add("PLATFORM", platform);
            if (exchange != null) fields.Add("EXCHANGE", exchange);
            if (securityType != null) fields.Add("SECURITYTYPE", securityType);
            if (securityIDSource != null) fields.Add("SECURITYIDSOURCE", securityIDSource);
            if (replyTo != null) fields.Add("REPLYTO", replyTo);

            if (clientHostname != null) fields.Add("CLIENTHOSTNAME", clientHostname);
            if (status != null) fields.Add("STATUS", status);
            if (errorMsg != null) fields.Add("ERROR_1", errorMsg);

            if (account != null) fields.Add("ACCOUNT", account);
            if (strategy != null) fields.Add("STRATEGY", strategy);


            if (unknownFields.Count > 0) {
                foreach (var pair in unknownFields) {
                    fields[pair.Key] = pair.Value;
                }
            }
            return fields;
        }

        public Order MergeWith(Order order) {
            var originalOrder = ToDictionary();
            var valuesToMerge = order.ToDictionary();

            foreach (var pair in valuesToMerge) {
                originalOrder[pair.Key] = pair.Value;
            }

            return new Order(originalOrder);
        }

        #region static methods
        static string upperCaseOrNull(string value)
        {
            return String.IsNullOrEmpty(value) ? null : value.ToUpper();
        }
        #endregion
    }
}