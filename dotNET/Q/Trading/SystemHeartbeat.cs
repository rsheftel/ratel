using System;
using System.Collections.Generic;
using System.Threading;
using Q.Messaging;
using Q.Trading.Results;
using Q.Util;
using systemdb.metadata;

namespace Q.Trading {
    public class SystemHeartbeat : Objects {
        public static int defaultWaitMillis = 1000;
        public static int waitTimeMillis = defaultWaitMillis;
        readonly Topic topic;
        int ticks;
        DateTime lastTick;
        Timer timer;
        public static readonly string SUFFIX = "heartbeat";

        public SystemHeartbeat(LiveSystem liveSystem, Collectible symbol, string topicPrefix) {
            topic = new Topic(liveSystem.topicName(topicPrefix, symbol.name + "." + SUFFIX));
        }

        public void tickProcessed(Tick tick) {
            ticks++;
            lastTick = tick.time;
        }

        public void goLive() {
            if(LiveTradeMonitor.inNoPublishMode()) 
                return;
            timerManager().everyMillis(waitTimeMillis, () => topic.send(new Dictionary<string, object> {
                {"hostname", hostname()},
                {"ticksReceived", ticks},
                {"lastTickProcessed", ymdHuman(lastTick)},
                {"timestamp", ymdHuman(now())}
            }), out timer);
        }

        public void stop() {
            if(timer == null) return;
            timer.Dispose();
            timer = null;
        }
    }
}