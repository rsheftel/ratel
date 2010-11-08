package org.ratel.util;

import static org.ratel.util.Dates.*;
import static org.ratel.util.Log.*;

import java.util.*;

import org.ratel.file.*;

public class ClearLog {
    public static void main(String[] args) {
        QDirectory logs = Systematic.logsDir();
        setFile(logs.file("clear_log.log"));
        deleteOld(logs, 2, ".*\\.old.*");
        deleteOld(logs, 5, ".*\\.log.*");
        deleteOld(logs, 2, ".*\\.out.*");
    }

    private static void deleteOld(QDirectory logs, int bizDaysBack, String pattern) {
        Date oldTime = businessDaysAgo(bizDaysBack, now(), "nyb");
        Log.info("deleting " + pattern + " older than " + ymdHuman(oldTime));
        for (QFile file : logs.files(pattern)) {
            if (file.lastModified().before(oldTime)) {
                Log.info("delete " + file.name());
                file.delete();
            }
        }
    }

}
