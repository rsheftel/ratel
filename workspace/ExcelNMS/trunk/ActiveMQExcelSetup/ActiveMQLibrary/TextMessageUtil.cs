using System;
using System.Collections.Generic;
using System.Globalization; 
using System.Text;
using System.Text.RegularExpressions;


namespace ActiveMQLibrary
{
    public class TextMessageUtil
    {
        private static readonly log4net.ILog log = log4net.LogManager.GetLogger("PublisherApp.Logging");

        private static readonly Regex pairRegex = new Regex(@"\|");  // this needs to be escaped to work as a regex
        private static readonly Regex keyValueRegex = new Regex(@"=");
        public const string TopicKeyName = "MSTopicName";
        public const string TimestampKeyName = "MSTimestamp";


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
            var parts = pairRegex.Split(rawString);

            IDictionary<string, string> record = new Dictionary<string, string>();

            foreach (var token in parts) {
                try {
                    // a token is a key/value pair separated by '='
                    // Take each token and split into the key/value
                    var keyValue = keyValueRegex.Split(token);
                    if (keyValue.Length > 1) {
                        var key = keyValue[0];
                        if (record.ContainsKey(key)) {
                            record[key] = keyValue[1];
                        } else {
                            record.Add(key, keyValue[1]);
                        }
                    }
                } catch (Exception e) {
                    // when we have messed-up messages, the parsing fails
                    // try to get as much as possible
                    log.Error(e.Message);
                }
            }

            return record;
        }

        public static string CreateMessage(IDictionary<string, string> message)
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
        public static string CreateMessage(string topic, string fieldName, string fieldValue)
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


    }
}
