package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class PropertyBase extends Table {

    private static final long serialVersionUID = 1L;    public static final PropertyBase T_PROPERTY = new PropertyBase("Propertybase");

    public PropertyBase(String alias) { super("SystemDB..Property", alias); }

    public NvarcharColumn C_NAME = new NvarcharColumn("Name", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_DESCRIPTION = new NvarcharColumn("Description", "nvarchar(150)", this, NULL);


}

