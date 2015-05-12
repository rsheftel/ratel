using util;

namespace Q.Amazon {
    public class RunTonsOfRun : Util.Objects {
        public static void Main(string[] args) {
            var numInstances = 1;
            var systemId = 356297;
            var run = 10930;
            freezeNow("2009/06/30 13:28:31");
            var stoRunner = new STOClient.STORunner(numInstances, systemId, run, run, true);
            Dates.thawNow();
            zeroTo(100, i => stoRunner.enqueueRun(run));
        }
    }
}
