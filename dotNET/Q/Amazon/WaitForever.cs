using Q.Util;

namespace Q.Amazon {
    public class WaitForever : Objects {
        public static void Main(string[] args) {
            while(trueDat()) sleep(10000);
        }
    }
}
