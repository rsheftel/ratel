package com.fftw.ivydb.service;

import com.fftw.ivydb.domain.StandardOptionPrice;
import com.fftw.tsdb.service.batch.TimeSeriesBatchService;

public interface StandardOptionTimeSeriesService extends TimeSeriesBatchService
{
    /**
     * Sets up all the attribute and attribute value objects so that they can be
     * reused
     * 
     */
    void setUp ();

    /**
     * Given a StandardOptionPrice, create or update 2 time series with data points
     * @param standardOptionPrice
     */
    void createTimeSeriesDatas (StandardOptionPrice standardOptionPrice);

}
