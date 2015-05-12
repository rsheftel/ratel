package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class ClassBase extends Table {

    private static final long serialVersionUID = 1L;    public static final ClassBase T_CLASS = new ClassBase("Classbase");

    public ClassBase(String alias) { super("SystemDB..Class", alias); }

    public NvarcharColumn C_NAME = new NvarcharColumn("Name", "nvarchar(50)", this, NOT_NULL);


}

