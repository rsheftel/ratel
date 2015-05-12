using System;
using System.Diagnostics;
using System.Globalization;
using System.Runtime.InteropServices;
using System.Threading;
using FixExecutionAddin.nms;
using FixExecutionAddin.util;
using Microsoft.Win32;

namespace FixExecutionAddin {
    /// <summary>
    /// The Order publishing functions.  Only put public methods here that you want exposed.
    /// 
    /// </summary>
    [Guid("67175717-788C-4207-B4F9-89A8761E3030"), ClassInterface(ClassInterfaceType.AutoDual), ComVisible(true)]
    public class OrderPublisherAddin
    {
        readonly NmsClientApp nmsClient;

        public OrderPublisherAddin() {
            nmsClient = NmsClientFactory.Instance.getClientFor(Configuration.BrokerUrl);
            if (!nmsClient.Connected()) {
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
        public static void RegisterFunction(Type type) {
            Registry.ClassesRoot.CreateSubKey(GetSubKeyName(type, "Programmable"));
            var key = Registry.ClassesRoot.OpenSubKey(GetSubKeyName(type, "InprocServer32"), true);
            key.SetValue("", Environment.SystemDirectory + @"\mscoree.dll", RegistryValueKind.String);
        }

        [ComUnregisterFunction] public static void UnregisterFunction(Type type) {
            Registry.ClassesRoot.DeleteSubKey(GetSubKeyName(type, "Programmable"), false);
        }

        static string GetSubKeyName(Type type, string subKeyName) {
            var s = new System.Text.StringBuilder();
            s.Append(@"CLSID\{");
            s.Append(type.GUID.ToString().ToUpper());
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
            // We are public API called from Excel, ensure we catch all exceptions
            try {
                // turn the string into an order so that we can do some validation on it.
                var orderFields = MessageUtil.ExtractRecord(orderString);
                var order = new Order(orderFields);
                if (order.ModifiedClientID) {
                    return "#Error: ClientOrderID does not meet requirements.  Must be 16 or less, without spaces and %";
                }

                if (nmsClient.Connected()) {
                    var cachedOrder = nmsClient.GetOrderByClientOrderID(order.ClientOrderID);
                    if (cachedOrder != null && cachedOrder.Status != "INVALID") {
                        return "#Error - order already sent '"+ order.ClientOrderID +"'";
                    }

                    var nmsMessageID = nmsClient.SendOrder(order);
                    
                } else {
                    return "#Warn: Not connected to broker, try again";
                }

                return "Published "+ GenerateExcelTimestamp();
            } catch (Exception e) {
                Console.WriteLine(e);
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

                return "BROKERURL" == setting.ToUpper() ? Configuration.BrokerUrl : "";
            } catch (Exception e) {
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