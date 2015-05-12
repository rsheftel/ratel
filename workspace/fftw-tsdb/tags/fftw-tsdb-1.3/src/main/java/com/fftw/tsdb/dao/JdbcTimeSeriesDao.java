package com.fftw.tsdb.dao;

public interface JdbcTimeSeriesDao
{
    void bulkInsert(String bcpFile);
}
