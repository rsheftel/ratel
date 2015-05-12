using System;
using System.Collections.Generic;
using System.Reflection;
using Apache.NMS;

namespace ActiveMQLibrary
{
    public class Broker
    {
        readonly log4net.ILog _log = log4net.LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);

        readonly IDictionary<string, Publisher> _publisherCache = new Dictionary<string, Publisher>();
        readonly IDictionary<string, Subscriber> _subscriberCache = new Dictionary<string, Subscriber>();

        private const string Retroactive = "?consumer.retroactive=true";

        public delegate void ConnectionFailedEventHandler(object sender, EventArgs e);


        public Broker(string url, IConnection connection)
        {
            Url = url;
            Connection = connection;
        }

        public string Url { get; private set; }
        public IConnection Connection { get; private set; }

        public int PublisherCount
        {
            get { return _publisherCache.Count; }
        }
        public int SubscriberCount
        {
            get { return _subscriberCache.Count; }
        }
        public bool HasConnectionFailed
        {
            get; private set;

        }

        public Publisher TopicPublisher(string topic)
        {
            lock (_publisherCache) {
                if (!_publisherCache.ContainsKey(topic)) {
                    var session = Connection.CreateSession(AcknowledgementMode.AutoAcknowledge);
                    var producer = session.CreateProducer(session.GetTopic(topic));
                    var publisher = new Publisher(producer);

                    _publisherCache.Add(topic, publisher);
                }
                return _publisherCache[topic];
            }
        }

        public Subscriber TopicSubscriber(string topicText, MessageListener onMessageHandler)
        {
            lock (_subscriberCache) {
                if (!_subscriberCache.ContainsKey(topicText)) {
                    var session = Connection.CreateSession(AcknowledgementMode.AutoAcknowledge);
                    IDestination topic = session.GetTopic(topicText + Retroactive);
                    var consumer = session.CreateConsumer(topic);
                    var subscriber = new Subscriber(Url, consumer, onMessageHandler);

                    _subscriberCache.Add(topicText, subscriber);
                }

                return _subscriberCache[topicText];
            }
        }

        public void RemoveSubscriber(string topicText)
        {
            lock (_subscriberCache) {
                if (!_subscriberCache.ContainsKey(topicText)) {
                    return;
                }

                var subscriber = _subscriberCache[topicText];
                _subscriberCache.Remove(topicText);
                
                subscriber.Close();
            }
        }

        public void OnConnectionExceptionHandler(Exception exception) {
            // Assume the worst and close everything.  The user will resubscribe/republish
            _log.Error("Processing connection error", exception);

            HasConnectionFailed = true;

            if (ConnectionFailed != null) {
                ConnectionFailed(this, EventArgs.Empty);
            }
        }

        public event ConnectionFailedEventHandler ConnectionFailed;

        public void Shutdown() {
            lock (_publisherCache) {
                foreach (var publisher in _publisherCache) {
                    publisher.Value.Close();
                }

                _publisherCache.Clear();
            }

            lock (_subscriberCache) {
                foreach (var subscriber in _subscriberCache) {
                    subscriber.Value.Close();
                }
                _subscriberCache.Clear();
            }
        }
    }
}