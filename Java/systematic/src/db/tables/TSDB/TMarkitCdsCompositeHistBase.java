package db.tables.TSDB;

import db.*;
import db.columns.*;

public class TMarkitCdsCompositeHistBase extends Table {

    private static final long serialVersionUID = 1L;    public static final TMarkitCdsCompositeHistBase T_T_MARKIT_CDS_COMPOSITE_HIST = new TMarkitCdsCompositeHistBase("T_Markit_Cds_Composite_Histbase");

    public TMarkitCdsCompositeHistBase(String alias) { super("TSDB..T_Markit_Cds_Composite_Hist", alias); }

    public VarcharColumn C_HEADERNAME = new VarcharColumn("headerName", "varchar(40)", this, NOT_NULL);
    public VarcharColumn C_HEADERVERSION = new VarcharColumn("headerVersion", "varchar(40)", this, NOT_NULL);
    public VarcharColumn C_AVRATING = new VarcharColumn("avRating", "varchar(3)", this, NULL);
    public VarcharColumn C_CCY = new VarcharColumn("ccy", "varchar(30)", this, NOT_NULL);
    public VarcharColumn C_COMPOSITEDEPTH5Y = new VarcharColumn("compositeDepth5y", "varchar(30)", this, NULL);
    public VarcharColumn C_COMPOSITELEVEL10Y = new VarcharColumn("compositeLevel10y", "varchar(30)", this, NULL);
    public VarcharColumn C_COMPOSITELEVEL15Y = new VarcharColumn("compositeLevel15y", "varchar(30)", this, NULL);
    public VarcharColumn C_COMPOSITELEVEL1Y = new VarcharColumn("compositeLevel1y", "varchar(30)", this, NULL);
    public VarcharColumn C_COMPOSITELEVEL20Y = new VarcharColumn("compositeLevel20y", "varchar(30)", this, NULL);
    public VarcharColumn C_COMPOSITELEVEL2Y = new VarcharColumn("compositeLevel2y", "varchar(30)", this, NULL);
    public VarcharColumn C_COMPOSITELEVEL30Y = new VarcharColumn("compositeLevel30y", "varchar(30)", this, NULL);
    public VarcharColumn C_COMPOSITELEVEL3Y = new VarcharColumn("compositeLevel3y", "varchar(30)", this, NULL);
    public VarcharColumn C_COMPOSITELEVEL4Y = new VarcharColumn("compositeLevel4y", "varchar(30)", this, NULL);
    public VarcharColumn C_COMPOSITELEVEL5Y = new VarcharColumn("compositeLevel5y", "varchar(30)", this, NULL);
    public VarcharColumn C_COMPOSITELEVEL6M = new VarcharColumn("compositeLevel6m", "varchar(30)", this, NULL);
    public VarcharColumn C_COMPOSITELEVEL7Y = new VarcharColumn("compositeLevel7y", "varchar(30)", this, NULL);
    public VarcharColumn C_COMPOSITELEVELRECOVERY = new VarcharColumn("compositeLevelRecovery", "varchar(30)", this, NULL);
    public VarcharColumn C_CONTRIBUTOR = new VarcharColumn("contributor", "varchar(30)", this, NULL);
    public DatetimeColumn C_DATE = new DatetimeColumn("date", "datetime", this, NOT_NULL);
    public VarcharColumn C_DOCCLAUSE = new VarcharColumn("docClause", "varchar(2)", this, NOT_NULL);
    public VarcharColumn C_IMPLIEDRATING = new VarcharColumn("impliedRating", "varchar(3)", this, NULL);
    public FloatColumn C_RECOVERY = new FloatColumn("recovery", "float(53)", this, NULL);
    public VarcharColumn C_REDCODE = new VarcharColumn("redCode", "varchar(30)", this, NULL);
    public VarcharColumn C_REGION = new VarcharColumn("region", "varchar(300)", this, NULL);
    public VarcharColumn C_SECTOR = new VarcharColumn("sector", "varchar(300)", this, NULL);
    public VarcharColumn C_SHORTNAME = new VarcharColumn("shortName", "varchar(300)", this, NOT_NULL);
    public FloatColumn C_SPREAD10Y = new FloatColumn("spread10y", "float(53)", this, NULL);
    public FloatColumn C_SPREAD15Y = new FloatColumn("spread15y", "float(53)", this, NULL);
    public FloatColumn C_SPREAD1Y = new FloatColumn("spread1y", "float(53)", this, NULL);
    public FloatColumn C_SPREAD20Y = new FloatColumn("spread20y", "float(53)", this, NULL);
    public FloatColumn C_SPREAD2Y = new FloatColumn("spread2y", "float(53)", this, NULL);
    public FloatColumn C_SPREAD30Y = new FloatColumn("spread30y", "float(53)", this, NULL);
    public FloatColumn C_SPREAD3Y = new FloatColumn("spread3y", "float(53)", this, NULL);
    public FloatColumn C_SPREAD4Y = new FloatColumn("spread4y", "float(53)", this, NULL);
    public FloatColumn C_SPREAD5Y = new FloatColumn("spread5y", "float(53)", this, NULL);
    public FloatColumn C_SPREAD6M = new FloatColumn("spread6m", "float(53)", this, NULL);
    public FloatColumn C_SPREAD7Y = new FloatColumn("spread7y", "float(53)", this, NULL);
    public VarcharColumn C_TICKER = new VarcharColumn("ticker", "varchar(30)", this, NULL);
    public VarcharColumn C_TIER = new VarcharColumn("tier", "varchar(30)", this, NOT_NULL);
    public VarcharColumn C_RATING10Y = new VarcharColumn("rating10y", "varchar(10)", this, NULL);
    public VarcharColumn C_RATING15Y = new VarcharColumn("rating15y", "varchar(10)", this, NULL);
    public VarcharColumn C_RATING1Y = new VarcharColumn("rating1y", "varchar(10)", this, NULL);
    public VarcharColumn C_RATING20Y = new VarcharColumn("rating20y", "varchar(10)", this, NULL);
    public VarcharColumn C_RATING2Y = new VarcharColumn("rating2y", "varchar(10)", this, NULL);
    public VarcharColumn C_RATING30Y = new VarcharColumn("rating30y", "varchar(10)", this, NULL);
    public VarcharColumn C_RATING3Y = new VarcharColumn("rating3y", "varchar(10)", this, NULL);
    public VarcharColumn C_RATING4Y = new VarcharColumn("rating4y", "varchar(10)", this, NULL);
    public VarcharColumn C_RATING5Y = new VarcharColumn("rating5y", "varchar(10)", this, NULL);
    public VarcharColumn C_RATING6M = new VarcharColumn("rating6m", "varchar(10)", this, NULL);
    public VarcharColumn C_RATING7Y = new VarcharColumn("rating7y", "varchar(10)", this, NULL);
    public VarcharColumn C_COUNTRY = new VarcharColumn("country", "varchar(50)", this, NULL);
    public VarcharColumn C_COMPOSITECURVERATING = new VarcharColumn("compositeCurveRating", "varchar(10)", this, NULL);


}

