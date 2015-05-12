package tsdb;

import static tsdb.Attribute.*;
import static tsdb.AttributeValues.*;
import db.*;

public class TestDataTableSplit extends DbTestCase {
    private static final AttributeValue CDS = INSTRUMENT.value("cds");
    private static final AttributeValue OPTION = INSTRUMENT.value("std_equity_option");
    private static final AttributeValue EQUITY = INSTRUMENT.value("equity");
    private static final AttributeValue TBA = INSTRUMENT.value("mbs_tba");

    public void testDataTableChooser() throws Exception {
        assertDataTable("time_series_data_cds_spread_5y", CDS, TENOR.value("5y"), QUOTE_TYPE.value("spread"));
        assertDataTable("time_series_data_cds_spread_2y", CDS, TENOR.value("2y"), QUOTE_TYPE.value("spread"));
        assertDataTable("time_series_data_cds_av_rating", CDS, QUOTE_TYPE.value("av_rating"));
        assertDataTable("time_series_data_cds_composite_depth", CDS, QUOTE_TYPE.value("composite_depth"), TENOR.value("5y"));
        assertDataTable("time_series_data_cds_composite_depth", CDS, QUOTE_TYPE.value("composite_depth"), TENOR.value("2y"));
        assertDataTable("time_series_data", CDS, TENOR.value("2y"));
        assertDataTable("time_series_data_std_equity_option_30d", OPTION, EXPIRY.value("30d"), OPTION_TYPE.value("call"));
        assertDataTable("time_series_data_std_equity_option_30d", OPTION, EXPIRY.value("30d"), OPTION_TYPE.value("put"));
        assertDataTable("time_series_data_std_equity_option_all", OPTION, EXPIRY.value("all"), OPTION_TYPE.value("put"));
        assertDataTable("time_series_data_equity_volume", EQUITY, QUOTE_TYPE.value("volume"));
        assertDataTable("time_series_data_equity_shares_outstanding", EQUITY, QUOTE_TYPE.value("shares_outstanding"));
        assertDataTable("time_series_data_equity_close_tri_daily_pct", EQUITY, QUOTE_TYPE.value("close"), QUOTE_CONVENTION.value("tri_daily_pct"));
        assertDataTable("time_series_data", EQUITY, QUOTE_TYPE.value("close"));
        assertDataTable("time_series_data_mbs_tba_fncl", TBA, PROGRAM.value("fncl"));
        assertDataTable("time_series_data_mbs_tba_fnci", TBA, PROGRAM.value("fnci"));
        assertDataTable("time_series_data", TBA);
        assertDataTable("time_series_data_futures_option", INSTRUMENT.value("futures_option"));
        assertDataTable("time_series_data_fxfwd", INSTRUMENT.value("fxfwd"));
        assertDataTable("time_series_data_transformation_tri", TRANSFORMATION.value("tri"));
        assertDataTable("time_series_data_transformation_dtd_1dot0", TRANSFORMATION.value("dtd_1.0"));
        assertDataTable("time_series_data", TICKER.value("aapl"), QUOTE_TYPE.value("close"));
    }

    private void assertDataTable(String tableName, AttributeValue  ... values) {
        assertEquals(tableName, TimeSeriesTable.dataTableName(values(values)));
    }
}
