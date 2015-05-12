package db.temptables.TSDB;

import db.*;
import db.columns.*;

public class BloombergMoribundBase extends Table {
    private static final long serialVersionUID = 1L;
    public static final BloombergMoribundBase T_BLOOMBERG_MORIBUND = new BloombergMoribundBase("#bloomberg_moribundbase");

    public BloombergMoribundBase(String alias) { super("TSDB..#bloomberg_moribund", alias); }

    public IntColumn C_IDBBDATA = new IntColumn("idBBData", "int", this, NOT_NULL);


}

