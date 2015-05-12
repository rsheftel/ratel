package db.tables.BloombergFeedDB;

import db.*;
import db.columns.*;

public class JobBloombergDataHistBase extends Table {

    private static final long serialVersionUID = 1L;    public static final JobBloombergDataHistBase T_JOBBLOOMBERGDATAHIST = new JobBloombergDataHistBase("JobBloombergDataHistbase");

    public JobBloombergDataHistBase(String alias) { super("BloombergFeedDB..JobBloombergDataHist", alias); }

    public IntColumn C_IDJOB = new IntColumn("idJob", "int", this, NOT_NULL);
    public IntColumn C_IDBBDATA = new IntColumn("idBBData", "int", this, NOT_NULL);
    public DatetimeColumn C_DATETIMEOBSERVATION = new DatetimeColumn("datetimeObservation", "datetime", this, NOT_NULL);
    public FloatColumn C_VALUE = new FloatColumn("value", "float(53)", this, NULL);
    public DatetimeColumn C_LASTCHANGETMS = new DatetimeColumn("lastChangeTMS", "datetime", this, NOT_NULL);


}

