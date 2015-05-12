package com.fftw.ivydb.service;

import com.fftw.ivydb.domain.SecurityPrice;
import com.fftw.tsdb.service.batch.TimeSeriesBatchService;

public interface SecurityTimeSeriesService extends TimeSeriesBatchService
{
    /**
     * Sets up all the attribute and attribute value objects so that they can be
     * reused
     * 
     */
    void setUp ();

    /**
     * Given a SecurityPrice, create or update 5 time series with data points
     * @param securityPrice
     */
    void createTimeSeriesDatas (SecurityPrice securityPrice);
}
