using System;
using System.Collections;
using System.Collections.Generic;
using ActiveMQExcel;
using ActiveMQLibrary;
using Apache.NMS;
using NUnit.Framework;

namespace ActiveMQExcelTest {
    [TestFixture] 
    public class TopicHandlerTest {
        string _messageReceived;

        [Test] 
        public void TestHandleMessage() {
            var handler = new TopicHandler(ReceivedMessage);
            var testTextMessage = new MockTextMessage("Test.Topic");
            var testMessage = TextMessageUtil.CreateMessage("Test.Topic", "FieldName", "FieldValue");
            testTextMessage.Text = testMessage;
            testTextMessage.Properties.SetString("BrokerUrl", "tcp://localhost:60606");

            handler.OnMessageHandler(testTextMessage);
            
        }
        private void ReceivedMessage(object sender, EventArgs ea)
        {
            Console.WriteLine("Received message, need to notify");

            _messageReceived = "Received";
        }

    }
    internal class MockTextMessage : ITextMessage {
        readonly IPrimitiveMap _properties = new PrimitiveMap();

        public MockTextMessage(string destination) {
            NMSDestination = new MockDestination(destination);
        }

        /// <summary>
        /// If using client acknowledgement mode on the session, then this method will acknowledge that the
        ///             message has been processed correctly.
        /// </summary>
        public void Acknowledge() {
            throw new NotImplementedException();
        }

        /// <summary>
        /// Provides access to the message properties (headers).
        /// </summary>
        public IPrimitiveMap Properties {
            get { return _properties; }
        }
        /// <summary>
        /// The correlation ID used to correlate messages from conversations or long running business processes.
        /// </summary>
        public string NMSCorrelationID {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        /// <summary>
        /// The destination of the message.  This property is set by the IMessageProducer.
        /// </summary>
        public IDestination NMSDestination { get; private set; }
        /// <summary>
        /// The amount of time for which this message is valid.  Zero if this message does not expire.
        /// </summary>
        public TimeSpan NMSTimeToLive {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        /// <summary>
        /// The message ID which is set by the provider.
        /// </summary>
        public string NMSMessageId {
            get { throw new NotImplementedException(); }
        }
        /// <summary>
        /// Whether or not this message is persistent.
        /// </summary>
        public MsgDeliveryMode NMSDeliveryMode {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        /// <summary>
        /// The Priority of this message.
        /// </summary>
        public MsgPriority NMSPriority {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        /// <summary>
        /// Returns true if this message has been redelivered to this or another consumer before being acknowledged successfully.
        /// </summary>
        public bool NMSRedelivered {
            get { throw new NotImplementedException(); }
        }
        /// <summary>
        /// The destination that the consumer of this message should send replies to
        /// </summary>
        public IDestination NMSReplyTo {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        /// <summary>
        /// The timestamp of when the message was pubished in UTC time.  If the publisher disables setting 
        ///             the timestamp on the message, the time will be set to the start of the UNIX epoc (1970-01-01 00:00:00).
        /// </summary>
        public DateTime NMSTimestamp {
            get { throw new NotImplementedException(); }
        }
        /// <summary>
        /// The type name of this message.
        /// </summary>
        public string NMSType {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public string Text {
            get; set;
        }
    }

    internal class PrimitiveMap : Dictionary<object, object>, IPrimitiveMap {
        public bool Contains(object key) {
            return ContainsKey(key);
        }

        void IPrimitiveMap.Remove(object key) {
            Remove(key);
        }

        public string GetString(string key) {
            return ContainsKey(key) ? base[key].ToString() : null;
        }

        public void SetString(string key, string value) {
            if (ContainsKey(key)) {
                base[key] = value;
            } else {
                Add(key, value);
            }
        }

        public bool GetBool(string key) {
            throw new NotImplementedException();
        }

        public void SetBool(string key, bool value) {
            throw new NotImplementedException();
        }

        public byte GetByte(string key) {
            throw new NotImplementedException();
        }

        public void SetByte(string key, byte value) {
            throw new NotImplementedException();
        }

        public char GetChar(string key) {
            throw new NotImplementedException();
        }

        public void SetChar(string key, char value) {
            throw new NotImplementedException();
        }

        public short GetShort(string key) {
            throw new NotImplementedException();
        }

        public void SetShort(string key, short value) {
            throw new NotImplementedException();
        }

        public int GetInt(string key) {
            throw new NotImplementedException();
        }

        public void SetInt(string key, int value) {
            throw new NotImplementedException();
        }

        public long GetLong(string key) {
            throw new NotImplementedException();
        }

        public void SetLong(string key, long value) {
            throw new NotImplementedException();
        }

        public float GetFloat(string key) {
            throw new NotImplementedException();
        }

        public void SetFloat(string key, float value) {
            throw new NotImplementedException();
        }

        public double GetDouble(string key) {
            throw new NotImplementedException();
        }

        public void SetDouble(string key, double value) {
            throw new NotImplementedException();
        }

        public IList GetList(string key) {
            throw new NotImplementedException();
        }

        public void SetList(string key, IList list) {
            throw new NotImplementedException();
        }

        public IDictionary GetDictionary(string key) {
            throw new NotImplementedException();
        }

        public void SetDictionary(string key, IDictionary dictionary) {
            throw new NotImplementedException();
        }

        ICollection IPrimitiveMap.Keys {
            get { throw new NotImplementedException(); }
        }

        ICollection IPrimitiveMap.Values {
            get { throw new NotImplementedException(); }
        }

        public object this[string key] {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
    }
    internal class MockDestination : IDestination {
        readonly string _topicString;

        public MockDestination(string destination) {
            _topicString = destination;
        }

        public DestinationType DestinationType {
            get { throw new NotImplementedException(); }
        }
        public bool IsTopic {
            get { return true; }
        }
        public bool IsQueue {
            get { throw new NotImplementedException(); }
        }
        public bool IsTemporary {
            get { throw new NotImplementedException(); }
        }

        public override string ToString() {
            return @"topic://" + _topicString;
        }
    }
}