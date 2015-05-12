package bloomberg;

import static futures.BloombergJobTable.*;
import static util.Dates.*;
import static util.Log.*;
import static util.Objects.*;

import java.util.*;

import db.*;
import futures.*;

public class BloombergDirectDownloader {
    public void run() {
        List<BloombergJob> toRun = BLOOMBERG_JOBS.due();
        for (BloombergJob job : toRun) {
            info("running " + job);
            List<BloombergJobEntry> entries = job.entries();
            for (BloombergJobEntry entry : entries) {
                if(job.hasObservation(entry)) continue;
                BloombergSecurity security = entry.security();
                String field = entry.field();
                Date time = job.observationTime();
                double value;
                try {
                    if(list("LAST_PRICE", "BID", "ASK").contains(field)) 
                        value = security.numeric(field, time);
                    else if(field.equals("SETTLE_DT"))
                        value = asLong(date(security.string(field)));
                    else
                        value = security.numeric(field);
                } catch(Exception e) {
                    info("Skipping " + entry + "...");
                    continue;
                }
                info(entry + " " + ymdHuman(time) + " = " + value);
                entry.writeObservation(time, value);
            }
            job.updateLastRunOn();
        }
    }
    
    public static void main(String[] args) {
        freezeNow();
        doNotDebugSqlForever();
        //BloombergJobEntry.forceSourceToTestForTesting = true;
        new BloombergDirectDownloader().run();
        Db.commit();
        System.exit(0);
    }
}