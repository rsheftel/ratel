using System;
using System.Collections.Generic;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Text;
using ActiveMQLibrary;
using Microsoft.Office.Interop.Excel;

// look for a file named ActiveMQ.exe.config or ActiveMQ.dll.config

[assembly : log4net.Config.XmlConfigurator(Watch = true)]

namespace ActiveMQExcel {
    [ComVisible(true), ProgId("AMQSub")] 
    public class ActiveMQRtdServer : IRtdServer {

        readonly IDictionary<string, Broker> _brokers = new Dictionary<string, Broker>();
        readonly IDictionary<int, TopicDetails> _topicToDetails = new Dictionary<int, TopicDetails>();
        // broker - topic - field
        readonly IDictionary<string, IDictionary<string, IList<TopicDetails>>> _fieldToDetails = new Dictionary<string, IDictionary<string, IList<TopicDetails>>>();
        readonly IDictionary<string, string> _topicValueCache = new Dictionary<string, string>();
        readonly IList<int> _changedTopics = new List<int>();

        readonly object _lockObject = new object();

        readonly log4net.ILog _log = log4net.LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);
        readonly string _version = Assembly.GetExecutingAssembly().GetName().Version.ToString();

        IRTDUpdateEvent _xlRtdUpdateCallbackHandler;

        private readonly TopicHandler _topicHandler;

        AppConfiguration _config;

        public ActiveMQRtdServer() :this(AppConfiguration.Load()) {}

        public ActiveMQRtdServer(AppConfiguration config) {
            _topicHandler = new TopicHandler(TopicChanged);
            _config = config;
        }

        public int TopicCount {
            get {
                lock (_topicToDetails) {
                    return _topicToDetails.Count;
                }
            }
        }
        public int FieldCount {
            get {
                var count = 0;
                foreach (var broker in _fieldToDetails) {
                   foreach (var topic in broker.Value) {
                       // We cannot count the topic details, we need to fields
                       var fieldNames = new List<string>();
                       foreach (var detail in topic.Value) {
                           if (!fieldNames.Contains(detail.Field)) {
                               fieldNames.Add(detail.Field);
                           }
                       }
                       count = count + fieldNames.Count;
                   }
                }
                
                return count;
            }
        }
        #region IRtdServer Members
        /// <summary>
        /// Create a new Excel topic.
        /// 
        /// An Excel topic is a cell.  We may have multiple Excel topics listening to the same
        /// NMS topic.
        /// 
        /// Parse the supplied values and create a mapping so that the application can 
        /// send data back to the correct topic/cell.
        /// </summary>
        /// <param name="topicId"></param>
        /// <param name="strings">
        /// The first value is a string representing the topic.
        /// The second value is the field of interest.
        /// The third (optional) value is the broker. 
        /// </param>
        /// <param name="getNewValues"></param>
        /// <returns></returns>
        public object ConnectData(int topicId, ref Array strings, ref bool getNewValues) {
            getNewValues = true; // over-write any saved values in the spreadsheet
            TopicDetails details = null;
            try {
                _log.Info("Creating subscriber for Excel topic " + topicId);
                // If we have a brokerUrl use it, otherwise use the default broker
                 details = new TopicDetails {
                    TopicId = topicId,
                    Topic = ((string) strings.GetValue(0)),
                    Field = ((string) strings.GetValue(1)),
                    BrokerUrl = _config.MarketDataBrokerUrl
                };

                if (strings.Length > 2) {
                    details.BrokerUrl = (string)strings.GetValue(2);
                }

                // setup our mappings
                lock (_topicToDetails) {
                    _topicToDetails.Add(topicId, details);

                    var detailList = GetFieldDetails(details);
                    if (!detailList.Contains(details)) detailList.Add(details);

                    var broker = BrokerFactory.Broker(details.BrokerUrl);
                    var subscriber = broker.TopicSubscriber(details.Topic, _topicHandler.OnMessageHandler);
                    if (!_brokers.ContainsKey(details.BrokerUrl)) {
                        _brokers.Add(details.BrokerUrl, broker);
                    }

                    var rt = LookupValueFor(details.BrokerUrl, details.Topic, details.Field);
                    if (rt != null) {
                        return rt;
                    }
                }
                switch (details.Field) {
                    case "productVersion":
                        return _version;
                    case "brokerUrl":
                        return _config.MarketDataBrokerUrl;
                }
                return "N/A";
            } catch (Exception e) {
                _log.Error("Tried to subscribe to topic " + strings, e);

                if (details != null) {
                    SetLookupValue(details.BrokerUrl, details.Topic, details.Field, "#Error");
                }
                return "#Error";
            }
        }

        void SetLookupValue(string url, string topic, string field, string value) {
            var key = BuildValueCacheKey(url, topic, field);
            if (_topicValueCache.ContainsKey(key)) {
                _topicValueCache[key] = value;
            } else {
                _topicValueCache.Add(key, value);
            }
        }

        string LookupValueFor(TopicDetails details) {
            return LookupValueFor(details.BrokerUrl, details.Topic, details.Field);
        }

        string LookupValueFor(string url, string topic, string field) {
            var key = BuildValueCacheKey(url, topic, field);
            return _topicValueCache.ContainsKey(key) ? _topicValueCache[key] : null;
        }

        IList<TopicDetails> GetFieldDetails(string brokerUrl, string topic) {
            lock (_topicToDetails) {
                if (_fieldToDetails.ContainsKey(brokerUrl)) {
                    var topics = _fieldToDetails[brokerUrl];
                    if (topics.ContainsKey(topic)) {
                        var sourceList = topics[topic];
                        var copy = new List<TopicDetails>(sourceList);

                        return copy;
                    }
                }
            }
            return null;
        }

        IList<TopicDetails> GetFieldDetails(TopicDetails details) {
            // We have a completely new entry
            if (!_fieldToDetails.ContainsKey(details.BrokerUrl)) {
                var newTopics = new Dictionary<string, IList<TopicDetails>>();
                _fieldToDetails.Add(details.BrokerUrl, newTopics);
                var newFieldList = new List<TopicDetails>();
                newTopics.Add(details.Topic, newFieldList);

                return newFieldList;
            }

            var topics = _fieldToDetails[details.BrokerUrl];
            // We have a new field with a NMS Topic
            if (!topics.ContainsKey(details.Topic)) {
                var newFieldList = new List<TopicDetails>();
                topics.Add(details.Topic, newFieldList);

                return newFieldList;
            }

            return topics[details.Topic];
        }

        /// <summary>
        /// Remove the Excel topic and clean up the subscribers.
        /// </summary>
        /// <param name="topicId"></param>
        public void DisconnectData(int topicId) {
            try {
                _log.Info("Disconnecting Excel topic " + topicId);
                lock (_topicToDetails) {
                    // handle the Topic Id first
                    if (!_topicToDetails.ContainsKey(topicId)) {
                        return;
                    }

                    var topicDetails = _topicToDetails[topicId];
                    _topicToDetails.Remove(topicId);

                    if (!_fieldToDetails.ContainsKey(topicDetails.BrokerUrl)) return;

                    var topics = _fieldToDetails[topicDetails.BrokerUrl];
                    if (!topics.ContainsKey(topicDetails.Topic)) return;

                    var fields = topics[topicDetails.Topic];
                    if (fields.Contains(topicDetails)) {
                        fields.Remove(topicDetails);
                    }
                    if (fields.Count == 0) {
                        topics.Remove(topicDetails.Topic);
                    }
                    // If we have no interested parties, clean up the subscriber
                    if (topics.Count != 0) return;

                    _fieldToDetails.Remove(topicDetails.BrokerUrl);
                    var broker = BrokerFactory.Broker(topicDetails.BrokerUrl);
                    broker.RemoveSubscriber(topicDetails.Topic);
                }
            } catch(Exception e) {
                _log.Error("Tried to disconnect Excel topic " + topicId, e);
            }
        }

        /// <summary>
        /// Return the status of the RtdServer
        /// </summary>
        /// <returns></returns>
        public int Heartbeat() {
            try {
                if (_brokers.Count > 0) {
                    // check that each broker is started
                    var allBrokersStarted = true;
                    foreach (var broker in _brokers)
                        if (broker.Value.HasConnectionFailed) {
                            allBrokersStarted = false;
                            break;
                        }
                    if (!allBrokersStarted) return -1;
                }
                // force a restart
                if (_topicValueCache.Values.Contains("#Error")) {
                    return -1;
                }
                return 1;
            } catch (Exception e) {
                _log.Error("Brokers are not connected", e);
            }
            return -1;
        }

        /// <summary>
        /// Get the list of changed topics, and publish their values back to the
        /// spreadsheet.
        /// </summary>
        /// <param name="topicCount">the number of topics updated</param>
        /// <returns>array of TopicId and topicValue</returns>
        public Array RefreshData(ref int topicCount) {
            try {
                lock (_lockObject) {
                    var changedTopics = new List<int>(_changedTopics);
                    _changedTopics.Clear();

                    var updatedTopics = new Object[2,changedTopics.Count];
                    var i = 0;

                    foreach (var topicId in changedTopics) {
                        if (!_topicToDetails.ContainsKey(topicId)) {
                            continue;
                        }
                        var topicDetails = _topicToDetails[topicId];

                        updatedTopics[0, i] = topicId;
                        updatedTopics[1, i] = LookupValueFor(topicDetails);
                        i++;
                    }
                    topicCount = i;

                    //log.Info("Refreshing data: " + changedTopics.Count);
                    return updatedTopics;
                }
            } catch (Exception e) {
                _log.Error("Tried to RefreshData", e);
                return new object[2,0];
            }
        }

        /// <summary>
        /// Called when the RTD server is loaded.
        /// 
        /// </summary>
        /// <param name="callbackHandler">handle to Excel that we use to notify Excel of 
        /// topic value updates</param>
        /// <returns>1 if we started OK, -1 if we cannot start</returns>
        public int ServerStart(IRTDUpdateEvent callbackHandler) {
            _log.Info("Starting RTD server Subscriber");
            // Setup our internal state as we have just been loaded by Excel
            _xlRtdUpdateCallbackHandler = callbackHandler;

            // Load when we are running inside of Excel
            if (_config == null) {
                _config = AppConfiguration.Load();
            }
            return 1;
        }

        public void ServerTerminate() {
            _log.Info("Stopping RTD Server");
            _xlRtdUpdateCallbackHandler = null;
               
            lock (_brokers) {
                foreach (var broker in _brokers) {
                    try {
                        broker.Value.Shutdown();
                    } catch (Exception e) {
                        _log.Error("Unable to shutdown broker", e);
                    }
                }

                _brokers.Clear();
            }
        }
        #endregion
        void TopicChanged(object sender, TopicUpdateEvent e) {
            try {
                var message = e.EventMessage;
                var topic = message["TopicName"];
                var brokerUrl = message["BrokerUrl"];

                lock (_lockObject) {
                    // put all the received fields into the cache
                    foreach (var field in message) {
                        var valueCacheKey = BuildValueCacheKey(brokerUrl, topic, field.Key);
                        _topicValueCache[valueCacheKey] = field.Value;
                    }

                    // add fields that we are interested in to the change field list
                    var fieldDetailList = GetFieldDetails(brokerUrl, topic);
                    foreach (var field in fieldDetailList) {
                        if (message.ContainsKey(field.Field) && !_changedTopics.Contains(field.TopicId)) {
                            _changedTopics.Add(field.TopicId);
                        }
                    }
                }

                if (_xlRtdUpdateCallbackHandler != null) {
                    _xlRtdUpdateCallbackHandler.UpdateNotify();
                }
            } catch (Exception exception) {
                _log.Error("Tried to update topic", exception);
            }
        }

        static string BuildValueCacheKey(string url, string topic, string field) {
            var key = new StringBuilder(50);
            key.Append(url).Append("-").Append(topic).Append("-").Append(field);

            return key.ToString();
        }
    }

    #region Nested type: TopicDetails
    internal class TopicDetails {
        public string Field; // field of interest
        public string Topic; // NMS topic
        public int TopicId; // Excel RTD TopicId
        public string BrokerUrl; // Broker URL

        public bool Equals(TopicDetails other) {
            if (ReferenceEquals(null, other)) return false;
            if (ReferenceEquals(this, other)) return true;
            return Equals(other.BrokerUrl, BrokerUrl) && other.TopicId == TopicId && Equals(other.Topic, Topic) && Equals(other.Field, Field);
        }

        public override bool Equals(object obj) {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            return obj.GetType() == typeof (TopicDetails) && Equals((TopicDetails) obj);
        }

        public override int GetHashCode() {
            unchecked {
                var result = BrokerUrl.GetHashCode();
                result = (result * 397)^TopicId;
                result = (result * 397)^Topic.GetHashCode();
                result = (result * 397)^Field.GetHashCode();
                return result;
            }
        }

        public static bool operator ==(TopicDetails left, TopicDetails right) {
            return Equals(left, right);
        }

        public static bool operator !=(TopicDetails left, TopicDetails right) {
            return !Equals(left, right);
        }
    }
    #endregion
}