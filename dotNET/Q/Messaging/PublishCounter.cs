using System.Collections.Generic;
using Q.Util;
using systemdb.data;

namespace Q.Messaging {
    public class PublishCounter : Objects, FieldsListener {
        readonly Topic topic;
        readonly List<Fields> messages = new List<Fields>();

        public PublishCounter(Topic topic) {
            this.topic = topic;
            topic.jChannel().register(FieldsReceiver.receiver(this));
            topic.subscribeIfNeeded();
        }

        public PublishCounter(string topic) : this(new Topic(topic)) {}

        public void onMessage(Fields message) {
            LogC.info("received " + message);
            messages.Add(message);
        }

        public T getOneAndClear<T>(string field) {
            var result = getOne<T>(field);
            clear();
            return result;
        }

        public void clear() {
            messages.Clear();
        }

        public void clearOne() {
            requireOne();
            clear();
        }

        void requireOne() {
            requireCount(1);
        }

        public void requireCount(int expected) {
            Bomb.when(messages.Count > expected, () => "too many messages received on " + topic.name());
            zeroTo(30, i => { if (messages.Count < expected) sleep(100); });
            Bomb.when(messages.Count < expected, () => "not enough messages received on " + topic.name());
        }

        public T getOne<T>(string field) {
            requireOne();
            return get<T>(field);
        }

        public T get<T>(string field) {
            return get<T>(0, field);
        }

        public T get<T>(int skip, string field) {
            Bomb.unless(skip < messages.Count, () => "can't skip " + skip + " not enough messages received on \r\n" + topic.name());
            return Topic.field<T>(field, messages[skip]);
        }

        public void requireNoMessages() {
            Bomb.unless(isEmpty(messages), () => "required to be empty, but received a message on " + topic.name());
        }

        public void requireFieldsMatch(PublishCounter entry) {
            requireOne();
            entry.requireOne();
            Channel.requireFieldsMatch(the(messages), the(entry.messages));
        }
    }
}