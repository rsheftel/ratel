using System;
using System.Collections.Generic;
using jms;
using Q.Util;
using systemdb.data;
using JChannel=jms.Channel;

namespace Q.Messaging {
    public class Topic : Channel {
        static readonly Dictionary<Topic, FieldsListener> listeners = new Dictionary<Topic, FieldsListener>();
        static readonly Dictionary<Topic, Fields> messages = new Dictionary<Topic, Fields>();

        private int maxAttempts = 100;

        public Topic(string topic) : this(topic, true) {}
        public Topic(string topic, bool doRetroactiveConsumer) : this (topic, JChannel.defaultBroker(), doRetroactiveConsumer) {}
        public Topic(string topic, string broker) : this(topic, broker, true) {}
        public Topic(string topic, string broker, bool doRetroactiveConsumer) : this(new QTopic(topic, broker, doRetroactiveConsumer)) {}
        public Topic(JChannel jTopic) : base(jTopic) {}

        public bool has(string fieldName) {
            subscribeIfNeeded();
            return messages.ContainsKey(this) && messages[this].containsKey(fieldName);
        }

        public T get<T>(string fieldName) {
            subscribeIfNeeded();
            var attempts = 0;
            while (attempts++ < maxAttempts && !messages.ContainsKey(this))
                sleep(100);
            Bomb.unless(messages.ContainsKey(this), 
                () => "no message received on " + this + " after " + (maxAttempts / 10) +" seconds of waiting.");
            Bomb.unless(messages[this].containsKey(fieldName), 
                () => "message on " + this + " does not contain key " + fieldName + ".  has: " + messages[this]);
            return field<T>(fieldName, messages[this]);
        }

        public static T field<T>(string fieldName, Fields message) {
            return (T) Convert.ChangeType(message.get(fieldName), typeof (T));
        }

        public void subscribeIfNeeded() {
            lock(listeners) {
                if (listeners.ContainsKey(this)) return;
                var listener = new TopicListener(this);
                listeners[this] = listener;
                jTopic.register(FieldsReceiver.receiver(listener));
            }
        }

        class TopicListener : FieldsListener {
            readonly Topic topic;

            public TopicListener(Topic topic) {
                this.topic = topic;
            }
            public void onMessage(Fields fields) {
                messages[topic] = fields;
            }
        }

        public bool Equals(Topic topic) {
            return topic != null && Equals(jTopic.name(), topic.jTopic.name());
        }

        public override bool Equals(object obj) {
            return ReferenceEquals(this, obj) || Equals(obj as Topic);
        }

        public override int GetHashCode() {
            return jTopic.name().GetHashCode();
        }

        internal static int numListeners() {
            return listeners.Count;
        }

        internal void setMaxAttempts(int newMax) {
            maxAttempts = newMax;
        }

        public void requireFieldsMatch(Topic that) {
            requireFieldsMatch(messages[this], messages[that]);
        }

        public static void clearCache() {
            listeners.Clear();
            messages.Clear();
        }
    }
}