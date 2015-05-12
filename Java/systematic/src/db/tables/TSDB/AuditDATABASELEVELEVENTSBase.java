package db.tables.TSDB;

import db.*;
import db.columns.*;

public class AuditDATABASELEVELEVENTSBase extends Table {

    private static final long serialVersionUID = 1L;    public static final AuditDATABASELEVELEVENTSBase T_AUDITDATABASE_LEVEL_EVENTS = new AuditDATABASELEVELEVENTSBase("auditDATABASE_LEVEL_EVENTSbase");

    public AuditDATABASELEVELEVENTSBase(String alias) { super("TSDB..auditDATABASE_LEVEL_EVENTS", alias); }

    public IntIdentityColumn C_EVENTID = new IntIdentityColumn("eventid", "int identity", this, NOT_NULL);
    public XmlColumn C_EVENTDATA = new XmlColumn("eventdata", "xml", this, NOT_NULL);


}

