package db.tables.TSDB;

import db.*;
import db.columns.*;

public class TbaDetailsBase extends Table {

    private static final long serialVersionUID = 1L;    public static final TbaDetailsBase T_TBA_DETAILS = new TbaDetailsBase("tba_detailsbase");

    public TbaDetailsBase(String alias) { super("TSDB..tba_details", alias); }

    public IntIdentityColumn C_ID = new IntIdentityColumn("id", "int identity", this, NOT_NULL);
    public NvarcharColumn C_PROGRAM = new NvarcharColumn("program", "nvarchar(50)", this, NOT_NULL);
    public FloatColumn C_LOW_COUPON = new FloatColumn("low_coupon", "float(53)", this, NOT_NULL);
    public FloatColumn C_HIGH_COUPON = new FloatColumn("high_coupon", "float(53)", this, NOT_NULL);


}

