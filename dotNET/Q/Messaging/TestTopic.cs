using System.Collections.Generic;
using jms;
using NUnit.Framework;
using Q.Util;
using System.Threading;
using System;
using systemdb.data;
using O=Q.Util.Objects;

namespace Q.Messaging {
    [TestFixture]
    public class TestTopic : DbTestCase {
        const string TOPIC_NAME = "TEST.Not.Is.A.Test";

        class TestListener : FieldsListener {
            public Fields received;
            public void onMessage(Fields fields) {
                received = fields;
            }
        }

        [Test]
        public void testPublish() {
            var listener = new TestListener();
            new QTopic(TOPIC_NAME).register(FieldsReceiver.receiver(listener));

            IDictionary<string, object> message = new Dictionary<string, object> {{"foo", 123.45}};
            var topic = new Topic(TOPIC_NAME);

            topic.send(message);
            Objects.sleep(100);
            AreEqual(123.45, listener.received.numeric("foo"));

            topic.send("bar", "string initializedValue");
            Objects.sleep(100);
            AreEqual("string initializedValue", listener.received.get("bar"));

            var data = new string[3, 2];

            data[0, 0] = "key1";
            data[0, 1] = "value1";
            data[1, 0] = "key2";
            data[1, 1] = "value2";
            data[2, 0] = "key3";
            data[2, 1] = "value3";

            topic.send(data);
            Objects.sleep(100);
            AreEqual("value1", listener.received.get("key1"));
            AreEqual("value2", listener.received.get("key2"));
            AreEqual("value3", listener.received.get("key3"));
        }


        [Test]
        public void testSubscribe() {
            var topic = new Topic(TOPIC_NAME);
            string result = null;
            new Thread(delegate() {result = topic.get<string>("key1"); }).Start();
            Objects.sleep(1000);
            topic.send("key1", "value1");
            Objects.sleep(1000);
            AreEqual("value1", result);
        }

        [Test]
        public void testSubscribeWithTwoObjects() {
            var topic1 = new Topic(TOPIC_NAME);
            var topic2 = new Topic(TOPIC_NAME);
            var topic3 = new Topic("TEST.NotExpecting");
            
            AreEqual(0, Topic.numListeners());

            string result1 = null;
            string result2 = null;
            new Thread(delegate() { result1 = topic1.get<string>("key1"); }).Start();
            new Thread(delegate() { result2 = topic2.get<string>("key1"); }).Start();
            waitMatches(1, Topic.numListeners);
            var bombed = false;

            topic3.setMaxAttempts(10);
            new Thread(delegate() {
                try {
                    topic3.get<string>("key1");
                } catch (Exception success) {
                    bombed = true;
                    Matches("no message received", success);
                }
            }).Start();
            waitMatches(2, Topic.numListeners);

            
            topic1.send("key1", "value1");
            Objects.sleep(500);
            waitMatches("value1", ()=> result1);
            waitMatches("value1", ()=> result2);
            waitMatches(true, ()=> bombed);
        }
    }
}
