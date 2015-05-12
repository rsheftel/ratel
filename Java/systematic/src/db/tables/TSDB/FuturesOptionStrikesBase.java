package db.tables.TSDB;

import db.*;
import db.columns.*;

public class FuturesOptionStrikesBase extends Table {

    private static final long serialVersionUID = 1L;    public static final FuturesOptionStrikesBase T_FUTURES_OPTION_STRIKES = new FuturesOptionStrikesBase("futures_option_strikesbase");

    public FuturesOptionStrikesBase(String alias) { super("TSDB..futures_option_strikes", alias); }

    public IntColumn C_OPTION_ID = new IntColumn("option_id", "int", this, NOT_NULL);
    public NvarcharColumn C_FREQUENCY = new NvarcharColumn("frequency", "nvarchar(50)", this, NOT_NULL);
    public IntColumn C_PERIODS_OUT = new IntColumn("periods_out", "int", this, NOT_NULL);
    public FloatColumn C_STRIKE_STEP = new FloatColumn("strike_step", "float(53)", this, NOT_NULL);
    public IntColumn C_NUM_STRIKES = new IntColumn("num_strikes", "int", this, NOT_NULL);


}

