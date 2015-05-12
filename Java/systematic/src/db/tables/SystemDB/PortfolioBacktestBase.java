package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class PortfolioBacktestBase extends Table {

    private static final long serialVersionUID = 1L;    public static final PortfolioBacktestBase T_PORTFOLIOBACKTEST = new PortfolioBacktestBase("PortfolioBacktestbase");

    public PortfolioBacktestBase(String alias) { super("SystemDB..PortfolioBacktest", alias); }

    public NvarcharColumn C_STODIR = new NvarcharColumn("STOdir", "nvarchar(1024)", this, NOT_NULL);
    public NvarcharColumn C_STOID = new NvarcharColumn("STOid", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_PORTFOLIONAME = new NvarcharColumn("PortfolioName", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_MSIV_NAME = new NvarcharColumn("MSIV_Name", "nvarchar(200)", this, NOT_NULL);
    public FloatColumn C_WEIGHT = new FloatColumn("weight", "float(53)", this, NOT_NULL);


}

