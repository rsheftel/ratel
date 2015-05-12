package db.tables.PerformanceDB;

import db.*;
import db.columns.*;

public class InvestorDataDefinitionBase extends Table {

    private static final long serialVersionUID = 1L;    public static final InvestorDataDefinitionBase T_INVESTORDATADEFINITION = new InvestorDataDefinitionBase("InvestorDataDefinitionbase");

    public InvestorDataDefinitionBase(String alias) { super("PerformanceDB..InvestorDataDefinition", alias); }

    public IntIdentityColumn C_INVDATADEFID = new IntIdentityColumn("invDataDefId", "int identity", this, NOT_NULL);
    public VarcharColumn C_DESCINVDATADEF = new VarcharColumn("descInvDataDef", "varchar(100)", this, NOT_NULL);


}

