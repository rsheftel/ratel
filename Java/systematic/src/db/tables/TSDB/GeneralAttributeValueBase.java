package db.tables.TSDB;

import db.*;
import db.columns.*;

public class GeneralAttributeValueBase extends Table {

    private static final long serialVersionUID = 1L;    public static final GeneralAttributeValueBase T_GENERAL_ATTRIBUTE_VALUE = new GeneralAttributeValueBase("general_attribute_valuebase");

    public GeneralAttributeValueBase(String alias) { super("TSDB..general_attribute_value", alias); }

    public IntIdentityColumn C_ATTRIBUTE_VALUE_ID = new IntIdentityColumn("attribute_value_id", "int identity", this, NOT_NULL);
    public VarcharColumn C_ATTRIBUTE_VALUE_NAME = new VarcharColumn("attribute_value_name", "varchar(200)", this, NOT_NULL);


}

