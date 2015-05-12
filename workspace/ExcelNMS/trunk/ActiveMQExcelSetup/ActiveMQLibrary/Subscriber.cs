using Apache.NMS;

namespace ActiveMQLibrary
{
    public class Subscriber
    {
        readonly IMessageConsumer _messageConsumer;
        readonly string _brokerUrl;
        readonly MessageListener _messageListener;


        public Subscriber(string brokerUrl, IMessageConsumer messageConsumer, MessageListener listener) {
            _messageConsumer = messageConsumer;
            _brokerUrl = brokerUrl;
            _messageListener = listener;
            _messageConsumer.Listener += OurMessageListenerAdapter;
        }

        void OurMessageListenerAdapter(IMessage message) {
            message.Properties.SetString("BrokerUrl", _brokerUrl);
            _messageListener(message);
        }

        public void Close() {
            _messageConsumer.Close();
            _messageConsumer.Dispose();
        }
    }
}
