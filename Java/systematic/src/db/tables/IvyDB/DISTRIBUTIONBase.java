package db.tables.IvyDB;

import db.*;
import db.columns.*;

public class DISTRIBUTIONBase extends Table {

    private static final long serialVersionUID = 1L;    public static final DISTRIBUTIONBase T_DISTRIBUTION = new DISTRIBUTIONBase("DISTRIBUTIONbase");

    public DISTRIBUTIONBase(String alias) { super("IvyDB..DISTRIBUTION", alias); }

    public IntColumn C_SECURITYID = new IntColumn("SecurityID", "int", this, NOT_NULL);
    public SmalldatetimeColumn C_RECORDDATE = new SmalldatetimeColumn("RecordDate", "smalldatetime", this, NOT_NULL);
    public IntColumn C_SEQUENCENUMBER = new IntColumn("SequenceNumber", "int", this, NOT_NULL);
    public SmalldatetimeColumn C_EXDATE = new SmalldatetimeColumn("ExDate", "smalldatetime", this, NOT_NULL);
    public RealColumn C_AMOUNT = new RealColumn("Amount", "real", this, NOT_NULL);
    public RealColumn C_ADJUSTMENTFACTOR = new RealColumn("AdjustmentFactor", "real", this, NOT_NULL);
    public SmalldatetimeColumn C_DECLAREDATE = new SmalldatetimeColumn("DeclareDate", "smalldatetime", this, NOT_NULL);
    public SmalldatetimeColumn C_PAYMENTDATE = new SmalldatetimeColumn("PaymentDate", "smalldatetime", this, NOT_NULL);
    public IntColumn C_LINKSECURITYID = new IntColumn("LinkSecurityID", "int", this, NOT_NULL);
    public CharColumn C_DISTRIBUTIONTYPE = new CharColumn("DistributionType", "char(1)", this, NULL);
    public CharColumn C_FREQUENCY = new CharColumn("Frequency", "char(1)", this, NULL);
    public CharColumn C_CURRENCY = new CharColumn("Currency", "char(3)", this, NULL);
    public CharColumn C_APPROXIMATEFLAG = new CharColumn("ApproximateFlag", "char(1)", this, NULL);
    public CharColumn C_CANCELFLAG = new CharColumn("CancelFlag", "char(1)", this, NULL);
    public CharColumn C_LIQUIDATIONFLAG = new CharColumn("LiquidationFlag", "char(1)", this, NULL);


}

