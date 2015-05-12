package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class StrategyBase extends Table {

    private static final long serialVersionUID = 1L;    public static final StrategyBase T_STRATEGY = new StrategyBase("Strategybase");

    public StrategyBase(String alias) { super("SystemDB..Strategy", alias); }

    public NvarcharColumn C_NAME = new NvarcharColumn("Name", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_CLASS = new NvarcharColumn("Class", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_DESCRIPTION = new NvarcharColumn("Description", "nvarchar(2000)", this, NULL);
    public NvarcharColumn C_OWNER = new NvarcharColumn("Owner", "nvarchar(50)", this, NULL);


}

