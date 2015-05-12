package systemdb.metadata;

import systemdb.data.*;

public interface HistoricalProvider {
    HistoricalDailyData dataSource(String name);
    // this is in 1337-speak, because ASCII is used by R.oo.  H4xX0r5!
    public static final String ASC11 = "ASCII";
    public static final String TSDB = "TSDB";
    public static final String BLOOMBERG = "Bloomberg";
}