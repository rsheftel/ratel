package systemdb.live;

import static systemdb.live.LivePublisherUniverseTable.*;
import static transformations.Constants.*;
import static util.Objects.*;
import systemdb.live.LivePublisherUniverseTable.*;
import util.*;
import file.*;

public class BloombergLivePublisher {

    public static void main(String[] args) {
        QFile logFile = Systematic.logsDir().file("bloomberg_publisher.log");
        logFile.deleteIfExists();
        Log.setFile(logFile);
        exitOnUncaughtExceptions(args, FAILURE_ADDRESS, "Bloomberg Live Publisher");
        for(LivePublisherEntry entry : UNIVERSE.entries()) {
            entry.startRepublisher();
        }
        Times.sleep(Long.MAX_VALUE);
        
    }

}
