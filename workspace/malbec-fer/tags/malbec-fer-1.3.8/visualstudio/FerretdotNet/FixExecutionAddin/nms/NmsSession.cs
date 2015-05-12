using System;
using System.Collections.Generic;
using System.Threading;
using Apache.NMS;
using Apache.NMS.ActiveMQ;
using FixExecutionAddin.Util;

/// <summary>
/// 
/// </summary>
namespace FixExecutionAddin.Nms
{
    internal class NmsSession : IConnectable
    {
        IConnection connection;
        ISession session;

        //readonly IList<IMessageConsumer> consumers = new List<IMessageConsumer>();
        readonly IDictionary<string, IMessageConsumer> consumers = new Dictionary<string, IMessageConsumer>();
        readonly IList<IMessageProducer> producers = new List<IMessageProducer>();

        IDestination consumerQueue;
        string producerQueue;

        public const string topicPrefix = "FER.Order.Response";

        private INmsApplication nmsApp; 

        Timer monitorTimer;
        bool connected;

        string brokerUrl;

        readonly object lockObject = new object();


        internal NmsSession(string brokerUrl, INmsApplication app) :this (app) {
            this.brokerUrl = brokerUrl;
        }

        internal NmsSession(INmsApplication app) {
            nmsApp = app;
        }

        #region properties
        public INmsApplication NmsApplication { 
            get {
                return nmsApp;
            }
            set {
                nmsApp = value;
            } 
        }


        /// <summary>
        /// The URL of the broker that this NMS Session is connected to.
        /// </summary>
        public string BrokerUrl {
            get {
                return brokerUrl;
            }

            set {
                brokerUrl = value;
            }
        }


        /// <summary>
        /// Where we send our order requests
        /// </summary>
        public string ProducerQueue {
            get {
                return producerQueue;
            }

            set {
                producerQueue = value;
            }
        }

        internal bool Connected {
            get {
                lock (lockObject) {
                    return connected;
                }
            }
            set {
                lock (lockObject) {
                    connected = value;
                }
            }
        }

        #endregion

        internal void Start() {
            if (monitorTimer == null) {
                // startup after half second and call every second after that
                monitorTimer = new Timer(MonitorConnection, null, 500, 1000);
            }
        }

        internal void Stop() {
            if (monitorTimer != null) {
                monitorTimer.Dispose();
                monitorTimer = null;
            }
            Disconnect();
        }

        void Connect() {
            try {
                lock (nmsApp) {
                    if (Connected) return;
                    var factory = new ConnectionFactory(new Uri(brokerUrl));
                    connection = factory.CreateConnection();
                    connection.ExceptionListener += OnExceptionHandler;
                    session = connection.CreateSession(AcknowledgementMode.AutoAcknowledge);

                    // create my consumer that uses a selector on the replyTo
                    consumerQueue = session.CreateTemporaryQueue();
                    var consumer = session.CreateConsumer(consumerQueue);
                    consumer.Listener += NmsApplication.InboundApp;
                    // create a wildcard topic consumer
                    //
//                    var topicStatus = session.GetTopic(topicPrefix);
//                    var topicConsumer = session.CreateConsumer(topicStatus);
//                    topicConsumer.Listener += NmsApplication.InboundApp;

                    //log.Info("Created consumer with selector of:"+ selector);
                    consumers["TempQueue"] = consumer;
//                    consumers.Add(topicConsumer);

                    // create the producer that sends orders
                    var producer = session.CreateProducer(session.GetQueue(producerQueue));
                    producers.Add(producer);

                    connection.Start();
                    Connected = true;
                    Console.WriteLine("Connected to broker-NmsSession");
                }
            } catch (Exception e) {
                Console.WriteLine(e);
            }
        }

        /// <summary>
        /// Dispose everything that we have opened.
        /// </summary>
        void Disconnect() {
            lock (nmsApp) {
                foreach (var producer in producers) {
                    try {
                        producer.Dispose();
                    } catch (Exception e) {
                        Console.WriteLine(e);
                    }
                }
                producers.Clear();
                foreach (var consumer in consumers.Values) {
                    try {
                        consumer.Dispose();
                    } catch (Exception e) {
                        Console.WriteLine(e);
                    }
                }
                consumers.Clear();
                try {
                    session.Dispose();
                } catch (Exception e) {
                    Console.WriteLine(e);
                }
                try {
                    connection.Dispose();
                } catch (Exception e) {
                    Console.WriteLine(e);
                }
                Connected = false;
            }
        }

        void MonitorConnection(object state) {
            if (!Connected) {
                Connect();
            }
        }

        #region nms events
        public void OnExceptionHandler(Exception exception)
        {
            Console.WriteLine(exception);
            lock (lockObject) {
                // remove the exception listener, as we might get into an 
                // endless loop of exception handling
                connection.ExceptionListener -= OnExceptionHandler;
            }

            Disconnect();
        }

        #endregion
        bool IConnectable.Connected() {
            return Connected;
        }

        public ITextMessage SendMessageResponse(IDictionary<string, string> order)
        {
            var textMessage = session.CreateTextMessage(MessageUtil.CreateMessage(order));
            textMessage.NMSReplyTo = consumerQueue;

            Send(textMessage);
            return textMessage;
        }

        internal bool Send(IMessage message) {

            try {
                var producer = producers[0];
                producer.Send(message);
                return true;
            } catch (Exception e) {
                Console.WriteLine(e);
            }
            return false;
        }

        public void SubscribeToTopic(string topicString) {
            var topicKey = topicPrefix + "." + topicString +".>";

            if (consumers.ContainsKey(topicKey)) return;

            var topicStatus = session.GetTopic(topicKey);
            var topicConsumer = session.CreateConsumer(topicStatus);
            topicConsumer.Listener += NmsApplication.InboundApp;
            consumers[topicKey] = topicConsumer;
        }
    }
}
