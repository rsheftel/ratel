package db.tables.PerformanceDB;

import db.*;
import db.columns.*;

public class RateDefinitionBase extends Table {

    private static final long serialVersionUID = 1L;    public static final RateDefinitionBase T_RATEDEFINITION = new RateDefinitionBase("RateDefinitionbase");

    public RateDefinitionBase(String alias) { super("PerformanceDB..RateDefinition", alias); }

    public IntIdentityColumn C_RATEDEFID = new IntIdentityColumn("rateDefId", "int identity", this, NOT_NULL);
    public VarcharColumn C_NAMERATEDEF = new VarcharColumn("nameRateDef", "varchar(100)", this, NOT_NULL);


}

