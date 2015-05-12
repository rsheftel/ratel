package db.tables.IvyDB;

import db.*;
import db.columns.*;

public class EXCHANGEBase extends Table {

    private static final long serialVersionUID = 1L;    public static final EXCHANGEBase T_EXCHANGE = new EXCHANGEBase("EXCHANGEbase");

    public EXCHANGEBase(String alias) { super("IvyDB..EXCHANGE", alias); }

    public IntColumn C_SECURITYID = new IntColumn("SecurityID", "int", this, NOT_NULL);
    public SmalldatetimeColumn C_DATE = new SmalldatetimeColumn("Date", "smalldatetime", this, NOT_NULL);
    public SmallintColumn C_SEQUENCENUMBER = new SmallintColumn("SequenceNumber", "smallint", this, NOT_NULL);
    public CharColumn C_STATUS = new CharColumn("Status", "char(1)", this, NULL);
    public CharColumn C_EXCHANGE = new CharColumn("Exchange", "char(1)", this, NULL);
    public CharColumn C_ADDDELETE = new CharColumn("AddDelete", "char(1)", this, NULL);
    public IntColumn C_EXCHANGEFLAGS = new IntColumn("ExchangeFlags", "int", this, NOT_NULL);


}

