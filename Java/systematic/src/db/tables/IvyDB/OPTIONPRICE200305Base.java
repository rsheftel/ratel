package db.tables.IvyDB;

import db.*;
import db.columns.*;

public class OPTIONPRICE200305Base extends Table {

    private static final long serialVersionUID = 1L;    public static final OPTIONPRICE200305Base T_OPTION_PRICE_2003_05 = new OPTIONPRICE200305Base("OPTION_PRICE_2003_05base");

    public OPTIONPRICE200305Base(String alias) { super("IvyDB..OPTION_PRICE_2003_05", alias); }

    public IntColumn C_SECURITYID = new IntColumn("SecurityID", "int", this, NOT_NULL);
    public SmalldatetimeColumn C_DATE = new SmalldatetimeColumn("Date", "smalldatetime", this, NOT_NULL);
    public CharColumn C_ROOT = new CharColumn("Root", "char(5)", this, NOT_NULL);
    public CharColumn C_SUFFIX = new CharColumn("Suffix", "char(2)", this, NOT_NULL);
    public IntColumn C_STRIKE = new IntColumn("Strike", "int", this, NOT_NULL);
    public SmalldatetimeColumn C_EXPIRATION = new SmalldatetimeColumn("Expiration", "smalldatetime", this, NOT_NULL);
    public CharColumn C_CALLPUT = new CharColumn("CallPut", "char(1)", this, NULL);
    public RealColumn C_BESTBID = new RealColumn("BestBid", "real", this, NOT_NULL);
    public RealColumn C_BESTOFFER = new RealColumn("BestOffer", "real", this, NOT_NULL);
    public SmalldatetimeColumn C_LASTTRADEDATE = new SmalldatetimeColumn("LastTradeDate", "smalldatetime", this, NULL);
    public IntColumn C_VOLUME = new IntColumn("Volume", "int", this, NOT_NULL);
    public IntColumn C_OPENINTEREST = new IntColumn("OpenInterest", "int", this, NOT_NULL);
    public CharColumn C_SPECIALSETTLEMENT = new CharColumn("SpecialSettlement", "char(1)", this, NULL);
    public RealColumn C_IMPLIEDVOLATILITY = new RealColumn("ImpliedVolatility", "real", this, NOT_NULL);
    public RealColumn C_DELTA = new RealColumn("Delta", "real", this, NOT_NULL);
    public RealColumn C_GAMMA = new RealColumn("Gamma", "real", this, NOT_NULL);
    public RealColumn C_VEGA = new RealColumn("Vega", "real", this, NOT_NULL);
    public RealColumn C_THETA = new RealColumn("Theta", "real", this, NOT_NULL);
    public IntColumn C_OPTIONID = new IntColumn("OptionID", "int", this, NOT_NULL);
    public IntColumn C_ADJUSTMENTFACTOR = new IntColumn("AdjustmentFactor", "int", this, NOT_NULL);


}

