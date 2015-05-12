using amazon;
using Q.Trading;
using Q.Util;
using util;

namespace Q.Amazon {
    public class FetchSTOParameters : Util.Objects {
        public static void Main(string[] args) {
            LogC.setVerboseLoggingForever(true);
            var arguments = Arguments.arguments(args, jStrings("systemId", "run"));
            var systemId = arguments.integer("systemId");
            var run = arguments.integer("run");

            var bytes = (byte[]) key(systemId, run).read();
            var o = deserialize(bytes);
            var p = o as Parameters;
            if (p == null) info("Object was not of type Parameters, was " + o.GetType().FullName);
            else info("Parameters for " + systemId + "." + run + ": \n" + p);
        }

        public static MetaBucket.Key key(int id, int run) {
            return new EC2Runner("" + id).s3Cache().bucket().key("parameters.", "" + run);
        }
    }
}