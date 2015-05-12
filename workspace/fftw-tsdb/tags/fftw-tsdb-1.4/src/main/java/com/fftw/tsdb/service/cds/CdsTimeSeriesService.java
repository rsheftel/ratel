package com.fftw.tsdb.service.cds;

import com.fftw.tsdb.domain.cds.MarkitCompositeHist;
import com.fftw.tsdb.service.batch.TimeSeriesBatchService;

public interface CdsTimeSeriesService extends TimeSeriesBatchService
{
    /**
     * Sets up all the attribute and attribute value objects so that they can be
     * reused
     * 
     */
    void setUp ();

    /**
     * Given a MarkitCompositeHist object, create or update 14 time series with
     * data points
     * 
     * @param markitCompositeHist
     *            MarkitCompositeHist object
     * 
     */
    void createTimeSeriesDatas (MarkitCompositeHist markitCompositeHist);
}
