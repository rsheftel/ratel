using System;
using System.Collections.Generic;
using System.Net.Sockets;
using System.Text;
using Apache.NMS;
using Apache.NMS.ActiveMQ;

namespace RediToActiveMQ
{

    /// <summary>
    /// Single threaded multiple topic publisher.
    /// 
    /// We will be publishing on one thread to multiple topics.  This will allow us to use one session for 
    /// multiple topics.  If we want to publish from multiple threads, we will need to create multiple
    /// sessions.
    /// </summary>
    public class ActiveMQPublisher
    {
        readonly Uri brokerUri;
        IConnection brokerConnection;
        ISession producerSession;

        readonly object lockObject = new object();

        static readonly log4net.ILog log =
            log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

        IDictionary<string, IMessageProducer> producers = new Dictionary<string, IMessageProducer>(); 
        #region Constructors
        public ActiveMQPublisher(Uri brokerUrl) {
            brokerUri = brokerUrl;
        }
        #endregion

        #region Properties
        public bool IsConnected {
            get {
                lock (lockObject) {
                    return brokerConnection != null && brokerConnection.IsStarted;
                }
            }
        }
        #endregion

        public void Connect() {
            lock (lockObject) {
                if (IsConnected) return;
                try {
                    var factory = new ConnectionFactory(brokerUri);
                    brokerConnection = factory.CreateConnection();
                    brokerConnection.ExceptionListener += OnExceptionHandler;

                    producerSession = brokerConnection.CreateSession(AcknowledgementMode.AutoAcknowledge);

                    brokerConnection.Start();
                } catch (SocketException e) {
                    log.Error("Unable to connect to ActiveMQ using: " + brokerUri, e);
                }
            }
        }

        public void Disconnect()
        {
            lock (lockObject) {
                foreach (var producer in producers) {
                    producer.Value.Dispose();
                }
                producers.Clear();

                if (producerSession != null) {
                    producerSession.Dispose();
                }

                if (brokerConnection == null) return;

                brokerConnection.Stop();
                brokerConnection.Close();
                brokerConnection = null;
            }
        }

        public string Publish(string topicName, IDictionary<string, string> message) {
            if (!IsConnected) return "NotSent";

            IMessageProducer topicPublisher;

            if (producers.ContainsKey(topicName)) {
                topicPublisher = producers[topicName];
            } else {
                topicPublisher = producerSession.CreateProducer(producerSession.GetTopic(topicName));
                producers[topicName] = topicPublisher;
            }

            if (!message.ContainsKey("TIMESTAMP")) {
                message.Add("TIMESTAMP", DateTime.Now.ToString("yyyy/MM/dd HH:mm:ss"));
            }

            var nmsMessage = topicPublisher.CreateTextMessage(CreateMessage(message));

            topicPublisher.Send(nmsMessage);

            return nmsMessage.NMSMessageId;
        }

        public string SendHeartbeat(string userId, string productVersion, bool rediRunning, bool positionConnected, DateTime? lastPosition)
        {
            var topicName = "Redi.Heartbeat." + userId;
            var tmp = "";

            if (lastPosition != null) {
                var dt = (DateTime) lastPosition;
                tmp = dt.ToString("yyyy/MM/dd HH:mm:ss");
            }

            var message = new Dictionary<string, string> {{"USERID", userId}, {"APPVERSION", productVersion},
            {"REDIRUNNING", rediRunning.ToString()}, {"POSITIONLISTENER", positionConnected.ToString()},
            {"LASTPOSITON", tmp}};

            return Publish(topicName, message);
        }

        #region Static Helper Methods
        static string CreateMessage(IEnumerable<KeyValuePair<string, string>> message)
        {
            var sb = new StringBuilder(1024);

            foreach (var kvp in message) {
                sb.Append(kvp.Key).Append("=").Append(kvp.Value).Append("|");
            }

            return sb.ToString();
        }
        #endregion

        #region NMS Events
        void OnExceptionHandler(Exception exception) {
            // NMS does not support failover, so we must implement it ourselves
            var localCon = brokerConnection;
            brokerConnection = null; // This helps out the UI display the correct status
            var localProducers = producers;
            producers = new Dictionary<string, IMessageProducer>();

            foreach (var producer in localProducers) {
                producer.Value.Dispose();
            }

            localCon.Stop();
            localCon.Close();

            log.Error("ActiveMQ Connection Error.", exception);
        }
        #endregion

    }
}
