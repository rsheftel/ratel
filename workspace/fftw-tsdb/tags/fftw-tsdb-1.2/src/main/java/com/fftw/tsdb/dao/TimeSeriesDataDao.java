package com.fftw.tsdb.dao;

import java.util.List;

import com.fftw.tsdb.domain.TimeSeriesData;
import com.fftw.tsdb.domain.TimeSeriesDataPK;

public interface TimeSeriesDataDao extends GenericDao<TimeSeriesData, TimeSeriesDataPK>
{
    void addTimeSeriesDatasBulk (List<TimeSeriesData> timeSeriesDatas);
}
