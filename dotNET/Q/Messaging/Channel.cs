using System;
using System.Collections.Generic;
using jms;
using Q.Util;
using systemdb.data;
using JChannel=jms.Channel;

namespace Q.Messaging {
    public class Channel : Objects {
        protected JChannel jTopic;

        public Channel(JChannel jTopic) {
            this.jTopic = jTopic;
        }

        public override string ToString() {
            return jTopic.name();
        }

        public void send(IDictionary<string, object> message) {
            var f = new Fields();
            foreach (var keyValue in message) 
                f.put(keyValue.Key, keyValue.Value.ToString());
            if(reDebug()) LogC.info("sending to " + this + ": " + f.messageText());
            jTopic.send(f);
        }

        public void send(string fieldName, string fieldValue) {
            send(dictionaryOne(fieldName, (object) fieldValue));
        }

        public void send(string[,] keyValues) {
            IDictionary<string, object> message = new Dictionary<string, object>();

            for (var i = keyValues.GetLowerBound(0); i <= keyValues.GetUpperBound(0); i++)
                message.Add(keyValues[i, 0], keyValues[i, 1]);
            send(message);
        }

        public void subscribe(Action<Fields> onFields) {
            jTopic.register(FieldsReceiver.receiver(new ActionListener(onFields)));
        }

        public class ActionListener : FieldsListener {
            readonly Action<Fields> onFields;

            public ActionListener(Action<Fields> onFields) {
                this.onFields = onFields;
            }

            public void onMessage(Fields fields) {
                try {
                    onFields(fields);
                } catch(Exception e) {
                    LogC.err("exception caught processing message: " + fields, e);
                    throw;
                }

            }

            public static MessageReceiver receiver(Action<Fields> action) {
                return FieldsReceiver.receiver(new ActionListener(action));
            }
        }

        public JChannel jChannel() {
            return jTopic;
        }

        public static void requireFieldsMatch(Fields thisFields, Fields thatFields) {
            var thisMessage = thisFields.copy();
            thisMessage.remove("MSTopicName");
            thisMessage.remove("MSTimestamp");
            var thatMessage = thatFields.copy();
            thatMessage.remove("MSTopicName");
            thatMessage.remove("MSTimestamp");
            Bomb.unless(thisMessage.Equals(thatMessage), 
                () => "fields do not match\nThis:\n" + thisMessage + "\nThat:\n" + thatMessage
            );
        }

        public string name() {
            return jChannel().name();
        }

        public void send(Fields fields) {
            jChannel().send(fields);
        }

        public void setReadonly(bool newValue) {
            jChannel().setReadonly(newValue);
        }
    }
}