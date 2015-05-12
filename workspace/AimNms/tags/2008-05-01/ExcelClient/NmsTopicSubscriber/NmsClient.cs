using System;
using System.Collections;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;
using Apache.NMS;
using Apache.NMS.ActiveMQ;
using Excel = Microsoft.Office.Interop.Excel;

namespace NmsRtdClient
{
    public class NmsClient
    {
        private static readonly IList listeningToTopics = new ArrayList();
        private static readonly Object lockObject = new object();

        private static string[] AccountAdminFields = { "lastUpdateTime", "batchCount", "onlineCount", "securityCount" };

        //private static readonly log4net.ILog log = log4net.LogManager.GetLogger("RtdClientApp.Logging");
        //log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

        private static Boolean connected;

        private static IConnection connection;
        private static ISession session;

        private static Boolean initialized;
        private readonly String[] baseFields;

        private static DateTime lastUpdateTime;

        private static DateTime heartBeatTime;

        private readonly String brokerUrl;

        /// <summary>
        /// Mapping from topic/field to Excel topicID
        /// </summary>
        private IDictionary topicNameToTopicID = new Hashtable();
        /// <summary>
        /// Mapping of Excel topicID to topic/field
        /// </summary>
        private IDictionary topicIDToTopicName = new Hashtable();

        /// <summary>
        /// The changed value for the Excel topicID
        /// </summary>
        private IDictionary changedTopics = new Hashtable();

        private DateTime startUpTime;
        private int startupCount = 0;
        // we use this during startup - we want to close it once we get all the messages

        private readonly Excel.IRTDUpdateEvent m_xlRTDUpdate;
        private IMessageConsumer batchConsumer;
        private Boolean running;

        public NmsClient(Excel.IRTDUpdateEvent xlRTDUpdate, String[] keyBaseFields, String brokerUrl)
        {
            baseFields = keyBaseFields;
            m_xlRTDUpdate = xlRTDUpdate;
            this.brokerUrl = brokerUrl;
            heartBeatTime = DateTime.Now;

            //log.Info("About to connect to ActiveMQ");
            // Each instance of the Excel RTD creates a new COM instance, since we are using static
            // fields to store the data, we do not need to re-load the data for each instance
            // ensure that we only initialize once
            lock (lockObject)
            {
                if (!initialized)
                {
                    initializeConnection();
                }
            }
        }

        public Boolean isConnected()
        {
            return connected;
        }

        private void initializeConnection()
        {
            try
            {
                IConnectionFactory factory = new ConnectionFactory(new Uri(brokerUrl));
                connection = factory.CreateConnection();

                connected = true;

                connection.ExceptionListener += this.OnException;

                //log.Info("Created a connection to " + brokerUrl);

                session = connection.CreateSession();
                initialized = true;
            }
            catch (Exception e)
            {
                // Nothing to do here.  If we don't catch this, Excel dies
               // log.Error(e);
            }
        }

        public void shutdown()
        {
            lock (lockObject)
            {
                running = false;
            }

            if (connection != null)
            {
                connection.ExceptionListener -= this.OnException;
                if (session != null)
                {
                    session.Close();
                }

                connection.Close();
            }
        }

        public void StartListeningTo(string topicName, string recordKey, string fieldName, int topicID)
        {
            // Ensure we only listen to the topic once - this allows clients to call 
            // us multiple times
            lock (lockObject)
            {
                // Add the recordKey/fieldName to topicID mapping and start subscribing
                TopicKey topicFieldKey = new TopicKey(recordKey, fieldName);

                if (!topicNameToTopicID.Contains(topicFieldKey))
                {
                    topicNameToTopicID.Add(topicFieldKey, topicID);
                    topicIDToTopicName.Add(topicID, topicFieldKey);
                }

                if (!listeningToTopics.Contains(topicName))
                {
                    if (createTopicListener(topicName))
                    {
                        listeningToTopics.Add(topicName);
                    }
                }
            }
        }


        public void RemoveTopic(int topicID)
        {
            if (topicIDToTopicName.Contains(topicID))
            {
                TopicKey topicFieldKey = (TopicKey)topicIDToTopicName[topicID];

                topicNameToTopicID.Remove(topicFieldKey);
                topicIDToTopicName.Remove(topicID);
            }
        }

        private Boolean createTopicListener(string topicName)
        {
            if (connected)
            {
                String localTopicName = "position." + topicName;
                IDestination topic = session.GetTopic(localTopicName);

                // create a consumer 
                IMessageConsumer consumer = session.CreateConsumer(topic);
                consumer.Listener += new MessageListener(this.OnlineMessage);

                // connect to the 'command' queue and issue a request for batch positions
                // need to send a conduit so that we can receive the data
                IQueue commandQueue = session.GetQueue("position.Command");
                IMessageProducer commandProducer = session.CreateProducer(commandQueue);

                ITemporaryQueue responseQueue = session.CreateTemporaryQueue();
                batchConsumer = session.CreateConsumer(responseQueue);
                batchConsumer.Listener += this.StartupMessageLoad;

                ITextMessage textCommandMessage = session.CreateTextMessage("SendBatchAndOnline account=" + topicName);
                textCommandMessage.NMSReplyTo = responseQueue;

                commandProducer.Send(textCommandMessage);
                return true;
            }
            else
            {
                return false;
            }
        }

        public String lookupValue(String key, String fieldName)
        {
            // provide away to findout the brokerUrl/batchCount/onlineCount
            if (fieldName == "brokerUrl")
            {
                return brokerUrl;
            }
            else if (fieldName == "securityCount")
            {
                IDictionary book = new Hashtable();
                string startsWith = key.Split('-')[0];
                PositionCache.findAllOnlineFieldValues(startsWith, new String[] { "securityId", "level1TagName" }, book);
                PositionCache.findAllBatchFieldValues(startsWith, new String[] { "securityId", "level1TagName" }, book);

                return "" + book.Count;
            }

            else if (fieldName == "batchCount")
            {
                return "" + PositionCache.countBatchCacheItems(key.Split('-')[0]);
            }
            else if (fieldName == "onlineCount")
            {
                return "" + PositionCache.countOnlineCacheItems(key.Split('-')[0]);
            }
            else if (fieldName == "lastUpdateTime")
            {
                return lastUpdateTime.ToString();
            }
            else if (fieldName == "heartBeat")
            {
                return (heartBeatTime == null) ? "" : heartBeatTime.ToString();
            }
            // We don't know what cache this item is in, check the online first, then batch
            String value = PositionCache.lookupOnlineCacheValue(key, fieldName);
            if (value == null)
            {
                value = PositionCache.lookupBatchCacheValue(key, fieldName);
            }

            if (value != null)
            {
                return value;
            }

            return "N/A";
        }

        public void NMSReaderLoop()
        {
            connection.Start();

            running = true;
            // Wait forever
            while (running)
            {
                //log.Debug("[NSM-" + Thread.CurrentThread.ManagedThreadId + "] Total entries in onlineCache=" +onlineCache.Count);
                //log.Debug("[NSM-" + Thread.CurrentThread.ManagedThreadId + "] Total entries in batchCache =" +batchCache.Count);
                Thread.Sleep(6*1000);
            }
        }

        private String BuildPositionKey(IDictionary record)
        {
            String key = "";
            int count = 0;
            foreach (String keyBaseField in baseFields)
            {
                count++;

                if (record.Contains(keyBaseField))
                {
                    key = key + record[keyBaseField];
                    if (count < baseFields.Length)
                    {
                        key = key + "-";
                    }
                }
            }
            return key;
        }

        public String BuildPositionLookupKey(String rtdString, String valueField)
        {
            IDictionary record = extractRtdString(rtdString);
            string positionKey = BuildPositionKey(record);

            if (valueField == "lastUpdateTime" || valueField == "batchCount" || valueField == "onlineCount")
            {
                return positionKey + valueField;
            }
            else if (valueField == "brokerUrl" || valueField == "heartBeat")
            {
                return valueField;
            }

            return positionKey;
        }


        public IDictionary ConsumeChangedTopics()
        {
            IDictionary consumedChanges = null;
            lock (lockObject)
            {
                consumedChanges = changedTopics;
                changedTopics = new Hashtable();
            }

            // Add the admin fields
            /*
            changedKeyList.Add("brokerUrl");
            changedKeyList.Add("batchCount");
            changedKeyList.Add("onlineCount");
            changedKeyList.Add("lastUpdateTime");
            changedKeyList.Add("heartBeat");
            changedKeyList.Add("securityCount");
            */

            return consumedChanges;
        }

        private static IDictionary extractRtdString(String rtdString)
        {
            // Store the message parts into the cache
            Regex pairRegex = new Regex(@"\|");
            Regex keyValueRegex = new Regex(@"=");

            String[] parts = pairRegex.Split(rtdString);

            IDictionary record = new Hashtable();

            foreach (string token in parts)
            {
                // a token is a key/value pair separated by '='
                // Take each token and split into the key/value
                String[] keyValue = keyValueRegex.Split(token);
                record.Add(keyValue[0], keyValue[1]);
            }
            return record;
        }

        public string extractJmsTopic(string rtdString)
        {
            IDictionary record = extractRtdString(rtdString);

            return (string)record["account"];
        }

        private static IDictionary ExtractPositionRecord(string rawString)
        {
            // Store the message parts into the cache
            Regex pairRegex = new Regex(@"\|");
            Regex keyValueRegex = new Regex(@"=");

            String[] parts = pairRegex.Split(rawString);

            IDictionary record = new Hashtable();

            foreach (string token in parts)
            {
                try
                {
                    // a token is a key/value pair separated by '='
                    // Take each token and split into the key/value
                    String[] keyValue = keyValueRegex.Split(token);
                    record.Add(keyValue[0], keyValue[1]);
                }
                catch (IndexOutOfRangeException e)
                {
                    Console.WriteLine(e.StackTrace);
                }
            }

            return record;
        }

        protected void OnException(Exception exception)
        {
            //log.Error("Exception on NMS connection", exception);
            // need to reconnect
            lock (lockObject)
            {
                connected = false;
                initialized = false;
                listeningToTopics.Clear();
                initializeConnection();

                foreach (object o in listeningToTopics)
                {
                    createTopicListener((String) o);
                }
            }
        }


        protected void OnlineMessage(IMessage message)
        {
                // We need to build a list of updates, this can then be used by the
                // RTD server interface to only send back topics that have changed.
                DateTime now = DateTime.Now;

                ITextMessage textMessage = message as ITextMessage;
                IDictionary record = ExtractPositionRecord(textMessage.Text);
                record.Add("receiveTime", now.ToString());

                // For each record build the cacheKey - the key would be configured
                string key = BuildPositionKey(record);

                // store each value
                string messageType = (string)record["messageType"];

                if ("BatchPosition".Equals(messageType))
                {
                    PositionCache.addBatchCacheItem(key, record);
                    lastUpdateTime = now;
                    RegisterChange(key, record);
                }
                else if ("OnlinePosition".Equals(messageType))
                {
                    PositionCache.addOnlineCacheItem(key, record);
                    lastUpdateTime = now;
                    RegisterChange(key, record);
                }
                else if ("HeartBeat".Equals(messageType))
                {
                    heartBeatTime = DateTime.Now;
                    RegisterGenericChange("heartBeat", heartBeatTime.ToString());
                }
                else if ("Command".Equals(messageType) && record["purgeRecordType"] != null)
                {
                    PositionCache.clearCaches();
                    lastUpdateTime = now;
                    return;
                }
                else
                {
                    return;
                }

                if (m_xlRTDUpdate != null)
                {
                    m_xlRTDUpdate.UpdateNotify();
                }
        }

        private void RegisterGenericChange(string fieldName, string fieldValue)
        {
            TopicKey topicFieldKey = new TopicKey(fieldName, fieldName);

            if (topicNameToTopicID.Contains(topicFieldKey))
            {
                int topicID = (int)topicNameToTopicID[topicFieldKey];
                // remove incase it is already present
                changedTopics.Remove(topicID);
                changedTopics.Add(topicID, fieldValue);
            }
        }

        private void RegisterChange(string topicKey, IDictionary parsedMessage)
        {
            // for each topic and record, add them to the list of changed topics
            lock (lockObject)
            {
                foreach (string fieldName in parsedMessage.Keys)
                {
                    TopicKey topicFieldKey = new TopicKey(topicKey, fieldName);

                    if (topicNameToTopicID.Contains(topicFieldKey))
                    {
                        int topicID = (int)topicNameToTopicID[topicFieldKey];
                        // remove incase it is already present
                        changedTopics.Remove(topicID);
                        changedTopics.Add(topicID, parsedMessage[fieldName]);
                    }
                }
            }
            // add in the admin fields
            AddAdminFieldUpdates(topicKey, parsedMessage);
        }

        private void AddAdminFieldUpdates(string topicKey, IDictionary parsedMessage)
        {
            string accountPair = "account="+parsedMessage["account"];

            foreach (string fieldName in AccountAdminFields)
            {
                string recordKey = BuildPositionLookupKey(accountPair, fieldName);

                TopicKey topicFieldKey = new TopicKey(recordKey, fieldName);

                if (topicNameToTopicID.Contains(topicFieldKey))
                {
                    int topicID = (int)topicNameToTopicID[topicFieldKey];
                    // remove incase it is already present
                    changedTopics.Remove(topicID);
                    changedTopics.Add(topicID, lookupValue(topicKey, fieldName));
                }
            }
        }

        protected void StartupMessageLoad(IMessage message)
        {
                // We need to build a list of updates, this can then be used by the
                // RTD server interface to only send back topics that have changed.
                DateTime now = DateTime.Now;
                lastUpdateTime = now;
                if (startUpTime == null)
                    startUpTime = now;

                startupCount++;

                ITextMessage textMessage = message as ITextMessage;
                IDictionary record = ExtractPositionRecord(textMessage.Text);
                record.Add("receiveTime", now.ToString());

                // For each record build the cacheKey - the key would be configured
                string key = BuildPositionKey(record);

                // store each value
                string messageType = (string)record["messageType"];

                if ("BatchPosition".Equals(messageType))
                {
                    PositionCache.addBatchCacheItem(key, record);
                    RegisterChange(key, record);
                }
                else if ("OnlinePosition".Equals(messageType))
                {
                    PositionCache.addOnlineCacheItem(key, record);
                    RegisterChange(key, record);
                }
                else if ("Command".Equals(messageType))
                {
                    if (record.Contains("purgeRecordType") && record["purgeRecordType"] != null)
                    {
                        PositionCache.clearCaches();
                        return;
                    }
                    else if (record.Contains("endOfMessages") && record["endOfMessages"] != null)
                    {
                        RegisterGenericChange("brokerUrl", brokerUrl);
                        // Ensure we update on the last message
                        if (m_xlRTDUpdate != null)
                        {
                            m_xlRTDUpdate.UpdateNotify();
                        }

                        // The queue is complete, disconnect.
                        if (batchConsumer != null)
                        {
                            //batchConsumer.Close(); // free up some resources
                        }
                        return;
                    }
                    else
                    {
                        return; // unknown record type
                    }
                }
                else
                {
                    return; // unknown command
                }

                //We update at the end too
                if (m_xlRTDUpdate != null)
                {
                    m_xlRTDUpdate.UpdateNotify();
                }
        }
    }

    internal class TopicKey
    {
        public TopicKey(string topic, string field)
        {
            topicName = topic;
            fieldName = field;
        }

        public string topicName;
        public string fieldName;

        public override bool Equals(Object other)
        {
            TopicKey otherKey = other as TopicKey;

            return topicName == otherKey.topicName && fieldName == otherKey.fieldName;
        }

        public override int GetHashCode()
        {
            return topicName.GetHashCode() * 17 + fieldName.GetHashCode();
        }
    }
}