package db;

import static util.Dates.*;
import static util.Log.*;
import static util.Objects.*;
import static util.Strings.*;
import static util.Systematic.*;
import static util.Times.*;

import java.util.*;

import util.*;
import db.clause.*;
import db.tables.ScheduleDB.*;

public class TestLocksTable extends TestLocksBase {
    public static final String NONE = "NONE";
    public static TestLocksTable TEST_LOCK = new TestLocksTable();
    public TestLocksTable() { super("locks"); }
    private static final long serialVersionUID = 1L;
    
    public static void main(String[] args) {
        Arguments arguments = Arguments.arguments(args, list("lockName", "who"));
        doNotDebugSqlForever();
        boolean doWho = arguments.get("who", false);
        if (doWho) {
            lineEnd("");
            List<Row> rows = TEST_LOCK.rows();
            if (isEmpty(rows)) linePart("No test locks.");
            else for(Row r : rows) {
                linePart(sprintf("%-30s %-10s %-10s %-8s %-4s",
                   r.value(TEST_LOCK.C_TESTNAME),
                   r.value(TEST_LOCK.C_USERNAME),
                   r.value(TEST_LOCK.C_HOSTNAME),
                   ymdHuman(r.value(TEST_LOCK.C_LOCK_TIME)).replaceFirst(".* ", ""),
                   r.value(TEST_LOCK.C_LOCK_NAME)
                ));
                lineEnd("");
            }
            System.exit(0);
        }
        TEST_LOCK.releaseAll(arguments.get("lockName", "test"));
        Db.commit();
    }
    
    private void releaseAll(String lockName) {
        deleteAll(C_LOCK_NAME.is(lockName));
    }   

    public void acquireLock(String lock, String info ) {
        while (true) {
            String locked = tryAcquireLockOnce(lock, info);
            if (locked.equals(NONE)) return;
            Log.info("Waiting for test lock: " + locked);
            sleep(350);
        }
    }

    public String tryAcquireLockOnce(final String lock, final String info) {
        final String[] result = new String[1];
        Thread t = new Thread() {
            @Override public void run() {
                try {
                    Log.doNotDebugSqlForever();
                    insert(
                        C_LOCK_NAME.with(lock), 
                        C_LOCK_TIME.now(), 
                        C_TESTNAME.with(info),
                        C_HOSTNAME.with(hostname()),
                        C_USERNAME.with(username())
                    );
                    Db.reallyCommit();
                    result[0] = NONE;
                } catch (RuntimeException e) {
                    Db.reallyRollback();
                    List<Row> lockInfo = lockInfo(lock);
                    result[0]= lockInfo.toString();
                    if(hasContent(lockInfo) && C_LOCK_TIME.value(the(lockInfo)).before(minutesAgo(5, now()))) {
                        result[0] = "lock has expired.  breaking lock: " + result[0];
                        releaseLock(lock, true);
                    }
                } finally {
                    Log.restoreSqlDebugging();
                }
            }
        };
        t.start();
        join(t);
        return result[0];
    }

    private List<Row> lockInfo(String lock) {
        return rows(C_LOCK_NAME.is(lock));
    }

    public void releaseLock(final String lock, final boolean breakOthersLocks) {
        Db.doInSidestepTransaction(new SidestepThreadResult<Boolean>(true) {
            @Override public Boolean result() {
                Clause hostUserMatches = C_HOSTNAME.is(hostname()).and(C_USERNAME.is(username()));
                Clause matches = C_LOCK_NAME.is(lock);
                if (!breakOthersLocks) matches = matches.and(hostUserMatches);
                deleteOne(matches);
                Db.reallyCommit();
                return Boolean.TRUE;
            }
        });
    }
}