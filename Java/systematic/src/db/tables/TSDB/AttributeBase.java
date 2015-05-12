package db.tables.TSDB;

import db.*;
import db.columns.*;

public class AttributeBase extends Table {

    private static final long serialVersionUID = 1L;    public static final AttributeBase T_ATTRIBUTE = new AttributeBase("attributebase");

    public AttributeBase(String alias) { super("TSDB..attribute", alias); }

    public IntIdentityColumn C_ATTRIBUTE_ID = new IntIdentityColumn("attribute_id", "int identity", this, NOT_NULL);
    public VarcharColumn C_ATTRIBUTE_NAME = new VarcharColumn("attribute_name", "varchar(200)", this, NOT_NULL);
    public VarcharColumn C_TABLE_NAME = new VarcharColumn("table_name", "varchar(200)", this, NOT_NULL);
    public VarcharColumn C_PRIMARY_KEY_COL_NAME = new VarcharColumn("primary_key_col_name", "varchar(200)", this, NOT_NULL);
    public VarcharColumn C_DESCRIPTION_COL_NAME = new VarcharColumn("description_col_name", "varchar(200)", this, NOT_NULL);


}

