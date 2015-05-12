using System;
using System.Globalization;
using System.Runtime.InteropServices;
using System.Threading;
using FixExecutionAddin.Nms;
using FixExecutionAddin.Util;
using Microsoft.Win32;


namespace FixExecutionAddin {
    /// <summary>
    /// The Order publishing functions.  Only put public methods here that you want exposed.
    /// 
    /// </summary>
    [Guid("67175717-788C-4207-B4F9-89A8761E3030"), ClassInterface(ClassInterfaceType.AutoDual), ComVisible(true)]
    public class OrderPublisherAddin
    {
        private static readonly log4net.ILog _log = log4net.LogManager.GetLogger("Console");

        internal readonly NmsClientApp _nmsClient;
        readonly AppConfiguration _config;


        static OrderPublisherAddin() {
            var configurationFileName = System.Reflection.Assembly.GetExecutingAssembly().Location + ".config";
            log4net.Config.XmlConfigurator.Configure(new System.IO.FileInfo(configurationFileName));
        }

        public OrderPublisherAddin() : this(AppConfiguration.Load()) {}

        public OrderPublisherAddin(AppConfiguration config) {
            _config = config;

            _nmsClient = NmsClientFactory.GetClientFor(_config.BrokerUrl);
            if (!_nmsClient.Connected()) {
                // Sleep to give the connection time to startup
                Thread.Sleep(500);
            }
        }

        #region COM registration
        /// <summary>
        /// Register the COM object when we run regasm.  This does not get called when
        /// using a setup project.  We must manually add these registrations to the 
        /// project.
        /// 
        /// </summary>
        /// <param name="type"></param>
        [ComRegisterFunction] 
        private static void RegisterFunction(Type type) {
            Registry.ClassesRoot.CreateSubKey(GetSubKeyName(type, "Programmable"));
            var key = Registry.ClassesRoot.OpenSubKey(GetSubKeyName(type, "InprocServer32"), true);
            if (key != null) key.SetValue("", Environment.SystemDirectory + @"\mscoree.dll", RegistryValueKind.String);
        }

        [ComUnregisterFunction] 
        private static void UnregisterFunction(Type type) {
            Registry.ClassesRoot.DeleteSubKey(GetSubKeyName(type, "Programmable"), false);
        }

        static string GetSubKeyName(Type type, string subKeyName) {
            var s = new System.Text.StringBuilder();
            s.Append(@"CLSID\{");
            s.Append(type.GUID.ToString().ToUpperInvariant());
            s.Append(@"}\");
            s.Append(subKeyName);

            return s.ToString();
        }
        #endregion
        
        #region UDFs
        /// <summary>
        /// Publish an order that is represented by a string of key/value pairs.
        /// </summary>
        /// <param name="orderString"></param>
        /// <returns></returns>
        public object Pub(string orderString) {
            return Pub(orderString, "NEW");
        }

        public object PubCancel(string orderString) 
        {
            return Pub(orderString, "CANCEL");
        }

        public object PubReplace(string orderString)
        {
            return Pub(orderString, "REPLACE");
        }

        private object Pub(string orderString, string messageType) {
            // We are public (well, we are common code to the public API) API called from Excel, ensure we catch all exceptions
            try {
                _log.Info(orderString);
                // turn the string into an order so that we can do some validation on it.
                var orderFields = MessageUtil.ExtractRecord(orderString, true);
                if (orderFields.Count == 0) {
                    return "#Warn: did not receive a proper order string. " + orderString;
                }
                // To work with Bloomberg EMSX we require the OrderDate and UserOrderId
                // ClientOrderId is nolonger valid
                if (orderFields.ContainsKey("CLIENTORDERID")) {
                    return "#Error: Specify UserOrderId and OrderDate instead of ClientOrderId";
                }

                var order = new Order(orderFields);
                if (order.ModifiedUserOrderId) {
                    return "#Error: UserOrderID does not meet requirements.  Must be 6 or less, without spaces and %";
                }
                if (!order.CanCalculateCompositOrderId) {
                    return "#Error: Missing OrderDate";
                }

                lock (_nmsClient) {
                    if (_nmsClient.Connected()) {
                        var cachedOrder = _nmsClient.GetOrderByKey(order.CacheKey);
                        // what kind of order are we?
                        switch (messageType) {
                            case "NEW":
                                if (cachedOrder != null && cachedOrder.Status != "INVALID" && cachedOrder.Status != "UNKNOWN") {
                                    return "#Error - order already sent '" + order.CacheKey + "'";
                                }
                                _nmsClient.SendOrder(order);
                                break;
                            case "CANCEL":
                                _nmsClient.SendCancel(order);
                                break;
                            case "REPLACE":
                                order.FixMessageType = "G";
                                _nmsClient.SendReplace(order);
                                break;
                            default:
                                return "#Error:  Unable to determine type of message to send.  '" + messageType + "'";
                        }
                        _log.Info("Sent order:" + order.ToSting());

                    } else {
                        _nmsClient.Start();
                        return "#Warn: Not connected to broker, try again";
                    }
                }
                return "Published " + GenerateExcelTimestamp();
            } catch (Exception e) {
                Console.WriteLine("Unable to publish" + e.Message);
                return "#Error: Unable to Publish: " + e.Message;
            }   
        }

        /// <summary>
        /// Return configuration properties.
        /// </summary>
        /// <param name="setting"></param>
        /// <returns></returns>
        public object PubConfig(string setting) {
            try {
                if (string.IsNullOrEmpty(setting)) return "";

                return "BROKERURL" == setting.ToUpperInvariant() ? _config.BrokerUrl : "";
            } catch (Exception e) {
                Console.WriteLine("Unable to return brokerUrl" + e.Message);
                return e.Message;
            }
        }
        #endregion


        #region helper functions
        private static string GenerateExcelTimestamp()
        {
            return DateTime.Now.ToString("T", DateTimeFormatInfo.InvariantInfo);
        }
        #endregion
    }
}