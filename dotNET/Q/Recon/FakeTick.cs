using db;
using Q.Util;

namespace Q.Recon {
    public class FakeTick : Objects {
        public static void Main(string[] args) {
            TestOrderTracker.insertSimFilled(39, "RE.TEST.TU.1C", "test");
            Db.commit();
            OrderTable.TOPIC.send("timestamp", ymdHuman(now()));
        }
    }
}