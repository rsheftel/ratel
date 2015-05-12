package com.malbec.tsdb.markit;

import static util.Errors.*;
import static util.Objects.*;

import java.util.*;

import db.*;
import db.clause.*;
import db.tables.TSDB.*;

public class CreditRatingTable extends CreditRatingBase {
    private static final long serialVersionUID = 1L;
	public static final CreditRatingTable CREDIT_RATING = new CreditRatingTable();
	private final Map<String, Double> bySnp = emptyMap(); 
	
	public CreditRatingTable() {
		super("rating");
		List<Row> rows = rows(Clause.TRUE);
		for (Row row : rows)
			bySnp.put(row.value(C_SNP), row.value(C_RATING_VALUE));
	}

	public Double value(String rating) {
		return bombNull(bySnp.get(rating), "cannot find rating value for " + rating);
	}

}
