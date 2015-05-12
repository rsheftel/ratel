package com.malbec.tsdb.markit;

import static com.malbec.tsdb.markit.CdsIndexOtrOverride.*;
import static db.clause.Clause.*;
import static db.columns.BinaryOperatorColumn.*;
import static db.columns.ConstantColumn.*;
import static db.tables.TSDB.CdsIndexTickerBase.*;
import static db.temptables.TSDB.MaxSeriesVersionBase.*;
import static mail.Email.*;
import static util.Dates.*;
import static util.Objects.*;

import java.util.*;

import mail.*;
import util.*;
import db.*;
import db.clause.*;
import db.columns.*;
import db.tables.TSDB.*;

public class IndexTable extends TMarkitIndexCompositeHistBase {
    private static final long serialVersionUID = 1L;
	public class IndexRow extends Row {
	    private static final long serialVersionUID = 1L;
	    public IndexRow(Row r) {
			super(r);

		}

		public Double marketSpread() {
			return value(C_COMPOSITESPREAD);
		}

		public String tenor() {
			return value(C_TERM).toLowerCase();
		}

		public String series() {
			return String.valueOf(value(C_SERIES));
		}

		public String version() {
			return String.valueOf(value(C_VERSION));
		}

		public String ticker() {
			CdsIndexTickerBase t = T_CDS_INDEX_TICKER;
			return t.C_TICKER_NAME.value(t.C_MARKIT_NAME.is(value(C_NAME)));
		}

		public Double marketPrice() {
			return value(C_COMPOSITEPRICE);
		}

		public Double modelSpread() {
			return value(C_MODELSPREAD);
		}

		public Double modelPrice() {
			return value(C_MODELPRICE);
		}
	}

	public static final IndexTable MARKIT_INDEX = new IndexTable("markit_index");
	
	public IndexTable(String alias) {
		super(alias);
	}

	private List<IndexRow> indexRows(Clause clause) {
		List<IndexRow> result = empty();
		for (Row row : rows(clause)) 
			result.add(new IndexRow(row));
		return result;
	}


	private Clause matches(Date date) {
		Clause notTickerHoliday = not(tickerHoliday(date));
		return C_DATE.is(date).and(inTickerTable().and(notTickerHoliday));
	}
	
	protected Clause tickerHoliday(Date date) {
		CdsIndexTickerBase ticker = T_CDS_INDEX_TICKER;
		List<String> centers = ticker.C_FINANCIAL_CENTER.distinct(TRUE);
		List<String> skip = empty();
		for (String c : centers)
			if(isHoliday(date, c))
				skip.add(c);
		return skip.isEmpty() ? FALSE : ticker.C_FINANCIAL_CENTER.in(skip);
	}

	private Clause inTickerTable() {
		return C_NAME.is(T_CDS_INDEX_TICKER.C_MARKIT_NAME);
	}

	public List<IndexRow> indexRows(Date date) {
		Log.info("processing " + count(matches(date)) + " rows");
		return indexRows(matches(date));
	}
	
	public List<IndexRow> otrIndexRows(Date date) {
		createMaxSeriesTable(date);
		Clause matches = matches(date).and(joinTo(T_MAXSERIESVERSION, C_NAME, C_SERIES, C_VERSION));
		Log.info("processing " + count(matches) + " rows");
		return indexRows(matches);
	}

	private void createMaxSeriesTable(Date date) {
		Column<Integer> tenGrand = constant(10000);
		BinaryOperatorColumn<Integer> seriesVersion = plus(times(C_SERIES, tenGrand), C_VERSION);
		FunctionColumn<Integer> maxSeriesVersion = seriesVersion.max();
		BinaryOperatorColumn<Integer> maxSeries = divide(maxSeriesVersion, tenGrand);
		BinaryOperatorColumn<Integer> maxVersion = mod(maxSeriesVersion, tenGrand);
		List<Column<?>> tempColumns = Column.columns(C_NAME, maxSeries.alias("series"), maxVersion.alias("version"));
		Clause dateMatches = C_DATE.lessThanOr(date);
		Clause dataIsNotNull = parenGroup(
			C_COMPOSITEPRICE.isNotNull()
			.or(C_COMPOSITESPREAD.isNotNull())
			.or(C_MODELPRICE.isNotNull())
			.or(C_MODELSPREAD.isNotNull())
		);
		Clause matches = dateMatches.and(dataIsNotNull);
		SelectMultiple maxSeriesVersionSelect = T_T_MARKIT_INDEX_COMPOSITE_HIST.selectDistinct(tempColumns, matches);
		maxSeriesVersionSelect.groupBy(C_NAME);
		maxSeriesVersionSelect.intoTemp("maxSeriesVersion");
		OVERRIDE.updateMaxSeriesVersion(date);
		
//		new Generator().writeFile(temp.schemaTable(), "temptables");
	}

	public IndexRow indexRow(Date date, String ticker, int series, int version, String tenor) {
		Clause matches = matches(date);
		matches = matches.and(inTickerTable());
		matches = matches.and(T_CDS_INDEX_TICKER.C_TICKER_NAME.is(ticker));
		matches = matches.and(C_SERIES.is(series));
		matches = matches.and(C_VERSION.is(version));
		matches = matches.and(C_TERM.isWithoutCase(tenor));
		return new IndexRow(the(indexRows(matches)));
	}

	public void emailHolidaySkippedTickers(String failureAddresses, Date date) {
		Email skippedTickers = notification("tickers not loaded due to holiday " + yyyyMmDd(date), "");
		List<Row> onHoliday = T_CDS_INDEX_TICKER.rowsDistinct(tickerHoliday(date));
		if (onHoliday.isEmpty()) return;
		for(Row r : onHoliday) {
			String ticker = r.value(T_CDS_INDEX_TICKER.C_TICKER_NAME);
			String center = r.value(T_CDS_INDEX_TICKER.C_FINANCIAL_CENTER);
			skippedTickers.append("ticker " + ticker + " in center " + center + " was skipped");
		}
		skippedTickers.sendTo(failureAddresses);
	}

    public void insertTestData(Date date) {
        insert(
            C_DATE.with(date),
            C_HEADERNAME.with("test"),
            C_HEADERVERSION.with("test"),
            C_HEADERDATE.with(now()),
            C_NAME.with("CDXNAIGHVOL"),
            C_SERIES.with(50),
            C_VERSION.with(49),
            C_TERM.with("5y"),
            C_COMPOSITESPREAD.with(97.0)
        );
    }


}
