package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class SourcesBase extends Table {

    private static final long serialVersionUID = 1L;    public static final SourcesBase T_SOURCES = new SourcesBase("Sourcesbase");

    public SourcesBase(String alias) { super("SystemDB..Sources", alias); }

    public NvarcharColumn C_SOURCENAME = new NvarcharColumn("SourceName", "nvarchar(50)", this, NOT_NULL);
    public BitColumn C_HISTDAILY = new BitColumn("HistDaily", "bit", this, NULL);
    public BitColumn C_HISTTICK = new BitColumn("HistTick", "bit", this, NULL);
    public BitColumn C_TODAYTICK = new BitColumn("TodayTick", "bit", this, NULL);
    public BitColumn C_LIVE = new BitColumn("Live", "bit", this, NULL);


}

