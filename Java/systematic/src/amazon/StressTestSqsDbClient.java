package amazon;

import static util.Arguments.*;
import static util.Log.*;
import static util.Objects.*;
import static util.Sequence.*;
import static util.Times.*;
import systemdb.metadata.*;
import util.*;

public class StressTestSqsDbClient {
    static int count = 0;
    static long startTime;
    
    public static void main(String[] arguments) {
        S3Cache.setDefaultSqsDbMode(true);
        S3Cache.beInSqsDbMode(true);
        Arguments args = arguments(arguments, list("nthreads"));
        int nthreads = args.get("nthreads", 4);
        exitOnUncaughtExceptions(arguments, "us", "SqsDbClient");
        startTime = nowMillis();
        for (int i : zeroTo(nthreads)) {
            info("starting thread " + i);
            new Thread() {
                @Override public void run() {
                    while(true) {
                        long start = nowMillis();
                        new Market("RE.TEST.TY.1C");
                        long millis = reallyMillisSince(start);
                        count++;
                        info("query took " + millis + " millis, throughput = " + count / reallySecondsSince(startTime) + " qps");
                    }
                }
            }.start();
        }
    }
}
