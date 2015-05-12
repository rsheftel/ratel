package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class TypeBase extends Table {

    private static final long serialVersionUID = 1L;    public static final TypeBase T_TYPE = new TypeBase("Typebase");

    public TypeBase(String alias) { super("SystemDB..Type", alias); }

    public NvarcharColumn C_TYPE = new NvarcharColumn("Type", "nvarchar(50)", this, NOT_NULL);


}

