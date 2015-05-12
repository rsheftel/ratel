package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class StrategyParameterNamesBase extends Table {

    private static final long serialVersionUID = 1L;    public static final StrategyParameterNamesBase T_STRATEGYPARAMETERNAMES = new StrategyParameterNamesBase("StrategyParameterNamesbase");

    public StrategyParameterNamesBase(String alias) { super("SystemDB..StrategyParameterNames", alias); }

    public NvarcharColumn C_STRATEGY = new NvarcharColumn("Strategy", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_PARAMETERNAME = new NvarcharColumn("ParameterName", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_DESCRIPTION = new NvarcharColumn("Description", "nvarchar(2000)", this, NULL);
    public BitColumn C_ISSIZING = new BitColumn("IsSizing", "bit", this, NOT_NULL);


}

