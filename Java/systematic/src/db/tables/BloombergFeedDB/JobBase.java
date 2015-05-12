package db.tables.BloombergFeedDB;

import db.*;
import db.columns.*;

public class JobBase extends Table {

    private static final long serialVersionUID = 1L;    public static final JobBase T_JOB = new JobBase("Jobbase");

    public JobBase(String alias) { super("BloombergFeedDB..Job", alias); }

    public IntIdentityColumn C_IDJOB = new IntIdentityColumn("idJob", "int identity", this, NOT_NULL);
    public VarcharColumn C_NAMEJOB = new VarcharColumn("nameJob", "varchar(100)", this, NOT_NULL);
    public VarcharColumn C_NAMEFILE = new VarcharColumn("nameFile", "varchar(200)", this, NOT_NULL);
    public VarcharColumn C_TIMEOBSERVATION = new VarcharColumn("timeObservation", "varchar(8)", this, NOT_NULL);
    public BitColumn C_ISCLOSEOFBUSINESS = new BitColumn("isCloseOfBusiness", "bit", this, NOT_NULL);
    public VarcharColumn C_TIMERUN = new VarcharColumn("timeRun", "varchar(5)", this, NOT_NULL);
    public DatetimeColumn C_LASTRUNON = new DatetimeColumn("lastRunOn", "datetime", this, NULL);


}

