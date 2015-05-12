package db.tables.TSDB;

import db.*;
import db.columns.*;

public class CouponBase extends Table {

    private static final long serialVersionUID = 1L;    public static final CouponBase T_COUPON = new CouponBase("couponbase");

    public CouponBase(String alias) { super("TSDB..coupon", alias); }

    public IntIdentityColumn C_COUPON_ID = new IntIdentityColumn("coupon_id", "int identity", this, NOT_NULL);
    public FloatColumn C_COUPON_VALUE = new FloatColumn("coupon_value", "float(53)", this, NOT_NULL);


}

