package db.tables.PerformanceDB;

import db.*;
import db.columns.*;

public class ProductBase extends Table {

    private static final long serialVersionUID = 1L;    public static final ProductBase T_PRODUCT = new ProductBase("Productbase");

    public ProductBase(String alias) { super("PerformanceDB..Product", alias); }

    public IntColumn C_ID = new IntColumn("Id", "int", this, NOT_NULL);
    public NvarcharColumn C_NAME = new NvarcharColumn("Name", "nvarchar(200)", this, NOT_NULL);


}

