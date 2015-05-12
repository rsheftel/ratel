package db.tables.IvyDB;

import db.*;
import db.columns.*;

public class IndustryGroupBase extends Table {

    private static final long serialVersionUID = 1L;    public static final IndustryGroupBase T_INDUSTRYGROUP = new IndustryGroupBase("IndustryGroupbase");

    public IndustryGroupBase(String alias) { super("IvyDB..IndustryGroup", alias); }

    public IntColumn C_CLASSIFICATIONCODE = new IntColumn("classificationCode", "int", this, NOT_NULL);
    public VarcharColumn C_INDUSTRYGROUPDESCRIPTION = new VarcharColumn("industryGroupDescription", "varchar(100)", this, NOT_NULL);


}

