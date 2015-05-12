package com.malbec.tsdb.markit;

import static com.malbec.tsdb.markit.CreditRatingTable.*;
import static tsdb.Attribute.*;
import static tsdb.AttributeValue.*;
import static util.Objects.*;
import static util.Range.*;

import java.util.*;

import tsdb.*;
import util.*;
import db.*;
import db.clause.*;
import db.columns.*;
import db.tables.TSDB.*;

public class MarkitTable extends TMarkitCdsCompositeHistBase {
    private static final long serialVersionUID = 1L;
	public class MarkitRow extends Row implements CdsData {
	    private static final long serialVersionUID = 1L;
	    public MarkitRow(Row r) {
			super(r);
		}

		public Double spread(String tenor) {
			return (Double) value(column("spread" + tenor));
		}

		public Double recovery() {
			return value(C_RECOVERY);
		}

		public Double avRating() {
			String rating = value(C_AVRATING);
			return rating == null ? null : CREDIT_RATING.value(rating);
		}

		public Double compositeDepth5y() {
			String value = value(C_COMPOSITEDEPTH5Y);
			return value == null ? null : Double.valueOf(value);
		}
		
		public AttributeValue cdsTicker() {
		    return CdsTimeSeriesDefinition.cdsTicker(this);
		}

		public AttributeValue docClause() {
			return attributeValue(DOC_CLAUSE, C_DOCCLAUSE);
		}

		private AttributeValue attributeValue(Attribute attribute, VarcharColumn column, Cell<?> ... extra) {
			return createdIfNecessary(attribute, value(column), extra);
		}

		public AttributeValue ccy() {
			return attributeValue(CCY, C_CCY, 
				CcyTable.CCY.C_DESCRIPTION.with("created by markit loader"),
				CcyTable.CCY.C_PRECEDENCE.with(CcyTable.INVALID_PRECEDENCE)
			);
		}

		public AttributeValue tier() {
			return attributeValue(TIER, C_TIER);
		}

		public AttributeValue ticker() {
			return attributeValue(TICKER, C_TICKER, 
				TickerTable.TICKER.C_TICKER_DESCRIPTION.with(value(C_SHORTNAME))
			);
		}

	}

	public static final MarkitTable MARKIT_CDS = new MarkitTable("markit");
	
	public MarkitTable(String alias) {
		super(alias);
	}

	private List<Row> markitRows(Clause clause) {
		List<Column<?>> list = empty();
		list.add(C_AVRATING); 
		list.add(C_CCY); 
		list.add(C_COMPOSITEDEPTH5Y); 
		list.add(C_DATE); 
		list.add(C_DOCCLAUSE); 
		list.add(C_TIER); 
		list.add(C_TICKER); 
		list.add(C_SHORTNAME); 
		list.add(C_RECOVERY); 
		list.add(C_SPREAD6M); 
		list.add(C_SPREAD1Y); 
		list.add(C_SPREAD2Y); 
		list.add(C_SPREAD3Y); 
		list.add(C_SPREAD4Y); 
		list.add(C_SPREAD5Y); 
		list.add(C_SPREAD7Y); 
		list.add(C_SPREAD10Y); 
		list.add(C_SPREAD15Y); 
		list.add(C_SPREAD20Y); 
		list.add(C_SPREAD30Y); 
		list.add(C_HEADERNAME); 
		list.add(C_HEADERVERSION); 
		return select(list, clause).rows();
	}

	public MarkitRow markitRow(Date date, AttributeValue ticker, AttributeValue tier, AttributeValue ccy, AttributeValue docClause) {
		Clause matches = matches(date);
		matches = matches.and(C_TICKER.isWithoutCase(ticker.name()));
		matches = matches.and(C_TIER.isWithoutCase(tier.name()));
		matches = matches.and(C_CCY.isWithoutCase(ccy.name()));
		matches = matches.and(C_DOCCLAUSE.isWithoutCase(docClause.name()));
		return new MarkitRow(the(markitRows(matches)));
	}

	public Clause matches(Date date) {
		return C_DATE.in(onDayOf(date));
	}

	public List<MarkitRow> markitRows(Date date) {
		Log.info("processing " + count(matches(date)) + " rows");
		List<Row> rows = markitRows(matches(date));
		List<MarkitRow> result = empty();
		for (Row row : rows) 
			result.add(new MarkitRow(row));
		return result;
	}

	public List<String> changedTickers(Date date, Date otherDate) {
		MarkitTable other = new MarkitTable("other");
		Clause otherDateRow = other.matches(otherDate).and(C_TICKER.joinOn(other));
		Clause notExists = other.notExists(otherDateRow);
		return C_TICKER.distinct(matches(date).and(notExists));
	}

	public void insertTestData(Date testDate) {
		insert(
			C_DATE.with(testDate),
			C_HEADERNAME.with("test"),
			C_HEADERVERSION.with("test"),
			C_CCY.with("USD"),
			C_DOCCLAUSE.with("ZZ"),
			C_SHORTNAME.with("test"),
			C_TIER.with("test"),
			C_TICKER.with("TEST"),
			C_SPREAD5Y.with(99.0)
		);
	}


	

}
