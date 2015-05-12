package db.tables.IvyDB;

import db.*;
import db.columns.*;

public class PATCHOptionPriceTEMPBase extends Table {

    private static final long serialVersionUID = 1L;    public static final PATCHOptionPriceTEMPBase T_PATCH_OPTIONPRICE_TEMP = new PATCHOptionPriceTEMPBase("PATCH_OptionPrice_TEMPbase");

    public PATCHOptionPriceTEMPBase(String alias) { super("IvyDB..PATCH_OptionPrice_TEMP", alias); }

    public VarcharColumn C_ACTION = new VarcharColumn("Action", "varchar(1)", this, NOT_NULL);
    public IntColumn C_SECURITYID = new IntColumn("securityID", "int", this, NOT_NULL);
    public SmalldatetimeColumn C_DATE = new SmalldatetimeColumn("date", "smalldatetime", this, NOT_NULL);
    public CharColumn C_ROOT = new CharColumn("root", "char(5)", this, NOT_NULL);
    public CharColumn C_SUFFIX = new CharColumn("suffix", "char(2)", this, NOT_NULL);
    public IntColumn C_STRIKE = new IntColumn("strike", "int", this, NOT_NULL);
    public SmalldatetimeColumn C_EXPIRATION = new SmalldatetimeColumn("expiration", "smalldatetime", this, NOT_NULL);
    public CharColumn C_CALLPUTFLAG = new CharColumn("callPutFlag", "char(1)", this, NULL);
    public RealColumn C_BESTBID = new RealColumn("bestBid", "real", this, NOT_NULL);
    public RealColumn C_BESTOFFER = new RealColumn("bestOffer", "real", this, NOT_NULL);
    public SmalldatetimeColumn C_LASTTRADEDATE = new SmalldatetimeColumn("lastTradeDate", "smalldatetime", this, NULL);
    public IntColumn C_VOLUME = new IntColumn("volume", "int", this, NOT_NULL);
    public IntColumn C_OPENINTEREST = new IntColumn("openInterest", "int", this, NOT_NULL);
    public CharColumn C_SPECIALSETTLEMENT = new CharColumn("specialSettlement", "char(1)", this, NULL);
    public RealColumn C_IMPLIEDVOLATILITY = new RealColumn("impliedVolatility", "real", this, NOT_NULL);
    public RealColumn C_DELTA = new RealColumn("delta", "real", this, NOT_NULL);
    public RealColumn C_GAMMA = new RealColumn("gamma", "real", this, NOT_NULL);
    public RealColumn C_VEGA = new RealColumn("vega", "real", this, NOT_NULL);
    public RealColumn C_THETA = new RealColumn("theta", "real", this, NOT_NULL);
    public IntColumn C_OPTIONID = new IntColumn("optionID", "int", this, NOT_NULL);
    public IntColumn C_ADJUSTMENTFACTOR = new IntColumn("adjustmentFactor", "int", this, NOT_NULL);


}

