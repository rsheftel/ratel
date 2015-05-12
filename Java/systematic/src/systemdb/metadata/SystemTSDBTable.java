package systemdb.metadata;

import systemdb.data.*;
import systemdb.data.bars.*;
import tsdb.*;
import db.tables.SystemDB.*;


public class SystemTSDBTable extends TSDBBase implements HistoricalProvider{
    private static final long serialVersionUID = 1L;
    public static final SystemTSDBTable SYSTEM_SERIES_DATA = new SystemTSDBTable();
    
    public SystemTSDBTable() {
        super("system_tsdb");
    }
    
    public void insert(String name, DataSource source, 
        String close, String open, String high, String low, 
        String volume, String openInterest
    ) {
        insert(name, source, close, open, high, low, volume, openInterest, new BasicBarSmith());
    }

    public void insert(String name, DataSource source, 
        String close, String open, String high, String low, 
        String volume, String openInterest, 
        BarSmith smith
    ) {
        insert(
            C_NAME.with(name),
            C_DATA_SOURCE.with(source.name()),
            C_NAME_CLOSE.with(close),
            C_NAME_OPEN.with(open),
            C_CALCULATE_METHOD.with(smith.name()),
            C_NAME_HIGH.with(high),
            C_NAME_LOW.with(low),
            C_NAME_VOLUME.with(volume),
            C_NAME_OPEN_INTEREST.with(openInterest)
        );
        
    }

    public void insert(String name, DataSource source, String close) {
        insert(name, source, close, new BasicBarSmith());
    }
    
    public void insert(String name, DataSource source, String close, BarSmith smith) {
        insert(name, source, close, close, close, close, null, null, smith);
    }

    public HistoricalDailyData dataSource(String name) {
        return new SystemTSDBRow(this, C_NAME.is(name));
    }


}
