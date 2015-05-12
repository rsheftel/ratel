package db.tables.SystemDB;

import db.*;
import db.columns.*;

public class LiveOrdersBase extends Table {

    private static final long serialVersionUID = 1L;    public static final LiveOrdersBase T_LIVEORDERS = new LiveOrdersBase("LiveOrdersbase");

    public LiveOrdersBase(String alias) { super("SystemDB..LiveOrders", alias); }

    public IntIdentityColumn C_ID = new IntIdentityColumn("id", "int identity", this, NOT_NULL);
    public IntColumn C_SYSTEMID = new IntColumn("systemId", "int", this, NOT_NULL);
    public NvarcharColumn C_MARKET = new NvarcharColumn("market", "nvarchar(50)", this, NOT_NULL);
    public DatetimeColumn C_TIME = new DatetimeColumn("time", "datetime", this, NULL);
    public NvarcharColumn C_ENTRYEXIT = new NvarcharColumn("entryExit", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_POSITIONDIRECTION = new NvarcharColumn("positionDirection", "nvarchar(5)", this, NOT_NULL);
    public BigintColumn C_SIZE = new BigintColumn("size", "bigint", this, NOT_NULL);
    public FloatColumn C_PRICE = new FloatColumn("price", "float(53)", this, NULL);
    public NvarcharColumn C_ORDERDETAILS = new NvarcharColumn("orderDetails", "nvarchar(255)", this, NOT_NULL);
    public NvarcharColumn C_DESCRIPTION = new NvarcharColumn("description", "nvarchar(255)", this, NOT_NULL);
    public NvarcharColumn C_HOSTNAME = new NvarcharColumn("hostname", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_TOPICPREFIX = new NvarcharColumn("topicPrefix", "nvarchar(50)", this, NOT_NULL);
    public NvarcharColumn C_FERRETORDERID = new NvarcharColumn("ferretOrderId", "nvarchar(6)", this, NULL);
    public DatetimeColumn C_SUBMITTEDTIME = new DatetimeColumn("submittedTime", "datetime", this, NULL);


}

