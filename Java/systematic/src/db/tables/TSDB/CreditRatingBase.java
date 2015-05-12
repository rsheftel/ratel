package db.tables.TSDB;

import db.*;
import db.columns.*;

public class CreditRatingBase extends Table {

    private static final long serialVersionUID = 1L;    public static final CreditRatingBase T_CREDIT_RATING = new CreditRatingBase("credit_ratingbase");

    public CreditRatingBase(String alias) { super("TSDB..credit_rating", alias); }

    public FloatColumn C_RATING_VALUE = new FloatColumn("rating_value", "float(53)", this, NULL);
    public VarcharColumn C_SNP = new VarcharColumn("snp", "varchar(32)", this, NULL);
    public VarcharColumn C_FITCH = new VarcharColumn("fitch", "varchar(32)", this, NULL);
    public VarcharColumn C_MOODYS = new VarcharColumn("moodys", "varchar(32)", this, NULL);


}

