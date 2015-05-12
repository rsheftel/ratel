using System;
using System.Globalization; 
using System.Reflection;
using System.Runtime.InteropServices;
using System.Text;
using Microsoft.Win32;
using System.Collections.Generic;
using Microsoft.Office.Interop.Excel;
using ActiveMQLibrary;

namespace ActiveMQExcel
{
    [ComVisible(true)]
    [GuidAttribute("456C274B-A375-4371-953B-46EFF97CFC2C")]
    public interface IPublisher {
        object AMQPub(string topicName, object fieldNames, object fieldValues);
        object AMQPubBroker(string topicName, object fieldNames, object fieldValues, string brokerUrl);
    }

    [Guid("55DA90F3-8BB4-421d-9242-A162006D6B1C")]
    [ClassInterface(ClassInterfaceType.None)]
    [ComVisible(true)]
    public class ActiveMQPublisher : IPublisher
    {
        readonly log4net.ILog _log = log4net.LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);

        private readonly IDictionary<string, string> _publishedValueCache = new Dictionary<string, string>();

        readonly AppConfiguration _config;

        public ActiveMQPublisher(AppConfiguration configuration)
        {
            _config = configuration;
        }

        public ActiveMQPublisher() : this(AppConfiguration.Load()) {}

        #region COM registration
        /// <summary>
        /// Register the COM object when we run regasm.  This does not get called when
        /// using a setup project.  We must manually add these registrations to the 
        /// project.
        /// 
        /// </summary>
        /// <param name="type"></param>
        [ComRegisterFunctionAttribute]
        public static void RegisterFunction(Type type)
        {
            Registry.ClassesRoot.CreateSubKey(GetSubKeyName(type, "Programmable"));
            var key = Registry.ClassesRoot.OpenSubKey(GetSubKeyName(type, "InprocServer32"), true);
            if (key != null) key.SetValue("", Environment.SystemDirectory + @"\mscoree.dll", RegistryValueKind.String);
        }

        [ComUnregisterFunctionAttribute]
        public static void UnregisterFunction(Type type)
        {
            Registry.ClassesRoot.DeleteSubKey(GetSubKeyName(type, "Programmable"), false);
        }



        private static string GetSubKeyName(Type type, string subKeyName)
        {
            var s = new StringBuilder();
            s.Append(@"CLSID\{");
            s.Append(type.GUID.ToString().ToUpper(CultureInfo.InvariantCulture));
            s.Append(@"}\");
            s.Append(subKeyName);

            return s.ToString();
        }
        #endregion


        #region UDFs
        /// <summary>
        /// 
        /// </summary>
        /// <param name="topicName"></param>
        /// <param name="fieldNames"></param>
        /// <param name="fieldValues"></param>
        /// <returns></returns>
        public object AMQPub(string topicName, object fieldNames, object fieldValues) {
            return AMQPubBroker(topicName, fieldNames, fieldValues, _config.MarketDataBrokerUrl);
        }
        /// <summary>
        /// Publish the values to the specified topic.
        /// 
        /// </summary>
        /// <param name="topicName"></param>
        /// <param name="fieldNames"></param>
        /// <param name="fieldValues"></param>
        /// <param name="brokerUrl"></param>
        /// <returns></returns>
        [STAThread]
        public object AMQPubBroker(string topicName, object fieldNames, object fieldValues, string brokerUrl)
        //public object AMQPub(string topicName, params object[] parameters)
        {
            // Wrap this in a big try/catch since this is the API exposed to Excel
            try {
                // Make sure that we have a topic
                if (string.IsNullOrEmpty(topicName) || topicName.Trim().Length == 0) {
                    return "#Topic";
                }

                if (string.IsNullOrEmpty(brokerUrl)) {
                    return "#NoBrokerUrl";
                }

                var fieldNameRange = fieldNames as Range;
                var fieldValueRange = fieldValues as Range;

                // passed in non range values - should be simple string
                if (fieldNameRange == null || fieldValueRange == null) {
                    return PublishValue(topicName, brokerUrl, fieldNames, fieldValues);
                }
                // Validate that the two ranges are the same size, if not return 
                // an error message of '#Range'
                if (fieldNameRange.Rows.Count != fieldValueRange.Rows.Count ||
                    fieldNameRange.Columns.Count != fieldValueRange.Columns.Count) {
                    return "#Range";
                }
                // only checking one, as they are the same size at this point
                if (fieldNameRange.Rows.Count == 1 && fieldNameRange.Columns.Count == 1) {
                    // a range of 1 - single cell
                    return PublishValue(topicName, brokerUrl, fieldNames, fieldValues);
                }
                return PublishRange(topicName, brokerUrl, fieldNameRange, fieldValueRange);
            } catch (Exception e) {
                _log.Error("Exception in publish function", e);
                return "#PublishError - " + e.Message;
            }
        }

        private static string GetCellValue(object cell) {
            var cellValue = cell as Range;
            return cellValue != null ? Convert.ToString(cellValue.Value2, CultureInfo.InvariantCulture) : Convert.ToString(cell, CultureInfo.InvariantCulture);
        }

        private static string GetCellText(object cell) {
            var cellText = cell as Range;
            return cellText != null ? Convert.ToString(cellText.Text, CultureInfo.InvariantCulture) : Convert.ToString(cell, CultureInfo.InvariantCulture);
        }

        private string PublishValue(string topicName, string brokerUrl, object fieldName, object fieldValue) {
            var name = GetCellValue(fieldName);
            var value = GetCellText(fieldValue);

            var recordChanged = UpdateCache(brokerUrl, topicName, name, value);

            if (!recordChanged) {
                return "No change - " + GenerateResponseTimestamp();
            }
            var message = new Dictionary<string, string> {{name, value}};

            return PublishRecord(topicName, brokerUrl, message);
        }

        private bool UpdateCache(string brokerUrl, string topicName, string name, string value)
        {
            var recordChanged = false;
            // only publish changes since the last time we received this cell
            var cacheKey = brokerUrl + "-" + topicName + "-" + name;
            if (_publishedValueCache.ContainsKey(cacheKey)) {
                var cacheValue = _publishedValueCache[cacheKey];
                if (cacheValue != value) {
                    _publishedValueCache[cacheKey] = value;
                    recordChanged = true;
                }
            } else {
                _publishedValueCache.Add(cacheKey, value);
                recordChanged = true;
            }
            return recordChanged;
        }

        private string PublishRange(string topicName, string brokerUrl, Range fieldNameRange, Range fieldValueRange)
        {
            var rowCount = fieldNameRange.Rows.Count;
            var columnCount = fieldNameRange.Columns.Count;
            var recordFieldNames = new List<string>(rowCount * columnCount + 2);

            // Put everything into an Map, if we have duplicate keys, return an error message 
            // and do not publish
            // Create a message from this request.  A message is name/value pairs separated by '|'
            var recordChanged = false;

            var names = ConvertToObjectArray(fieldNameRange, rowCount, columnCount);
            var values = ConvertToObjectArray(fieldValueRange, rowCount, columnCount);

            // Excel starts indexes at 1 NOT 0
            var message = new Dictionary<string, string>();
            for (var i = 1; i <= rowCount; i++) {
                for (var j = 1; j <= columnCount; j++) {
                    // Using the faster version
                    var name = Convert.ToString(names[i, j], CultureInfo.InvariantCulture);
                    var value = Convert.ToString(values[i, j], CultureInfo.InvariantCulture);

                    if (recordFieldNames.Contains(name)) {
                        return "#Non-Unique Record " + name;
                    }
                    recordFieldNames.Add(name);
                    recordChanged = recordChanged || UpdateCache(brokerUrl, topicName, name, value);
                    message.Add(name, value);
                }
            }

            if (!recordChanged) {
                return "No change - " + GenerateResponseTimestamp();
            }

            return PublishRecord(topicName, brokerUrl, message);
        }

        private static object[,] ConvertToObjectArray(Range range, int rowCount, int columnCount)
        {
            var array = range.get_Value(XlRangeValueDataType.xlRangeValueDefault) as object[,];
            return array ?? new object[rowCount,columnCount];
        }
        private static string PublishRecord(string topicName, string brokerUrl, IDictionary<string, string> message)
        {
            // Add some administration values
            message.Add(TextMessageUtil.TopicKeyName, topicName);
            message.Add(TextMessageUtil.TimestampKeyName,GenerateRecordTimestamp());

            var broker = BrokerFactory.Broker(brokerUrl);
            var publisher = broker.TopicPublisher(topicName);
            publisher.Publish(topicName, message);

            // Use a thread here so we can tell (hopefully) when the broker is down.
            //if (!Connected) return "#PublishError - disconnected";
            return "Submitted " + GenerateResponseTimestamp();
        }
        #endregion

        private static string GenerateResponseTimestamp()
        {
            return DateTime.Now.ToString("T", DateTimeFormatInfo.InvariantInfo);
        }

        private static string GenerateRecordTimestamp()
        {
            var timestamp = DateTime.Now;

            var timestampStr = timestamp.ToString("yyyy/MM/dd HH:mm:ss.SSS zzz", DateTimeFormatInfo.InvariantInfo);

            // Use '0' as a placeholder and ensure we have 3 digits
            var millis = String.Format(CultureInfo.InvariantCulture, "{0:000}", timestamp.Millisecond);
            return timestampStr.Replace("SSS", millis);
        }

        /// <summary>
        /// This touches one of the cells that we take in so that Excel knows that 
        /// we touched them.  If we don't Excel thinks we don't require the field 
        /// cell to change.
        /// 
        /// </summary>
        /// <returns></returns>
        private static void TouchCell(Range fieldNameRange, Range fieldValueRange) {
            ConvertToObjectArray(fieldNameRange, 0, 0);
            ConvertToObjectArray(fieldValueRange, 0, 0);
        }
    }

}
