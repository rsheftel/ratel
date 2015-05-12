package bloomberg;

import static mail.EmailAddress.*;
import static util.Dates.*;
import static util.Log.*;
import db.*;
import file.*;

public class TestBloombergDownloaderWatcher extends DbTestCase {

    private static final QDirectory TEST_WATCH = new QDirectory("./bberg");
    private BloombergDownloaderWatcher watcher;

    @Override protected void setUp() throws Exception {
        info("test watch dir: " + TEST_WATCH.path());
        super.setUp();
        TEST_WATCH.clear();
        TEST_WATCH.file("NotABloombergFile.xls").create("nothing");
        freezeNow();
        watcher = new BloombergDownloaderWatcher(TEST_WATCH, US.address());
        emailer.allowMessages();
    }
    
    @Override protected void tearDown() throws Exception {
        super.tearDown();
        TEST_WATCH.destroyIfExists();
    }

    public void testSendsEmailWhenFileMissing() throws Exception {
        watcher.check();
        emailer.requireSent(1);
        emailer.clear();
        QFile validFile = createFile("2330");
        watcher.check();
        emailer.requireEmpty();
        validFile.file().setLastModified(minutesAgo(66, now()).getTime());
        watcher.check();
        emailer.requireSent(1);
    }

    private QFile createFile(String hhmm) {
        QFile validFile = TEST_WATCH.file("BbergFromExcel_TryAgain_20081111_" + hhmm + ".csv");
        validFile.create("some data");
        return validFile;
    }
    
}
