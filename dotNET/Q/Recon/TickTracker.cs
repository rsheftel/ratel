using System;
using Q.Util;

namespace Q.Recon {
    class TickTracker : Objects {
        DateTime initialTime;
        DateTime lastReceived;
        DateTime marketDataReceivedAt;
        bool cleared = true;
        DateTime systemProcessedAt_;
        double lastMeasuredLag;
        public double tickLag { get { return Math.Max(calcLag(), lastMeasuredLag); } }

        double calcLag() {
            return (cleared ? systemProcessedAt_ : now()).Subtract(marketDataReceivedAt).TotalMilliseconds;
        }

        public void marketDataTicked(DateTime time) {
            if (!cleared || time <= lastReceived) return;
            initialTime = time;
            cleared = false;
            marketDataReceivedAt = now();
        }

        public void systemProcessed(DateTime time) {
            if(cleared || time < initialTime) return;
            lastReceived = time;
            cleared = true;
            systemProcessedAt_ = now();
            lastMeasuredLag = calcLag();
        }

        public DateTime lastTickProcessed() {
            return lastReceived;
        }


    }
}