package com.fftw.tsdb.dao;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public interface JdbcTimeSeriesDao
{
    void bulkInsert(String bcpFile);
    
    List<Long> getCDSArbitrationTimeSeriesIDs (Calendar calArbitration);
    
    Map<String, List<String>> getCDSArbitrationTickerDiff (Calendar calArbitration);  
}
