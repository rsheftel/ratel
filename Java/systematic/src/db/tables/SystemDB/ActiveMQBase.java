package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class ActiveMQBase extends Table {

    private static final long serialVersionUID = 1L;    public static final ActiveMQBase T_ACTIVEMQ = new ActiveMQBase("ActiveMQbase");

    public ActiveMQBase(String alias) { super("SystemDB..ActiveMQ", alias); }

    public NvarcharColumn C_NAME = new NvarcharColumn("Name", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_TOPIC = new NvarcharColumn("Topic", "nvarchar(250)", this, NOT_NULL);
    public NvarcharColumn C_VALUEFIELD = new NvarcharColumn("ValueField", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_DATEFIELD = new NvarcharColumn("DateField", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_VERIFIEDBY = new NvarcharColumn("VerifiedBy", "nvarchar(50)", this, NULL);
    public DatetimeColumn C_VERIFIEDDATE = new DatetimeColumn("VerifiedDate", "datetime", this, NULL);


}

