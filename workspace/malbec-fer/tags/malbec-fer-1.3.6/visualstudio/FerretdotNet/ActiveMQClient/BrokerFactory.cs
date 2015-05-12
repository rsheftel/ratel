using System;
using System.Collections.Generic;
using System.Text;
using Apache.NMS;
using Apache.NMS.ActiveMQ;

namespace ActiveMQClient
{
    public class BrokerFactory
    {
        readonly log4net.ILog _log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

        /// <summary>
        /// Static cache of BrokerFactories.
        /// </summary>
        static readonly IDictionary<string, BrokerFactory> _connectionCache = new Dictionary<string, BrokerFactory>();

        /// <summary>
        /// Instance level cache of broker producers.
        /// </summary>
        private readonly IDictionary<string, IMessageProducer> _producerCache = new Dictionary<string, IMessageProducer>();
        string _factoryKey;

        BrokerFactory(string brokerUrl) {
            _factoryKey = brokerUrl;
        }

        #region FactoryMethod
        public static BrokerFactory Broker(string brokerUrl) {
            lock (_connectionCache) {
                if (!_connectionCache.ContainsKey(brokerUrl)) {

                    var brokerFactory = new BrokerFactory(brokerUrl);

                    var factory = new ConnectionFactory(brokerUrl);
                    //var factory = new NMSConnectionFactory(brokerUrl);
                    var connection = factory.CreateConnection();
                    brokerFactory.Connection = connection;
                    connection.ExceptionListener += brokerFactory.OnConnectionExceptionHandler;

                    _connectionCache.Add(brokerUrl, brokerFactory);
                    brokerFactory.Connection.Start();
                }

                return _connectionCache[brokerUrl];
            }
        }
        public static void Remove(string key)
        {
            lock (_connectionCache) {
                if (_connectionCache.ContainsKey(key)) {
                    _connectionCache.Remove(key);
                }
            }
        }
        #endregion

        IConnection Connection {
            get; set;
        }
        public bool IsStarted {
            get { return Connection != null && Connection.IsStarted; }
        }

        void OnConnectionExceptionHandler(Exception exception) {
            _log.Error("Exception on NMS connection: ", exception);
        }

        public string Publish(string topicName, IDictionary<string, string> message) {
            try {
                lock (_producerCache) {
                    if (!_producerCache.ContainsKey(topicName)) {
                        var producerSession = Connection.CreateSession(AcknowledgementMode.AutoAcknowledge);
                        var newProducer = producerSession.CreateProducer(producerSession.GetTopic(topicName));

                        _producerCache.Add(topicName, newProducer);
                    }

                    var producer = _producerCache[topicName];
                    if (!message.ContainsKey("TIMESTAMP")) {
                        message.Add("TIMESTAMP", DateTime.Now.ToString("yyyy/MM/dd HH:mm:ss"));
                    }

                    var nmsMessage = producer.CreateTextMessage(CreateMessage(message));
                    producer.Send(nmsMessage);


                    return nmsMessage.NMSMessageId;
                }
            } catch (Exception exception) {
                _log.Error("Processing updates from TradingScreen", exception);
                Console.WriteLine(exception.Message);
                return "Not sent: " + exception.Message;
            }
        }

        #region Static Helper Methods
        static string CreateMessage(IEnumerable<KeyValuePair<string, string>> message)
        {
            var sb = new StringBuilder(1024);

            foreach (var kvp in message) {
                sb.Append(kvp.Key.ToUpperInvariant()).Append("=").Append(kvp.Value).Append("|");
            }

            return sb.ToString();
        }
        #endregion
        public void Shutdown() {
            // stop all producers and close the connection
            // should also remove from the map
            foreach (var producer in _producerCache) {
                producer.Value.Dispose();
            }
            _producerCache.Clear();

            Connection.Stop();
            Connection.Close();
            Remove(_factoryKey);
        }

      
    }
}
