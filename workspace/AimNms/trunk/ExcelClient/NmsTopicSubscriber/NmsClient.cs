using System;
using System.Collections;
using System.Collections.Generic;
using System.Text.RegularExpressions;
using System.Threading;
using Apache.NMS;
using Apache.NMS.ActiveMQ;
using Excel = Microsoft.Office.Interop.Excel;

namespace NmsRtdClient
{
    public class NmsClient
    {
        readonly log4net.ILog _log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

        private readonly IList _listeningToTopics = new ArrayList();
        private readonly object _lockObject = new object();

        private static readonly string[] _accountAdminFields = { "lastUpdateTime", "batchCount", "onlineCount", "securityCount", "purge" };

        //private static readonly log4net.ILog _log = log4net.LogManager.GetLogger("RtdClientApp.Logging");
        //log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

        private static readonly Regex _pairRegex = new Regex(@"\|");
        private static readonly Regex _keyValueRegex = new Regex(@"=");

        private bool _connected;

        private IConnection _connection;
        private ISession _session;

        private bool _initialized;
        private readonly string[] _baseFields;

        private DateTime _lastUpdateTime;

        private DateTime _heartBeatTime;

        private readonly string _brokerUrl;

        private readonly PositionCache _positionCache = PositionCache.Instance;

        /// <summary>
        /// Mapping from topic/field to Excel topicID
        /// </summary>
        private readonly IDictionary _topicNameToTopicId = new Hashtable();
        /// <summary>
        /// Mapping of Excel topicID to topic/field
        /// </summary>
        private readonly IDictionary _topicIdToTopicName = new Hashtable();

        /// <summary>
        /// The changed value for the Excel topicID
        /// </summary>
        private IDictionary _changedTopics = new Hashtable();

        private DateTime? _startUpTime;
        private int _startupCount;
        // we use this during startup - we want to close it once we get all the messages

        private readonly Excel.IRTDUpdateEvent _mXlRtdUpdate;
        private IMessageConsumer _batchConsumer;
        private bool _running;

        public NmsClient(Excel.IRTDUpdateEvent xlRtdUpdate, string[] keyBaseFields, string brokerUrl)
        {
            var configurationFileName = System.Reflection.Assembly.GetExecutingAssembly().Location + ".config";
            log4net.Config.XmlConfigurator.Configure(new System.IO.FileInfo(configurationFileName));
//            _log = log4net.LogManager.GetLogger("SubscriberApp.Logging");

            _baseFields = keyBaseFields;
            _mXlRtdUpdate = xlRtdUpdate;
            _brokerUrl = brokerUrl;
            _heartBeatTime = DateTime.Now;

            //_log.Info("About to connect to ActiveMQ");
            // Each instance of the Excel RTD creates a new COM instance, since we are using static
            // fields to store the data, we do not need to re-load the data for each instance
            // ensure that we only initialize once
            lock (_lockObject)
            {
                _positionCache.ClearCaches();
                if (!_initialized)
                {
                    InitializeConnection();
                }
            }
        }

        public bool IsConnected()
        {
            return _connected;
        }

        private void InitializeConnection()
        {
            try
            {
                if (_connection != null) {
                    Shutdown();
                }
                IConnectionFactory factory = new ConnectionFactory(new Uri(_brokerUrl));
                _connection = factory.CreateConnection();

                _connected = true;

                _connection.ExceptionListener += OnException;

                //_log.Info("Created a connection to " + _brokerUrl);

                _session = _connection.CreateSession();
                _initialized = true;
                _connection.Start();
            }
            catch (Exception e)
            {
                // Nothing to do here.  If we don't catch this, Excel dies
               _log.Error(e);
            }
        }

        public void Shutdown()
        {
            lock (_lockObject)
            {
                _running = false;
                _initialized = false;
            }

            if (_connection != null)
            {
                _connection.ExceptionListener -= OnException;
                if (_session != null)
                {
                    _session.Close();
                }

                _connection.Close();
            }
        }

        public void StartListeningTo(string topicName, string recordKey, string fieldName, int topicID)
        {
            // Ensure we only listen to the topic once - this allows clients to call 
            // us multiple times
            lock (_lockObject)
            {
                // Add the recordKey/fieldName to topicID mapping and start subscribing
                var topicFieldKey = new TopicKey(recordKey, fieldName);

                if (!_topicNameToTopicId.Contains(topicFieldKey))
                {
                    _topicNameToTopicId.Add(topicFieldKey, topicID);
                    _topicIdToTopicName.Add(topicID, topicFieldKey);
                }

                if (!_listeningToTopics.Contains(topicName))
                {
                    if (CreateTopicListener(topicName))
                    {
                        _listeningToTopics.Add(topicName);
                    }
                }
            }
        }


        public void RemoveTopic(int topicId)
        {
            if (_topicIdToTopicName.Contains(topicId))
            {
                var topicFieldKey = (TopicKey)_topicIdToTopicName[topicId];

                _topicNameToTopicId.Remove(topicFieldKey);
                _topicIdToTopicName.Remove(topicId);
            }
        }

        private bool CreateTopicListener(string topicName) {
            if (!_connected) return false;

            var localTopicName = "position." + topicName;
            IDestination topic = _session.GetTopic(localTopicName);

            // create a consumer 
            var consumer = _session.CreateConsumer(topic);
            consumer.Listener += OnlineMessage;

            // connect to the 'command' queue and issue a request for batch positions
            // need to send a conduit so that we can receive the data
            var commandQueue = _session.GetQueue("position.Command");
            var commandProducer = _session.CreateProducer(commandQueue);

            var responseQueue = _session.CreateTemporaryQueue();
            _batchConsumer = _session.CreateConsumer(responseQueue);
            _batchConsumer.Listener += StartupMessageLoad;

            var textCommandMessage = _session.CreateTextMessage("SendBatchAndOnline account=" + topicName);
            textCommandMessage.NMSReplyTo = responseQueue;

            commandProducer.Send(textCommandMessage);
            return true;
        }

        public string LookupValue(string key, string fieldName)
        {
            // provide away to findout the brokerUrl/batchCount/onlineCount
            switch (fieldName) {
                case "brokerUrl":
                    return _brokerUrl;
                case "purge":
                    _positionCache.ClearCaches();
                    return "Purged Caches";
                case "securityCount": {
                    IDictionary<string, string[]> book = new Dictionary<string, string[]>();
                    var startsWith = key.Split('-')[0];
                    _positionCache.FindAllOnlineFieldValues(startsWith, new[] { "securityId", "level1TagName" }, book);
                    _positionCache.FindAllBatchFieldValues(startsWith, new[] { "securityId", "level1TagName" }, book);

                    return "" + book.Count;
                }
                case "batchCount":
                    return "" + _positionCache.CountBatchCacheItems(key.Split('-')[0]);
                case "onlineCount":
                    return "" + _positionCache.CountOnlineCacheItems(key.Split('-')[0]);
                case "lastUpdateTime":
                    return _lastUpdateTime.ToString();
                case "heartBeat":
                    return _heartBeatTime.ToString();
            }
            // We don't know what cache this item is in, check the online first, then batch
            var value = _positionCache.LookupOnlineCacheValue(key, fieldName) ??
                _positionCache.LookupBatchCacheValue(key, fieldName);

            return value ?? "N/A";
        }

        public void NMSReaderLoop()
        {
            _connection.Start();

            _running = true;
            // Wait forever
            while (_running)
            {
                //_log.Debug("[NSM-" + Thread.CurrentThread.ManagedThreadId + "] Total entries in onlineCache=" +onlineCache.Count);
                //_log.Debug("[NSM-" + Thread.CurrentThread.ManagedThreadId + "] Total entries in batchCache =" +batchCache.Count);
                Thread.Sleep(6*1000);
            }
        }

        private String BuildPositionKey(IDictionary record)
        {
            var key = "";
            var count = 0;
            foreach (var keyBaseField in _baseFields)
            {
                count++;

                if (record.Contains(keyBaseField))
                {
                    key = key + record[keyBaseField];
                    if (count < _baseFields.Length)
                    {
                        key = key + "-";
                    }
                }
            }
            return key;
        }

        public String BuildPositionLookupKey(String rtdString, String valueField)
        {
            var record = ExtractRtdString(rtdString);
            var positionKey = BuildPositionKey(record);

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
            IDictionary consumedChanges;
            lock (_lockObject)
            {
                consumedChanges = _changedTopics;
                _changedTopics = new Hashtable();
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

        private IDictionary ExtractRtdString(String rtdString)
        {
            // Store the message parts into the cache
            var parts = _pairRegex.Split(rtdString);

            IDictionary record = new Hashtable();

            try {
                foreach (var token in parts) {
                    // a token is a key/value pair separated by '='
                    // Take each token and split into the key/value
                    var keyValue = _keyValueRegex.Split(token);
                    record.Add(keyValue[0], keyValue[1]);
                }
            } catch (IndexOutOfRangeException e) {
                _log.Error("Unalble to parse topic string: " + rtdString, e);
                throw;
            }
            return record;
        }

        public string ExtractJmsTopic(string rtdString)
        {
            var record = ExtractRtdString(rtdString);

            return (string)record["account"];
        }

        private static IDictionary ExtractPositionRecord(string rawString)
        {
            // Store the message parts into the cache
            var parts = _pairRegex.Split(rawString);

            IDictionary record = new Hashtable();

            foreach (var token in parts)
            {
                try
                {
                    // a token is a key/value pair separated by '='
                    // Take each token and split into the key/value
                    var keyValue = _keyValueRegex.Split(token);
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
            //_log.Error("Exception on NMS connection", exception);
            // need to reconnect
            lock (_lockObject)
            {
                _connected = false;
                _initialized = false;
                _listeningToTopics.Clear();
                InitializeConnection();

                foreach (var o in _listeningToTopics)
                {
                    CreateTopicListener((String) o);
                }
            }
        }


        protected void OnlineMessage(IMessage message)
        {
                // We need to build a list of updates, this can then be used by the
                // RTD server interface to only send back topics that have changed.
                var now = DateTime.Now;

                var textMessage = message as ITextMessage;
                if (textMessage == null) {
                    return;
                }
                var record = ExtractPositionRecord(textMessage.Text);
                record.Add("receiveTime", now.ToString());

                // For each record build the cacheKey - the key would be configured
                var key = BuildPositionKey(record);

                // store each value
                var messageType = (string)record["messageType"];

                if ("BatchPosition".Equals(messageType))
                {
                    _positionCache.AddBatchCacheItem(key, record);
                    _lastUpdateTime = now;
                    RegisterChange(key, record);
                }
                else if ("OnlinePosition".Equals(messageType))
                {
                    _positionCache.AddOnlineCacheItem(key, record);
                    _lastUpdateTime = now;
                    RegisterChange(key, record);
                }
                else if ("HeartBeat".Equals(messageType))
                {
                    _heartBeatTime = DateTime.Now;
                    RegisterGenericChange("heartBeat", _heartBeatTime.ToString());
                }
                else if ("Command".Equals(messageType) && record["purgeRecordType"] != null)
                {
                    _positionCache.ClearCaches();
                    _lastUpdateTime = now;
                    // Force a recalc of the spreadsheet
                    RegisterGenericChange("heartBeat", DateTime.Now.ToString());
                }
                else
                {
                    return;
                }

                if (_mXlRtdUpdate != null)
                {
                    _mXlRtdUpdate.UpdateNotify();
                }
        }

        private void RegisterGenericChange(string fieldName, string fieldValue)
        {
            var topicFieldKey = new TopicKey(fieldName, fieldName);

            if (_topicNameToTopicId.Contains(topicFieldKey))
            {
                var topicId = (int)_topicNameToTopicId[topicFieldKey];
                // remove incase it is already present
                UpdateChangedTopic(topicId, fieldValue);
            }
        }

        private void RegisterChange(string topicKey, IDictionary parsedMessage)
        {
            // for each topic and record, add them to the list of changed topics
            lock (_lockObject)
            {
                foreach (string fieldName in parsedMessage.Keys)
                {
                    var topicFieldKey = new TopicKey(topicKey, fieldName);

                    if (_topicNameToTopicId.Contains(topicFieldKey))
                    {
                        var topicId = (int)_topicNameToTopicId[topicFieldKey];
                        // remove incase it is already present
                        UpdateChangedTopic(topicId, (string)parsedMessage[fieldName]);
                    }
                }
            }
            // add in the admin fields
            AddAdminFieldUpdates(topicKey, parsedMessage);
        }

        private void AddAdminFieldUpdates(string topicKey, IDictionary parsedMessage)
        {
            var accountPair = "account="+parsedMessage["account"];

            foreach (var fieldName in _accountAdminFields)
            {
                var recordKey = BuildPositionLookupKey(accountPair, fieldName);

                var topicFieldKey = new TopicKey(recordKey, fieldName);

                if (_topicNameToTopicId.Contains(topicFieldKey))
                {
                    var topicId = (int)_topicNameToTopicId[topicFieldKey];
                    // remove incase it is already present
                    UpdateChangedTopic(topicId, LookupValue(topicKey, fieldName));
                }
            }
        }


        private void UpdateChangedTopic(int topicId, string newValue)
        {
            lock (_lockObject) {
                _changedTopics.Remove(topicId);
                _changedTopics.Add(topicId, newValue);
            }
        }

        void StartupMessageLoad(IMessage message)
        {
                // We need to build a list of updates, this can then be used by the
                // RTD server interface to only send back topics that have changed.
                var now = DateTime.Now;
                _lastUpdateTime = now;
                if (_startUpTime == null)
                    _startUpTime = now;

                _startupCount++;

                var textMessage = message as ITextMessage;
                if (textMessage == null) {
                    return;
                }

                var record = ExtractPositionRecord(textMessage.Text);
                record.Add("receiveTime", now.ToString());

                // For each record build the cacheKey - the key would be configured
                var key = BuildPositionKey(record);

                // store each value
                var messageType = (string)record["messageType"];

                if ("BatchPosition".Equals(messageType))
                {
                    _positionCache.AddBatchCacheItem(key, record);
                    RegisterChange(key, record);
                }
                else if ("OnlinePosition".Equals(messageType))
                {
                    _positionCache.AddOnlineCacheItem(key, record);
                    RegisterChange(key, record);
                }
                else if ("Command".Equals(messageType))
                {
                    if (record.Contains("purgeRecordType") && record["purgeRecordType"] != null)
                    {
                        _positionCache.ClearCaches();
                        return;
                    }
                    else if (record.Contains("endOfMessages") && record["endOfMessages"] != null)
                    {
                        RegisterGenericChange("brokerUrl", _brokerUrl);
                        // Ensure we update on the last message
                        if (_mXlRtdUpdate != null)
                        {
                            _mXlRtdUpdate.UpdateNotify();
                        }

                        // The queue is complete, disconnect.
                        if (_batchConsumer != null)
                        {
                            //_batchConsumer.Close(); // free up some resources
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
                if (_mXlRtdUpdate != null)
                {
                    _mXlRtdUpdate.UpdateNotify();
                }
        }
    }

    internal class TopicKey
    {
        public TopicKey(string topic, string field)
        {
            _topicName = topic;
            _fieldName = field;
        }

        readonly string _topicName;
        readonly string _fieldName;

        public override bool Equals(Object other)
        {
            var otherKey = other as TopicKey;

            if (otherKey == null) {
                return false;
            }
            return _topicName == otherKey._topicName && _fieldName == otherKey._fieldName;
        }

        public override int GetHashCode()
        {
            return _topicName.GetHashCode() * 17 + _fieldName.GetHashCode();
        }
    }
}