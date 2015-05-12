package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class LivePublisherUniverseBase extends Table {

    private static final long serialVersionUID = 1L;    public static final LivePublisherUniverseBase T_LIVEPUBLISHERUNIVERSE = new LivePublisherUniverseBase("LivePublisherUniversebase");

    public LivePublisherUniverseBase(String alias) { super("SystemDB..LivePublisherUniverse", alias); }

    public NvarcharColumn C_NAME = new NvarcharColumn("Name", "nvarchar(50)", this, NOT_NULL);
    public BitColumn C_ONLYPUBLISHDURINGMARKETHOURS = new BitColumn("OnlyPublishDuringMarketHours", "bit", this, NOT_NULL);


}

