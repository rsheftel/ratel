using System;
using System.Collections;
using System.Text.RegularExpressions;
using System.Threading;
using Apache.NMS;
using Apache.NMS.ActiveMQ;
using Excel = Microsoft.Office.Interop.Excel;

namespace NmsRtdClient
{
    public class NmsClient
    {
        private static log4net.ILog log;

        private readonly IList listeningToTopics = new ArrayList();
        private readonly object lockObject = new object();

        private static readonly string[] AccountAdminFields = { "lastUpdateTime", "batchCount", "onlineCount", "securityCount", "purge" };

        //private static readonly log4net.ILog log = log4net.LogManager.GetLogger("RtdClientApp.Logging");
        //log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

        private Boolean connected;

        private IConnection connection;
        private ISession session;

        private Boolean initialized;
        private readonly string[] baseFields;

        private DateTime? lastUpdateTime;

        private DateTime? heartBeatTime;

        private readonly string brokerUrl;

        private readonly PositionCache positionCache = PositionCache.Instance;

        /// <summary>
        /// Mapping from topic/field to Excel topicID
        /// </summary>
        private readonly IDictionary topicNameToTopicID = new Hashtable();
        /// <summary>
        /// Mapping of Excel topicID to topic/field
        /// </summary>
        private readonly IDictionary topicIDToTopicName = new Hashtable();

        /// <summary>
        /// The changed value for the Excel topicID
        /// </summary>
        private IDictionary changedTopics = new Hashtable();

        private DateTime? startUpTime;
        private int startupCount;
        // we use this during startup - we want to close it once we get all the messages

        private readonly Excel.IRTDUpdateEvent m_xlRTDUpdate;
        private IMessageConsumer batchConsumer;
        private Boolean running;

        public NmsClient(Excel.IRTDUpdateEvent xlRTDUpdate, string[] keyBaseFields, string brokerUrl)
        {
            var configurationFileName = System.Reflection.Assembly.GetExecutingAssembly().Location + ".config";
            log4net.Config.XmlConfigurator.Configure(new System.IO.FileInfo(configurationFileName));
            log = log4net.LogManager.GetLogger("SubscriberApp.Logging");

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
                positionCache.ClearCaches();
                if (!initialized)
                {
                    InitializeConnection();
                }
            }
        }

        public Boolean IsConnected()
        {
            return connected;
        }

        private void InitializeConnection()
        {
            try
            {
                IConnectionFactory factory = new ConnectionFactory(new Uri(brokerUrl));
                connection = factory.CreateConnection();

                connected = true;

                connection.ExceptionListener += OnException;

                //log.Info("Created a connection to " + brokerUrl);

                session = connection.CreateSession();
                initialized = true;
                connection.Start();
            }
            catch (Exception e)
            {
                // Nothing to do here.  If we don't catch this, Excel dies
               log.Error(e);
            }
        }

        public void Shutdown()
        {
            lock (lockObject)
            {
                running = false;
                initialized = false;
            }

            if (connection == null) return;

            connection.ExceptionListener -= OnException;
            if (session != null)
            {
                session.Close();
            }

            connection.Close();
        }

        public void StartListeningTo(string topicName, string recordKey, string fieldName, int topicId)
        {
            // Ensure we only listen to the topic once - this allows clients to call 
            // us multiple times
            lock (lockObject)
            {
                // Add the recordKey/fieldName to topicId mapping and start subscribing
                var topicFieldKey = new TopicKey(recordKey, fieldName);

                if (!topicNameToTopicID.Contains(topicFieldKey))
                {
                    topicNameToTopicID.Add(topicFieldKey, topicId);
                    topicIDToTopicName.Add(topicId, topicFieldKey);
                }

                if (listeningToTopics.Contains(topicName)) return;

                if (CreateTopicListener(topicName))
                {
                    listeningToTopics.Add(topicName);
                }
            }
        }


        public void RemoveTopic(int topicId)
        {
            if (!topicIDToTopicName.Contains(topicId)) return;
            var topicFieldKey = (TopicKey)topicIDToTopicName[topicId];

            topicNameToTopicID.Remove(topicFieldKey);
            topicIDToTopicName.Remove(topicId);
        }

        private bool CreateTopicListener(string topicName) {
            if (connected)
            {
                var localTopicName = "position." + topicName;
                IDestination topic = session.GetTopic(localTopicName);

                // create a consumer 
                var consumer = session.CreateConsumer(topic);
                consumer.Listener += OnlineMessage;

                // connect to the 'command' queue and issue a request for batch positions
                // need to send a conduit so that we can receive the data
                var commandQueue = session.GetQueue("position.Command");
                var commandProducer = session.CreateProducer(commandQueue);

                var responseQueue = session.CreateTemporaryQueue();
                batchConsumer = session.CreateConsumer(responseQueue);
                batchConsumer.Listener += StartupMessageLoad;

                var textCommandMessage = session.CreateTextMessage("SendBatchAndOnline account=" + topicName);
                textCommandMessage.NMSReplyTo = responseQueue;

                commandProducer.Send(textCommandMessage);
                return true;
            }
            return false;
        }

        public string LookupValue(string key, string fieldName)
        {
            // provide away to findout the brokerUrl/batchCount/onlineCount
            switch (fieldName) {
                case "brokerUrl":
                    return brokerUrl;
                case "purge":
                    positionCache.ClearCaches();
                    return "Purged Caches";
                case "securityCount": {
                    IDictionary book = new Hashtable();
                    var startsWith = key.Split('-')[0];
                    positionCache.FindAllOnlineFieldValues(startsWith, new[] { "securityId", "level1TagName", "normOpenPosition" }, book);
                    positionCache.FindAllBatchFieldValues(startsWith, new[] { "securityId", "level1TagName", "normOpenPosition" }, book);

                    return "" + book.Count;
                }
                case "batchCount":
                    return "" + positionCache.CountBatchCacheItems(key.Split('-')[0]);
                case "onlineCount":
                    return "" + positionCache.CountOnlineCacheItems(key.Split('-')[0]);
                case "lastUpdateTime":
                    return lastUpdateTime.ToString();
                case "heartBeat":
                    return (heartBeatTime == null) ? "" : heartBeatTime.ToString();
            }
            // We don't know what cache this item is in, check the online first, then batch
            var value = positionCache.LookupOnlineCacheValue(key, fieldName) ??
                positionCache.LookupBatchCacheValue(key, fieldName);

            return value ?? "N/A";
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
            
            var key = "";
            try {
                var count = 0;
                foreach (var keyBaseField in baseFields) {
                    count++;

                    if (!record.Contains(keyBaseField)) continue;

                    key = key + record[keyBaseField];
                    if (count < baseFields.Length) {
                        key = key + "-";
                    }
                }
            } catch(Exception e) {
                log.Error("Unable to build position key", e);
            }
            return key;
        }

        public String BuildPositionLookupKey(String rtdString, String valueField)
        {
            var record = ExtractRtdString(rtdString);
            var positionKey = BuildPositionKey(record);

            switch (valueField) {
                case "onlineCount":
                case "batchCount":
                case "lastUpdateTime":
                    return positionKey + valueField;
                case "heartBeat":
                case "brokerUrl":
                    return valueField;
            }

            return positionKey;
        }


        public IDictionary ConsumeChangedTopics()
        {
            IDictionary consumedChanges;
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

        private static IDictionary ExtractRtdString(String rtdString)
        {
            // Store the message parts into the cache
            var pairRegex = new Regex(@"\|");
            var keyValueRegex = new Regex(@"=");

            var parts = pairRegex.Split(rtdString);

            IDictionary record = new Hashtable();

            try {
                foreach (var token in parts) {
                    // a token is a key/value pair separated by '='
                    // Take each token and split into the key/value
                    var keyValue = keyValueRegex.Split(token);
                    if (keyValue.Length == 2) {
                        record.Add(keyValue[0], keyValue[1]);
                    } else {
                        record.Add(keyValue[0], "");
                    }
                }
            } catch (IndexOutOfRangeException e) {
                log.Error("Unalble to parse topic string: " + rtdString, e);
                throw;
            }
            return record;
        }

        public static string ExtractJmsTopic(string rtdString)
        {
            var record = ExtractRtdString(rtdString);

            return (string)record["account"];
        }

        private static IDictionary ExtractPositionRecord(string rawString)
        {
            // Store the message parts into the cache
            var pairRegex = new Regex(@"\|");
            var keyValueRegex = new Regex(@"=");

            var parts = pairRegex.Split(rawString);

            IDictionary record = new Hashtable();

            foreach (var token in parts)
            {
                try
                {
                    // a token is a key/value pair separated by '='
                    // Take each token and split into the key/value
                    var keyValue = keyValueRegex.Split(token);
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
                InitializeConnection();

                foreach (var o in listeningToTopics)
                {
                    CreateTopicListener((String) o);
                }
            }
        }

        protected void OnlineMessage(IMessage message) {
            // We need to build a list of updates, this can then be used by the
            // RTD server interface to only send back topics that have changed.
            var now = DateTime.Now;

            var textMessage = message as ITextMessage;
            if (textMessage == null) return;

            var record = ExtractPositionRecord(textMessage.Text);
            record.Add("receiveTime", now.ToString());

            // For each record build the cacheKey - the key would be configured
            var key = BuildPositionKey(record);

            // store each value
            var messageType = (string) record["messageType"];

            if ("BatchPosition".Equals(messageType)) {
                positionCache.AddBatchCacheItem(key, record);
                lastUpdateTime = now;
                RegisterChange(key, record);
            } else if ("OnlinePosition".Equals(messageType)) {
                positionCache.AddOnlineCacheItem(key, record);
                lastUpdateTime = now;
                RegisterChange(key, record);
            } else if ("HeartBeat".Equals(messageType)) {
                heartBeatTime = DateTime.Now;
                RegisterGenericChange("heartBeat", heartBeatTime.ToString());
            } else if ("Command".Equals(messageType) && record["purgeRecordType"] != null) {
                positionCache.ClearCaches();
                lastUpdateTime = now;
                // Force a recalc of the spreadsheet
                RegisterGenericChange("heartBeat", DateTime.Now.ToString());
            } else return;

            if (m_xlRTDUpdate != null) m_xlRTDUpdate.UpdateNotify();
        }

        private void RegisterGenericChange(string fieldName, string fieldValue)
        {
            var topicFieldKey = new TopicKey(fieldName, fieldName);

            if (!topicNameToTopicID.Contains(topicFieldKey)) return;

            var topicID = (int)topicNameToTopicID[topicFieldKey];
            // remove incase it is already present
            UpdateChangedTopic(topicID, fieldValue);
        }

        private void RegisterChange(string topicKey, IDictionary parsedMessage)
        {
            // for each topic and record, add them to the list of changed topics
            lock (lockObject)
            {
                foreach (string fieldName in parsedMessage.Keys)
                {
                    var topicFieldKey = new TopicKey(topicKey, fieldName);

                    if (!topicNameToTopicID.Contains(topicFieldKey)) continue;

                    var topicID = (int)topicNameToTopicID[topicFieldKey];
                    // remove incase it is already present
                    UpdateChangedTopic(topicID, (string)parsedMessage[fieldName]);
                }
            }
            // add in the admin fields
            AddAdminFieldUpdates(topicKey, parsedMessage);
        }

        private void AddAdminFieldUpdates(string topicKey, IDictionary parsedMessage)
        {
            var accountPair = "account="+parsedMessage["account"];

            foreach (var fieldName in AccountAdminFields)
            {
                var recordKey = BuildPositionLookupKey(accountPair, fieldName);

                var topicFieldKey = new TopicKey(recordKey, fieldName);

                if (!topicNameToTopicID.Contains(topicFieldKey)) continue;

                var topicID = (int)topicNameToTopicID[topicFieldKey];
                // remove incase it is already present
                UpdateChangedTopic(topicID, LookupValue(topicKey, fieldName));
            }
        }


        private void UpdateChangedTopic(int topicID, string newValue)
        {
            lock (lockObject) {
                changedTopics.Remove(topicID);
                changedTopics.Add(topicID, newValue);
            }
        }

        protected void StartupMessageLoad(IMessage message) {
            // We need to build a list of updates, this can then be used by the
            // RTD server interface to only send back topics that have changed.
            try {
                var now = DateTime.Now;
                lastUpdateTime = now;
                if (startUpTime == null) startUpTime = now;

                startupCount++;

                var textMessage = message as ITextMessage;
                if (textMessage == null) return;

                var record = ExtractPositionRecord(textMessage.Text);
                record.Add("receiveTime", now.ToString());

                // For each record build the cacheKey - the key would be configured
                var key = BuildPositionKey(record);

                // store each value
                var messageType = (string) record["messageType"];

                if ("BatchPosition".Equals(messageType)) {
                    log.Info("Adding batch position");
                    positionCache.AddBatchCacheItem(key, record);
                    RegisterChange(key, record);
                } else if ("OnlinePosition".Equals(messageType)) {
                    positionCache.AddOnlineCacheItem(key, record);
                    RegisterChange(key, record);
                } else if ("Command".Equals(messageType)) {
                    if (record.Contains("purgeRecordType") && record["purgeRecordType"] != null) {
                        positionCache.ClearCaches();
                        return;
                    }
                    if (record.Contains("endOfMessages") && record["endOfMessages"] != null) {
                        RegisterGenericChange("brokerUrl", brokerUrl);
                        // Ensure we update on the last message
                        if (m_xlRTDUpdate != null) m_xlRTDUpdate.UpdateNotify();

                        // The queue is complete, disconnect.
                        if (batchConsumer != null) {
                            //batchConsumer.Close(); // free up some resources
                        }
                        return;
                    }
                    return; // unknown record type
                } else return; // unknown command

                //We update at the end too
                if (m_xlRTDUpdate != null) m_xlRTDUpdate.UpdateNotify();
            } catch (Exception e) {
                log.Error("Receiving error:", e);
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

        public override bool Equals(object other)
        {
            var otherKey = other as TopicKey;

            if (otherKey == null) return false;
            
            return topicName == otherKey.topicName && fieldName == otherKey.fieldName;
        }

        public override int GetHashCode()
        {
            return topicName.GetHashCode() * 17 + fieldName.GetHashCode();
        }
    }
}