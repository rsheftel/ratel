package com.malbec.tsdb.markit;

import static mail.Email.*;

import java.util.*;

import mail.*;
import db.*;
import db.tables.TSDB.*;

public class CcyTable extends CcyBase {
    private static final long serialVersionUID = 1L;
	public static final CcyTable CCY = new CcyTable("ccy");
	public static final double INVALID_PRECEDENCE = -1.0;

	public CcyTable(String alias) {
		super(alias);
	}

	public void create(String name, Double precedence) {
		insert(
			C_CCY_NAME.with(name), 
			C_PRECEDENCE.with(precedence), 
			C_DESCRIPTION.with("created by markit loader")
		);
	}

	public void emailBadPrecedenceRows(String failureAddresses) {
		List<Row> rows = rows(C_PRECEDENCE.is(INVALID_PRECEDENCE));
		if (rows.isEmpty()) return;
		Email badCcy = problem("Currencies in ccy table without valid precedence", "");
		for (Row row : rows) 
			badCcy.append(row.string(C_CCY_NAME) + " id(" + row.string(C_CCY_ID) + ")");
		badCcy.sendTo(failureAddresses);
	}
}
