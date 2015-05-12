package bloomberg;

import static mail.Email.*;
import static util.Arguments.*;
import static util.Dates.*;
import static util.Log.*;
import static util.Objects.*;
import mail.*;
import util.*;
import file.*;

public class BloombergDownloaderWatcher {

    private final QDirectory toWatch;
    private final String to;

    public BloombergDownloaderWatcher(QDirectory toWatch, String to) {
        this.toWatch = toWatch;
        this.to = to;
    }

    public static void main(String[] in) {
        Arguments args = arguments(in, list("archive", "to")); 
        doNotDebugSqlForever();
        QDirectory archiveDir = new QDirectory(args.get("archive"));
        String to = args.get("to", "us");
        QDirectory asOfDir = archiveDir.directory("" + asLong(now()));
        BloombergDownloaderWatcher watcher = new BloombergDownloaderWatcher(asOfDir, to);
        watcher.check();
    }

    public void check() {
        for(QFile f : toWatch.files()) {
            if (!f.basename().startsWith("BbergFromExcel_")) continue;
            if (f.lastModified().before(minutesAgo(65, now()))) continue;
            return;
        }
        Email e = problem("Bloomberg Data Loader FAILURE", "No Bloomberg file in Archive directory in the last hour!\nCheck that spreadsheets are working properly and that crawler is running.");
        e.sendTo(to);
    }

}
