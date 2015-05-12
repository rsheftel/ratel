package com.malbec.tsdb.ivydb;

import static util.Errors.*;
import static util.Objects.*;
import java.util.*;

import db.*;
import db.clause.*;
import db.tables.IvyDB.*;

public class SecurityPriceTable extends SECURITYPRICEBase {
    private static final long serialVersionUID = 1L;
	public class SecurityPriceRow extends Row {
	    private static final long serialVersionUID = 1L;
	    public SecurityPriceRow(Row r) {
			super(r);
		}

		public int id() {
			return value(C_SECURITYID);
		}


		public Double price(String quoteType) {
		    if (quoteType.equals("close")) return C_CLOSEPRICE.doubleValue(this);
		    else if (quoteType.equals("open")) return C_OPENPRICE.doubleValue(this);
		    else if (quoteType.equals("low")) return C_BIDLOW.doubleValue(this);
		    else if (quoteType.equals("high")) return C_ASKHIGH.doubleValue(this);
		    throw bomb("unknown quote type " + quoteType);
		}

		public Double sharesOutstanding() {
			return Double.valueOf(value(C_SHARESOUTSTANDING));
		}

		public Double volume() {
			Integer volume = value(C_VOLUME);
			return volume == null ? null : volume.doubleValue();
		}

		public Double totalReturnFactor() {
			return C_ADJUSTMENTFACTOR2.doubleValue(this);
		}

		public Double totalReturn() {
			return C_TOTALRETURN.doubleValue(this);
		}

	}

	public static final SecurityPriceTable SECURITY_PRICE = new SecurityPriceTable();

	public SecurityPriceTable() {
		super("price");
	}

	public SecurityPriceRow row(int securityId, Date date) {
		Clause matches = C_SECURITYID.is(securityId).and(C_DATE.is(date));
		return new SecurityPriceRow(row(matches));
	}

	public List<SecurityPriceRow> rows(Date date) {
		List<Row> rows = rows(C_DATE.is(date));
		List<SecurityPriceRow> result = empty();
		for (Row row : rows) 
			result.add(new SecurityPriceRow(row));
		return result;
	}
}
