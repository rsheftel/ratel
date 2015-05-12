using System;
using System.Collections.Generic;
using System.Globalization;
using System.Security.Principal;
using System.Text;
using System.Text.RegularExpressions;

namespace FixExecutionAddin.Util
{
    public static class MessageUtil
    {
        private static readonly log4net.ILog _log = log4net.LogManager.GetLogger("Console");

        private static readonly Regex _pairRegex = new Regex(@"\|");  // this needs to be escaped to work as a regex
        private static readonly Regex _keyValueRegex = new Regex(@"=");
        const string TopicKeyName = "MSTopicName";
        const string TimestampKeyName = "MSTimestamp";
        const string OriginalMessageId = "JmsOriginalMessageID";
        const string MessageId = "JmsMessageID";
        const string MessageType = "MessageType";
        const string NewOrder = "NewOrder";
        const string QueryOrder = "QueryOrder";
        const string CancelOrder = "CancelOrder";
        const string ReplaceOrder = "ReplaceOrder";
        const string ClientHostname = "CLIENTHOSTNAME";
        const string ClientUserId = "CLIENTUSERID";


        /// <summary>
        /// Given a string that is in the 'field_name1'='field_value1'|'field_name2'='field_value2' format,
        /// return a IDictionary with the field_name2 and field_name2 as keys, and the respective values.
        /// 
        /// In the event that the string is not completely parsable, the return record will have as much
        /// of the string that was parsed before the error.
        /// </summary>
        /// <param name="rawString"></param>
        /// <returns></returns>
        public static IDictionary<string, string> ExtractRecord(string rawString)
        {
            return ExtractRecord(rawString, false);
        }

         public static IDictionary<string, string> ExtractRecord(string rawString, bool upperCaseKeys) {
             var parts = _pairRegex.Split(rawString);

             IDictionary<string, string> record = new Dictionary<string, string>();

             foreach (var token in parts) {
                 try {
                     // a token is a key/value pair separated by '='
                     // Take each token and split into the key/value
                     var keyValue = _keyValueRegex.Split(token);
                     if (keyValue.Length > 1) {
                         if (upperCaseKeys) {
                             record.Add(keyValue[0].ToUpperInvariant(), keyValue[1]);
                         } else {
                             record.Add(keyValue[0], keyValue[1]);
                         }
                     }
                 } catch (Exception e) {
                     // when we have messed-up messages, the parsing fails
                     // try to get as much as possible
                     _log.Warn("Tried to parse message string: " + rawString, e);
                 }
             }

             return record;
         }

        internal static string CreateMessage(IDictionary<string, string> message)
        {
            return CreateMessage(null, message);
        }

        /// <summary>
        /// Create a string from the key/value pair that can be sent via ActiveMQ.
        /// 
        /// This will tack on the timestamp that represents the time the messages was sent.
        /// In this case it is really the time the message was created, which should be close 
        /// enough.
        /// </summary>
        /// <param name="topicName"></param>
        /// <param name="message"></param>
        /// <returns></returns>
        public static string CreateMessage(string topicName, IDictionary<string, string> message)
        {
            var sb = new StringBuilder(1024);

            foreach (var kvp in message) {
                sb.Append(kvp.Key).Append("=").Append(kvp.Value).Append("|");
            }
            sb.Append(TimestampKeyName).Append("=").Append(GenerateRecordTimestamp());

            if (topicName != null) {
                sb.Append("|").Append(TopicKeyName).Append("=").Append(topicName);
            }

            return sb.ToString();
        }


        /// <summary>
        /// 
        /// </summary>
        /// <param name="topic"></param>
        /// <param name="fieldName"></param>
        /// <param name="fieldValue"></param>
        /// <returns></returns>
        internal static string CreateMessage(string topic, string fieldName, string fieldValue)
        {
            IDictionary<string, string> message = new Dictionary<string, string> {{fieldName, fieldValue}};

            return CreateMessage(topic, message);
        }

        /// <summary>
        /// Return the topic that this message was published on.  
        /// 
        /// </summary>
        /// <param name="messageRecord"></param>
        /// <returns>The topic the message was published to, otherwise an empty string.</returns>
        public static string GetTopic(IDictionary<string, string> messageRecord) {
            return messageRecord.ContainsKey(TopicKeyName) ? messageRecord[TopicKeyName] : "";
        }

        public static void AddTopic(string topic, IDictionary<string, string> message)
        {
            message[TopicKeyName] = topic;
        }

        public static void AddOriginalMessageId(string id, IDictionary<string, string> message) {
            message[OriginalMessageId] = id;
        }

        public static string GetOriginalMessageId(string id, IDictionary<string, string> message) {
            return message.ContainsKey(OriginalMessageId) ? message[OriginalMessageId] : null;
        }

        public static void AddMessageId(string id, IDictionary<string, string> message)
        {
            message[MessageId] = id;
        }

        /// <summary>
        /// Generate the timestamp that is used to track when messages are actually sent.
        /// </summary>
        /// <returns></returns>
        private static string GenerateRecordTimestamp()
        {
            var timestamp = DateTime.Now;

            var timestampStr = timestamp.ToString("yyyy/MM/dd HH:mm:ss.SSS zzz", DateTimeFormatInfo.InvariantInfo);

            // Use '0' as a placeholder and ensure we have 3 digits
            var millis = String.Format(CultureInfo.InvariantCulture, "{0:000}", timestamp.Millisecond);
            return timestampStr.Replace("SSS", millis);
        }

        public static void SetNewOrder(IDictionary<string, string> dictionary) {
            dictionary[MessageType] = NewOrder;
        }

        public static void SetQueryOrder(IDictionary<string, string> dictionary)
        {
            dictionary[MessageType] = QueryOrder;
        }

        public static void SetCancelOrder(IDictionary<string, string> dictionary)
        {
            dictionary[MessageType] = CancelOrder;
        }

        public static void SetReplaceOrder(IDictionary<string, string> dictionary)
        {
            dictionary[MessageType] = ReplaceOrder;
        }

        public static void SetClientHostname(IDictionary<string, string> dictionary) 
        {
            dictionary[ClientHostname] = Environment.MachineName;
        }

        public static void SetClientUserId(IDictionary<string, string> dictionary) 
        {
            var currentUser = WindowsIdentity.GetCurrent();
            dictionary[ClientUserId] = currentUser == null ? "Unknown User" : currentUser.Name;
        }
    }
}
