using System;
using ActiveMQLibrary;
using Apache.NMS;

namespace ActiveMQExcel
{
    public class TopicHandler
    {
        readonly EventHandler<TopicUpdateEvent> _changed;

        public TopicHandler(EventHandler<TopicUpdateEvent> changed)
        {
            _changed = changed;
        }

        /// <summary>
        /// The subscriber has added the BrokerURL as a property (BrokerUrl).
        /// </summary>
        /// <param name="message"></param>
        public void OnMessageHandler(IMessage message) {
            var textMessage = message as ITextMessage;

            if (textMessage == null) {
                return;
            }

            var brokerUrl = textMessage.Properties.GetString("BrokerUrl");
            var parsedMessage = TextMessageUtil.ExtractRecord(textMessage.Text);
            var destination = textMessage.NMSDestination;
            var topic = destination.ToString();
            // remove the 'topic://'
            if (destination.IsTopic) {
                topic = topic.Substring(8);
            }

            parsedMessage.Add("TopicName", topic);
            parsedMessage.Add("BrokerUrl", brokerUrl);


            _changed(this, new TopicUpdateEvent(parsedMessage));
        }
    }
}
