package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class CurrencyBase extends Table {

    private static final long serialVersionUID = 1L;    public static final CurrencyBase T_CURRENCY = new CurrencyBase("Currencybase");

    public CurrencyBase(String alias) { super("SystemDB..Currency", alias); }

    public NvarcharColumn C_CURRENCY = new NvarcharColumn("Currency", "nvarchar(50)", this, NOT_NULL);


}

