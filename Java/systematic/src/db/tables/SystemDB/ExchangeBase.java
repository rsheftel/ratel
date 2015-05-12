package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class ExchangeBase extends Table {

    private static final long serialVersionUID = 1L;    public static final ExchangeBase T_EXCHANGE = new ExchangeBase("Exchangebase");

    public ExchangeBase(String alias) { super("SystemDB..Exchange", alias); }

    public NvarcharColumn C_EXCHANGE = new NvarcharColumn("Exchange", "nvarchar(50)", this, NOT_NULL);
    public FloatColumn C_DEFAULTBIGPOINTVALUE = new FloatColumn("DefaultBigPointValue", "float(53)", this, NOT_NULL);
    public FloatColumn C_DEFAULTSLIPPAGE = new FloatColumn("DefaultSlippage", "float(53)", this, NOT_NULL);
    public NvarcharColumn C_SLIPPAGECALCULATOR = new NvarcharColumn("SlippageCalculator", "nvarchar(255)", this, NOT_NULL);


}

