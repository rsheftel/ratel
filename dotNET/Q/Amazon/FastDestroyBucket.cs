using System.Threading;
using amazon;
using util;

namespace Q.Amazon {
    public class FastDestroyBucket : Util.Objects {
        public static void Main(string[] args) {
            var bucketName = args[0];
            var bucket = new MetaBucket(bucketName);
            foreach(var key in list<MetaBucket.Key>(bucket.keys(null))) {
                var tempKey = key;
                ThreadPool.QueueUserWorkItem(o => {
                    tempKey.delete();
                    Log.dot();
                });
            }
            wait(600, 6000, () => {
                var size = bucket.keys(null).size();
                info("keys remaining: " + size);
                return size == 0;
            });
        }
    }
}
