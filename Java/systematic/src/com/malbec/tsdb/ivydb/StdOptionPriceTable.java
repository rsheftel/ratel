package com.malbec.tsdb.ivydb;

import static util.Errors.*;
import java.util.*;

import tsdb.*;

import db.*;
import db.clause.*;
import db.tables.IvyDB.*;
import static util.Objects.*;

public class StdOptionPriceTable extends STDOPTIONPRICEBase {

    private static final long serialVersionUID = 1L;
	public class StdOptionPriceRow extends Row {
	    private static final long serialVersionUID = 1L;
		public StdOptionPriceRow(Row r) {
			super(r);
		}

		public int id() {
			return value(C_SECURITYID);
		}
		
		public Double delta() {
			return C_DELTA.doubleValue(this);
		}

		public Double impliedVol() {
			return C_IMPLIEDVOLATILITY.doubleValue(this);
		}

		public boolean isCall() {
			return false;
		}

		public OptionType optionType() {
			return OptionType.fromFlag(value(C_CALLPUT));
		}

		public String expiry() {
			int value = value(C_DAYS).intValue();
			bombUnless(value == value(C_DAYS), "days value " + value(C_DAYS) + " was not an integer");
			return value + "d";
		}
	}

	public static final StdOptionPriceTable STD_OPTION_PRICE = new StdOptionPriceTable();

	public StdOptionPriceTable() {
		super("ivy_option");
	}

	public StdOptionPriceRow row(int testSecurityId, int expiryDays, String callPut, Date testDate) {
		Clause securityIdMatches = C_SECURITYID.is(testSecurityId);
		Clause dateMatches = C_DATE.is(testDate);
		Clause expiryMatches = C_DAYS.is(Integer.valueOf(expiryDays));
		Clause callPutMatches = C_CALLPUT.is(callPut);
		Clause matches = securityIdMatches.and(expiryMatches).and(callPutMatches).and(dateMatches);
		return new StdOptionPriceRow(row(matches));
	}

	public List<StdOptionPriceRow> rows(Date date) {
		List<Row> rows = rows(C_DATE.is(date));
		List<StdOptionPriceRow> result = empty();
		for (Row row : rows)
			result.add(new StdOptionPriceRow(row));
		return result;
	}

}
