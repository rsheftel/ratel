package db;

import util.*;

public abstract class SidestepThreadResult<T> {
    private final boolean hideSqlLogging;
    public SidestepThreadResult(boolean hideSqlLogging) {
        this.hideSqlLogging = hideSqlLogging;
    }

    public T resultProtected() {
        try {
            if (hideSqlLogging) Log.doNotDebugSqlForever();
            return result();
        } finally {
            Log.restoreSqlDebugging();
        }
    }
    public abstract T result();
}
