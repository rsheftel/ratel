using System;
using System.Collections.Generic;
using Apache.NMS.ActiveMQ;

namespace ActiveMQLibrary {
    public static class BrokerFactory {
        /// <summary>
        /// Static cache of BrokerFactories.
        /// </summary>
        static readonly IDictionary<string, Broker> _brokerCache = new Dictionary<string, Broker>();
        static readonly log4net.ILog _log =
            log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
        public static int Count {
            get { return _brokerCache.Count; }
        }
        #region FactoryMethod
        public static Broker Broker(string brokerUrl) {
            lock (_brokerCache) {
                if (!_brokerCache.ContainsKey(brokerUrl)) {
                    var failoverUrl = brokerUrl;
                    // Add this back once everyone is upgraded to the correct Apache.NMS version
//                    if (!brokerUrl.StartsWith("failover:")) {
//                        failoverUrl = "failover:" + brokerUrl;
//                    }
                    var factory = new ConnectionFactory(failoverUrl);
                    //var factory = new NMSConnectionFactory(brokerUrl);

                    var connection = factory.CreateConnection();
                    connection.ExceptionListener += OnConnectionExceptionHandler;

                    var broker = new Broker(brokerUrl, connection);
                    connection.ExceptionListener += broker.OnConnectionExceptionHandler;
                    broker.ConnectionFailed += OnBrokerConnectionFailure;

                    _brokerCache.Add(brokerUrl, broker);
                    connection.Start();
                }

                return _brokerCache[brokerUrl];
            }
        }

        static void OnBrokerConnectionFailure(object sender, EventArgs e) {
            
            var failedBroker = sender as Broker;

            if (failedBroker == null) {
                return;
            }
            lock (_brokerCache) {
                _brokerCache.Remove(failedBroker.Url);
                failedBroker.Shutdown();
            }

        }

        public static void Remove(string key) {
            lock (_brokerCache) {
                if (_brokerCache.ContainsKey(key)) _brokerCache.Remove(key);
            }
        }
        #endregion

        #region Error Handling
        static void OnConnectionExceptionHandler(Exception exception) {
            _log.Error("Exception on NMS connection: ", exception);
          
        }
        #endregion
    }
}