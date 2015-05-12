package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class EventTypeBase extends Table {

    private static final long serialVersionUID = 1L;    public static final EventTypeBase T_EVENTTYPE = new EventTypeBase("EventTypebase");

    public EventTypeBase(String alias) { super("SystemDB..EventType", alias); }

    public NvarcharColumn C_EVENTTYPE = new NvarcharColumn("EventType", "nvarchar(50)", this, NOT_NULL);


}

