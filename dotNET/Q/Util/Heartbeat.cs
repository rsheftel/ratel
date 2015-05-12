using System;
using System.Threading;
using Q.Messaging;
using systemdb.data;

namespace Q.Util {
    public class Heartbeat : Objects, IDisposable {
        readonly Topic outTopic;
        readonly Topic inTopic;
        readonly long waitMillis;
        readonly Action<Fields> augment;
        Timer timer;

        public Heartbeat(string broker, string topic, long waitMillis, Action<Fields> augment) {
            outTopic = new Topic(topic + "." + hostname(), broker);
            inTopic = new Topic(topic + ".>", broker);
            this.waitMillis = waitMillis;
            this.augment = augment;
        }

        public Heartbeat(string broker, string topic, int millis) : this(broker, topic, millis, fields=> { }) {}

        public void initiate() {
            timerManager().everyMillis(waitMillis, publish, out timer);
            LogC.verbose(() => "initiated " + timer);
        }

        void publish() {
            var fields = new Fields();
            fields.put("Hostname", hostname());
            fields.put("Timestamp", ymdHuman(now()));
            augment(fields);
            outTopic.send(fields);
        }

        public void subscribe(Action<Fields> onBeat) {
            inTopic.subscribe(onBeat);
        }

        public void Dispose() {
            timer.Dispose();
            timer = null;
        }
    }
}