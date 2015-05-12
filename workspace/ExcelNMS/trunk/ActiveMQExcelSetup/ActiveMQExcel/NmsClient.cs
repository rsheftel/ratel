using System;
using System.Collections.Generic;
using System.Text.RegularExpressions;
using Apache.NMS;
using Apache.NMS.ActiveMQ;
using Microsoft.Office.Interop.Excel;

namespace ActiveMQExcel {
    public class NmsClient
    {
        readonly log4net.ILog _log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

        private readonly IList<string> _listeningToTopics = new List<string>();
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
        /// Mapping from topic/field to Excel TopicId
        /// </summary>
        private readonly IDictionary<TopicKey, IList<int>> _topicNameToTopicId = new Dictionary<TopicKey, IList<int>>();
        /// <summary>
        /// Mapping of Excel TopicId to topic/field
        /// </summary>
        private readonly IDictionary<int, TopicKey> _topicIdToTopicName = new Dictionary<int, TopicKey>();

        /// <summary>
        /// The changed value for the Excel TopicId
        /// </summary>
        private IDictionary<int, string> _changedTopics = new Dictionary<int, string>();

        private DateTime? _startUpTime;
        private int _startupCount;
        // we use this during startup - we want to close it once we get all the messages

        private readonly IRTDUpdateEvent _mXlRtdUpdate;
        private IMessageConsumer _batchConsumer;

        public NmsClient(IRTDUpdateEvent xlRtdUpdate, string[] keyBaseFields, string brokerUrl)
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
                if (!Initialized)
                {
                    InitializeConnection();
                }
            }
        }

        private bool Initialized {
            get
            {
                lock (_lockObject) {
                    return _initialized;
                }
            }
            set {
                lock (_lockObject) {
                    _initialized = value;
                }
            }
        }
        public int TopicCount {
            get { return _topicIdToTopicName.Count; }
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
                Initialized = true;
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
            Initialized = false;

            if (_connection == null) return;
            _connection.ExceptionListener -= OnException;
            if (_session != null)
            {
                _session.Close();
            }

            _connection.Close();
        }

        public void StartListeningTo(string topicName, string recordKey, string fieldName, int topicId)
        {
            // Ensure we only listen to the topic once - this allows clients to call 
            // us multiple times
            lock (_lockObject)
            {
                // Add the recordKey/fieldName to topicId mapping and start subscribing
                var topicFieldKey = new TopicKey(recordKey, fieldName);

                if (!_topicNameToTopicId.ContainsKey(topicFieldKey))
                {
                    var newTopicList = new List<int>();
                    _topicNameToTopicId.Add(topicFieldKey, newTopicList);
                }
                var topicIds = _topicNameToTopicId[topicFieldKey];
                topicIds.Add(topicId);

                if (!_topicIdToTopicName.ContainsKey(topicId)) {
                    _topicIdToTopicName.Add(topicId, topicFieldKey);
                }

                if (_listeningToTopics.Contains(topicName)) return;
                if (CreateTopicListener(topicName))
                {
                    _listeningToTopics.Add(topicName);
                }
            }
        }

        public void RemoveTopic(int topicId)
        {
            lock (_lockObject) {
                if (!_topicIdToTopicName.ContainsKey(topicId)) return;
                var topicFieldKey = _topicIdToTopicName[topicId];
                var topicList = _topicNameToTopicId[topicFieldKey];
                topicList.Remove(topicId);
                if (topicList.Count == 0) {
                    _topicNameToTopicId.Remove(topicFieldKey);
                }
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

        private String BuildPositionKey(IDictionary<string, string> record)
        {
            var key = "";
            var count = 0;
            foreach (var keyBaseField in _baseFields)
            {
                count++;

                if (!record.ContainsKey(keyBaseField)) continue;
                key = key + record[keyBaseField];
                if (count < _baseFields.Length)
                {
                    key = key + "-";
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
            if (valueField == "brokerUrl" || valueField == "heartBeat")
            {
                return valueField;
            }

            return positionKey;
        }


        public IDictionary<int, string> ConsumeChangedTopics()
        {
            IDictionary<int, string> consumedChanges;
            lock (_lockObject)
            {
                consumedChanges = _changedTopics;
                _changedTopics = new Dictionary<int, string>();
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

        private IDictionary<string, string> ExtractRtdString(String rtdString)
        {
            // Store the message parts into the cache
            var parts = _pairRegex.Split(rtdString);

            var record = new Dictionary<string, string>();

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

            return record["account"];
        }

        private static IDictionary<string, string>ExtractPositionRecord(string rawString)
        {
            // Store the message parts into the cache
            var parts = _pairRegex.Split(rawString);

            var record = new Dictionary<string, string>();

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

        void OnException(Exception exception)
        {
            //_log.Error("Exception on NMS connection", exception);
            // need to reconnect
            lock (_lockObject)
            {
                _connected = false;
                Initialized = false;
                _listeningToTopics.Clear();
                InitializeConnection();

                foreach (var o in _listeningToTopics)
                {
                    CreateTopicListener(o);
                }
            }
        }

        void OnlineMessage(IMessage message)
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
            var messageType = record["messageType"];

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

            if (!_topicNameToTopicId.ContainsKey(topicFieldKey)) return;
            var topicId = _topicNameToTopicId[topicFieldKey];
            // remove incase it is already present
            UpdateChangedTopic(topicId, fieldValue);
        }

        private void RegisterChange(string topicKey, IDictionary<string, string> parsedMessage)
        {
            // for each topic and record, add them to the list of changed topics
            lock (_lockObject)
            {
                foreach (var fieldName in parsedMessage.Keys)
                {
                    var topicFieldKey = new TopicKey(topicKey, fieldName);

                    if (!_topicNameToTopicId.ContainsKey(topicFieldKey)) continue;
                    var topicId = _topicNameToTopicId[topicFieldKey];
                    // remove incase it is already present
                    UpdateChangedTopic(topicId, parsedMessage[fieldName]);
                }
            }
            // add in the admin fields
            AddAdminFieldUpdates(topicKey, parsedMessage);
        }

        private void AddAdminFieldUpdates(string topicKey, IDictionary<string, string> parsedMessage)
        {
            var accountPair = "account="+parsedMessage["account"];

            foreach (var fieldName in _accountAdminFields)
            {
                var recordKey = BuildPositionLookupKey(accountPair, fieldName);

                var topicFieldKey = new TopicKey(recordKey, fieldName);

                if (!_topicNameToTopicId.ContainsKey(topicFieldKey)) continue;
                var topicId = _topicNameToTopicId[topicFieldKey];
                // remove incase it is already present
                UpdateChangedTopic(topicId, LookupValue(topicKey, fieldName));
            }
        }


        private void UpdateChangedTopic(IEnumerable<int> topicIds, string newValue)
        {
            lock (_lockObject) {
                foreach (var topicId in topicIds) {
                    _changedTopics.Remove(topicId);
                    _changedTopics.Add(topicId, newValue);
                }
            }
        }

        void StartupMessageLoad(IMessage message)
        {
            try {
                // We need to build a list of updates, this can then be used by the
                // RTD server interface to only send back topics that have changed.
                var now = DateTime.Now;
                _lastUpdateTime = now;
                if (_startUpTime == null) _startUpTime = now;

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
                var messageType = record["messageType"];

                if ("BatchPosition".Equals(messageType)) {
                    _positionCache.AddBatchCacheItem(key, record);
                    RegisterChange(key, record);
                } else if ("OnlinePosition".Equals(messageType)) {
                    _positionCache.AddOnlineCacheItem(key, record);
                    RegisterChange(key, record);
                } else if ("Command".Equals(messageType)) {
                    if (record.ContainsKey("purgeRecordType") && record["purgeRecordType"] != null) {
                        _positionCache.ClearCaches();
                        return;
                    }
                    if (record.ContainsKey("endOfMessages") && record["endOfMessages"] != null) {
                        RegisterGenericChange("brokerUrl", _brokerUrl);
                        // Ensure we update on the last message
                        if (_mXlRtdUpdate != null) {
                            _mXlRtdUpdate.UpdateNotify();
                        }

                        // The queue is complete, disconnect.
                        if (_batchConsumer != null) {
                            //_batchConsumer.Close(); // free up some resources
                        }
                        return;
                    }
                    return; // unknown record type
                } else {
                    return; // unknown command
                }

                //We update at the end too
                if (_mXlRtdUpdate != null) {
                    _mXlRtdUpdate.UpdateNotify();
                }
            } catch(Exception e) {
                _log.Error("Unable to process startup message", e);
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