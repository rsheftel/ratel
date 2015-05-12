package malbec.util;

import java.sql.SQLException;

public class SqlUtil {

    private SqlUtil() {
        // prevent
    }

    public static Throwable findSqlException(Throwable e) {
        Throwable cause = e.getCause();
        while (cause != null && !(cause instanceof SQLException)) {
            cause = cause.getCause();
        }

        return cause;
    }
}
