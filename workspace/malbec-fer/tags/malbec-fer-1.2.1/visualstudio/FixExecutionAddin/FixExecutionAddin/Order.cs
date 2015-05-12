using System;
using System.Collections.Generic;
using System.Globalization;
using System.Security.Principal;

namespace FixExecutionAddin {
    public class Order {
        const decimal NOT_SET = -1;

        internal string fixMessageType = "D";

        internal string exchange;
        internal decimal limitPrice;

        internal string orderType;
        internal string platform;
        internal decimal quantity;
        internal string replyTo;

        internal string securityIdSource;

        internal string securityType;
        internal string side;
        internal decimal stopPrice;
        internal string symbol;
        internal string timeInForce;
        readonly string account;
        internal string strategy;

        internal string clientHostname;
        internal string clientUserId;
        internal string clientAppName;

        // TODO what do we do with this???
        //internal string clientOrderId;
        internal string userOrderId;
        internal DateTime? orderDate;
        readonly string status;
        internal DateTime transactionTime;
        internal string errorMsg;


        /// <summary>
        /// Any fields that we don't know about will be passed in here.
        /// This should allow us to add functionality on the server without forcing a client upgrade.
        /// </summary>
        readonly IDictionary<string, string> unknownFields = new Dictionary<string, string>();

        protected internal Order() {
            quantity = -1;
            limitPrice = -1;
            stopPrice = -1;
            //securityIDSource = "5";
            // OrderDate must now be supplied (Bloomberg)
            //orderDate = DateTime.Now;
            transactionTime = DateTime.Now;
            timeInForce = "DAY";
            status = "NEW";
            clientHostname = Environment.MachineName;

            var currentUser = WindowsIdentity.GetCurrent();
            if (currentUser != null) {
                clientUserId = currentUser.Name;
            }

            clientAppName = "EXCEL";
        }

        internal Order(IEnumerable<KeyValuePair<string, string>> fields) : this() {

            foreach (var entry in fields) {
                var key = entry.Key.ToUpper();

                switch (key) {
                    case "TIMEINFORCE":
                        timeInForce = UpperCaseOrNull(entry.Value);
                        break;
                    case "ORDERTYPE":
                        orderType = UpperCaseOrNull(entry.Value);
                        break;
                    case "SIDE":
                        side = UpperCaseOrNull(entry.Value);
                        break;
                    case "LIMITPRICE":
                        limitPrice = decimalOrNull(entry.Value);
                        break;
                    case "STOPPRICE":
                        stopPrice = decimalOrNull(entry.Value);
                        break;
//                    case "CLIENTORDERID":
//                        clientOrderId = UpperCaseOrNull(entry.Value);
//                        break;
                    case "USERORDERID":
                        userOrderId = UpperCaseOrNull(entry.Value);
                        break;
                    case "QUANTITY":
                        quantity = decimalOrNull(entry.Value);
                        break;
                    case "SYMBOL":
                        symbol = UpperCaseOrNull(entry.Value);
                        break;
                    case "PLATFORM":
                        platform = UpperCaseOrNull(entry.Value);
                        break;
                    case "EXCHANGE":
                        exchange = UpperCaseOrNull(entry.Value);
                        break;
                    case "SECURITYTYPE":
                        securityType = UpperCaseOrNull(entry.Value);
                        break;
                    case "SECURITYIDSOURCE":
                        securityIdSource = UpperCaseOrNull(entry.Value);
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
// do not over-write this
//                    case "CLIENTHOSTNAME":
//                        clientHostname = entry.Value;
//                        break;
// do not over-write this
//                    case "CLIENTUSERID":
//                        clientUserID = entry.Value;
//                        break;
// do not over-write this
//                    case "CLIENTAPPNAME":
//                        clientAppName = entry.Value;
//                        break;
                    case "STATUS":
                        status = entry.Value;
                        break;
                    case "ACCOUNT":
                        account = entry.Value;
                        break;
                    case "STRATEGY":
                        strategy = entry.Value;
                        break;
                    case "FIXMESSAGETYPE":
                        fixMessageType = entry.Value;
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

        private static decimal decimalOrNull(string value) {
            return String.IsNullOrEmpty(value) ? NOT_SET : Convert.ToDecimal(value);
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

        public string UserOrderId {
            get { return UserOrderId; }
            set {
                if (string.IsNullOrEmpty(value)) userOrderId = null;
                else {
                    var orginalLength = value.Length;

                    value = value.Replace(" ", "");
                    value = value.Replace("%", "");
                    var maxLength = Math.Min(6, value.Length);
                    ModifiedUserOrderId = maxLength != orginalLength;
                    userOrderId = value.Substring(0, maxLength);
                }
            }
        }

        public bool ModifiedUserOrderId { get; set; }

        public bool CanCalculateCompositOrderId { 
            get {
                return (!ModifiedUserOrderId && orderDate != null);
            } 
        }

        public bool IsCancelReplace {
            get { return (fixMessageType == "G"); }
        }

        /// <summary>
        /// This is to be integrated into the Bloomberg ClientOrderId logic TODO 
        /// </summary>
        public string CacheKey {
            get {
//                var middle = "-0";
//                if (IsCancelReplace) {
//                    middle = "-1";
//                }
//                return string.Format(@"{0:yyyyMMdd}", orderDate) + middle + userOrderId;
                return userOrderId;
            }
        }

        public string FixMessageType {
            get {
                return fixMessageType;
            }
            set { fixMessageType = value;
            }
        }
        #endregion


        public IDictionary<string, string> ToDictionary() {
            var fields = new Dictionary<string, string> {
                {"TRANSACTIONTIME", transactionTime.ToString()},
            };

            if (userOrderId != null) fields.Add("USERORDERID", userOrderId);
            if (orderDate != null) fields.Add("ORDERDATE", string.Format(@"{0:yyyy-MM-dd}", orderDate));
            //if (userOrderId != null && orderDate != null) fields.Add("CLIENTORDERID", CompositeOrderId);

            if (timeInForce != null) fields.Add("TIMEINFORCE", timeInForce);
            if (orderType != null) fields.Add("ORDERTYPE", orderType);
            if (side != null) fields.Add("SIDE", side);
            if (limitPrice != NOT_SET) fields.Add("LIMITPRICE", limitPrice.ToString(CultureInfo.InvariantCulture));
            if (stopPrice != NOT_SET) fields.Add("STOPPRICE", stopPrice.ToString(CultureInfo.InvariantCulture));
            
            

            if (quantity != NOT_SET) fields.Add("QUANTITY", quantity.ToString(CultureInfo.InvariantCulture));
            if (symbol != null) fields.Add("SYMBOL", symbol);
            if (platform != null) fields.Add("PLATFORM", platform);
            if (exchange != null) fields.Add("EXCHANGE", exchange);
            if (securityType != null) fields.Add("SECURITYTYPE", securityType);
            if (securityIdSource != null) fields.Add("SECURITYIDSOURCE", securityIdSource);
            if (replyTo != null) fields.Add("REPLYTO", replyTo);

            if (clientHostname != null) fields.Add("CLIENTHOSTNAME", clientHostname);
            if (clientUserId != null) fields.Add("CLIENTUSERID", clientUserId);
            if (clientAppName != null) fields.Add("CLIENTAPPNAME", clientAppName);
            if (status != null) fields.Add("STATUS", status);

            //if (errorMsg != null) fields.Add("ERROR_1", errorMsg);
            // Add the null value so that we can get merge to work
            fields.Add("ERROR_1", errorMsg);

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
        static string UpperCaseOrNull(string value)
        {
            return String.IsNullOrEmpty(value) ? null : value.ToUpper();
        }
        #endregion


        public string ToSting() {
            return "UserOrderId=" + userOrderId +", CompositOrderId=" + CacheKey + ", Quantity=" + quantity + ", Side=" + side;
        }
    }
}