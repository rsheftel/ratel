using System;
using jms;
using Q.Util;
using systemdb.data;
using JChannel = jms.Channel;

namespace Q.Messaging {
    public class Queue : Channel {
        public Queue(string name) : this(name, JChannel.defaultBroker()) {}
        public Queue(string name, string broker) : this(new QQueue(name, broker)) {}
        public Queue(JChannel queue) : base(queue) {}

        public void subscribe(Converter<Fields, Fields> responder) {
            jTopic.register(FieldsResponder.responder(new ConverterResponder(responder)));
        }

        public void response(Fields request, Action<Fields> onResponse) {
            ((QQueue) jTopic).response(request.messageText(), ActionListener.receiver(onResponse), 500);
        }
    }

    internal class ConverterResponder : FieldsResponderListener {
        readonly Converter<Fields, Fields> makeResponse;

        public ConverterResponder(Converter<Fields, Fields> makeResponse) {
            this.makeResponse = makeResponse;
        }

        public Fields reply(Fields fields) {
            try {
                return makeResponse(fields);
            } catch (Exception e) {
                throw Bomb.toss(LogC.errMessage("caught exception in responder", e));
            }
        }
    }
}
