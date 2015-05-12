using System.Collections.Generic;
using Apache.NMS;

namespace ActiveMQLibrary
{
    public class Publisher
    {
        readonly IMessageProducer _messageProducer;

        /// <summary>
        /// This should be deprecated !!!
        /// </summary>
        public Publisher() {
    
        }

        public Publisher(IMessageProducer messageProducer)
        {
            _messageProducer = messageProducer;
        }

        /// <summary>
        /// Publish to the message to the supplied topic.
        /// 
        /// </summary>
        /// <param name="topic"></param>
        /// <param name="fieldName"></param>
        /// <param name="fieldValue"></param>
        public string Publish(string topic, string fieldName, string fieldValue)
        {
            var textMessage = CreateTextMessage(TextMessageUtil.CreateMessage(topic, fieldName, fieldValue));
            _messageProducer.Send(textMessage);

            return textMessage.NMSMessageId;
        }

        public string Publish(string topic, IDictionary<string, string> messageBody)
        {
            var textMessage = CreateTextMessage(TextMessageUtil.CreateMessage(topic, messageBody));
            _messageProducer.Send(textMessage);

            return textMessage.NMSMessageId;
        }

        IMessage CreateTextMessage(string message)
        {
            return _messageProducer.CreateTextMessage(message);
        }

        public void Close() {
            _messageProducer.Close();
            _messageProducer.Dispose();
        }
    }
}
