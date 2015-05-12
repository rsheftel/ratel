using System;
using System.Collections.Generic;
using System.Security.Principal;
using System.Threading;
using Apache.NMS;
using Apache.NMS.ActiveMQ;
using FixExecutionAddin.util;

/// <summary>
/// 
/// </summary>
namespace FixExecutionAddin.nms
{
    internal class NmsSession : IConnectable
    {
        IConnection connection;
        ISession session;

        readonly IList<IMessageConsumer> consumers = new List<IMessageConsumer>();
        readonly IList<IMessageProducer> producers = new List<IMessageProducer>();

        string consumerQueue;
        string producerQueue;

        Timer monitorTimer;
        bool connected;

        readonly string replyTo;
        string brokerUrl;

        readonly object lockObject = new object();


        internal NmsSession(string brokerUrl, INmsApplication app) :this (app) {
            this.brokerUrl = brokerUrl;

        }

        internal NmsSession(INmsApplication app) {
            NmsApplication = app;
            var hostName = Environment.MachineName;
            var userName = "Unknown";

            var currentUser = WindowsIdentity.GetCurrent();
            if (currentUser != null) {
                userName = currentUser.Name;
            }

            replyTo = hostName +"-" + userName;
        }

        #region properties
        public INmsApplication NmsApplication { get; set; }
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
        /// Where we will receive our reponses
        /// </summary>
        public string ConsumerQueue {
            get {
                return consumerQueue;
            }

            set {
                consumerQueue = value;
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

        public string ReplyTo {
            get {
                return replyTo;
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
                lock (lockObject) {
                    var factory = new ConnectionFactory(new Uri(brokerUrl));
                    connection = factory.CreateConnection();
                    connection.ExceptionListener += OnExceptionHandler;
                    session = connection.CreateSession(AcknowledgementMode.AutoAcknowledge);

                    // create my consumer that uses a selector on the replyTo
                    var selector = "JMSCorrelationID = '" + replyTo + "'";
                    var consumer = session.CreateConsumer(session.GetQueue(consumerQueue), selector);
                    consumer.Listener += NmsApplication.InboundApp;

                    Console.WriteLine("Created consumer with selector of:"+ selector);
                    consumers.Add(consumer);

                    // create the producer that sends orders
                    var producer = session.CreateProducer(session.GetQueue(producerQueue));
                    producers.Add(producer);

                    connection.Start();
                    Connected = true;
                }
            } catch (Exception e) {
                //LogC.consoleOut(e.Message);
                Console.WriteLine(e);
            }
        }

        /// <summary>
        /// Dispose everything that we have opened.
        /// </summary>
        void Disconnect() {
            lock (lockObject) {
                foreach (var producer in producers) {
                    try {
                        producer.Dispose();
                    } catch (Exception e) {
                        Console.WriteLine(e);
                    }
                }
                foreach (var consumer in consumers) {
                    try {
                        consumer.Dispose();
                    } catch (Exception e) {
                        Console.WriteLine(e);
                    }
                }
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
            textMessage.Properties.SetString("ReplyTo", replyTo);

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
    }
}
