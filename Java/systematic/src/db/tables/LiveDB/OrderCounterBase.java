package db.tables.LiveDB;

import db.*;
import db.columns.*;

public class OrderCounterBase extends Table {

    private static final long serialVersionUID = 1L;    public static final OrderCounterBase T_ORDER_COUNTER = new OrderCounterBase("order_counterbase");

    public OrderCounterBase(String alias) { super("LiveDB..order_counter", alias); }

    public IntIdentityColumn C_ID = new IntIdentityColumn("id", "int identity", this, NOT_NULL);
    public DatetimeColumn C_DATE = new DatetimeColumn("date", "datetime", this, NOT_NULL);


}

