package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class TransformationBase extends Table {

    private static final long serialVersionUID = 1L;    public static final TransformationBase T_TRANSFORMATION = new TransformationBase("Transformationbase");

    public TransformationBase(String alias) { super("SystemDB..Transformation", alias); }

    public IntIdentityColumn C_ID = new IntIdentityColumn("ID", "int identity", this, NOT_NULL);
    public NvarcharColumn C_NAME = new NvarcharColumn("Name", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_EXECUTABLE = new NvarcharColumn("Executable", "nvarchar(250)", this, NULL);
    public NvarcharColumn C_SOURCE = new NvarcharColumn("Source", "nvarchar(250)", this, NULL);
    public NvarcharColumn C_LIBRARY = new NvarcharColumn("Library", "nvarchar(50)", this, NULL);
    public NvarcharColumn C_DESCRIPTION = new NvarcharColumn("Description", "nvarchar(2000)", this, NULL);


}

