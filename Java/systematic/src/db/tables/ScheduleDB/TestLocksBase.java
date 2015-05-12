package db.tables.ScheduleDB;

import db.*;
import db.columns.*;

public class TestLocksBase extends Table {

    private static final long serialVersionUID = 1L;    public static final TestLocksBase T_TEST_LOCKS = new TestLocksBase("test_locksbase");

    public TestLocksBase(String alias) { super("ScheduleDB..test_locks", alias); }

    public VarcharColumn C_USERNAME = new VarcharColumn("username", "varchar(50)", this, NOT_NULL);
    public VarcharColumn C_HOSTNAME = new VarcharColumn("hostname", "varchar(50)", this, NOT_NULL);
    public VarcharColumn C_TESTNAME = new VarcharColumn("testname", "varchar(1024)", this, NOT_NULL);
    public DatetimeColumn C_LOCK_TIME = new DatetimeColumn("lock_time", "datetime", this, NOT_NULL);
    public NcharColumn C_LOCK_NAME = new NcharColumn("lock_name", "nchar(10)", this, NOT_NULL);


}

