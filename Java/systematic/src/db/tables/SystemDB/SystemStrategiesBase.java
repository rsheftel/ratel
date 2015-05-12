package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class SystemStrategiesBase extends Table {

    private static final long serialVersionUID = 1L;    public static final SystemStrategiesBase T_SYSTEMSTRATEGIES = new SystemStrategiesBase("SystemStrategiesbase");

    public SystemStrategiesBase(String alias) { super("SystemDB..SystemStrategies", alias); }

    public NvarcharColumn C_SYSTEM = new NvarcharColumn("System", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_STRATEGY = new NvarcharColumn("Strategy", "nvarchar(50)", this, NOT_NULL);
    public BitColumn C_LONGENTRY = new BitColumn("LongEntry", "bit", this, NULL);
    public BitColumn C_LONGEXIT = new BitColumn("LongExit", "bit", this, NULL);
    public BitColumn C_SHORTENTRY = new BitColumn("ShortEntry", "bit", this, NULL);
    public BitColumn C_SHORTEXIT = new BitColumn("ShortExit", "bit", this, NULL);


}

