package db.tables.TSDB;

import db.*;
import db.columns.*;

public class DataSourceBase extends Table {

    private static final long serialVersionUID = 1L;    public static final DataSourceBase T_DATA_SOURCE = new DataSourceBase("data_sourcebase");

    public DataSourceBase(String alias) { super("TSDB..data_source", alias); }

    public IntIdentityColumn C_DATA_SOURCE_ID = new IntIdentityColumn("data_source_id", "int identity", this, NOT_NULL);
    public VarcharColumn C_DATA_SOURCE_NAME = new VarcharColumn("data_source_name", "varchar(200)", this, NOT_NULL);


}

