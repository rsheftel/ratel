package db.tables.TSDB;

import db.*;
import db.columns.*;

public class MonthBase extends Table {

    private static final long serialVersionUID = 1L;    public static final MonthBase T_MONTH = new MonthBase("monthbase");

    public MonthBase(String alias) { super("TSDB..month", alias); }

    public IntColumn C_NUMBER = new IntColumn("number", "int", this, NOT_NULL);
    public NvarcharColumn C_NAME = new NvarcharColumn("name", "nvarchar(20)", this, NOT_NULL);
    public NcharColumn C_FUTURES_LETTER = new NcharColumn("futures_letter", "nchar(1)", this, NOT_NULL);


}

