
package com.fftw.tsdb.service.irs;

import com.fftw.tsdb.domain.TimeSeries;
import com.fftw.tsdb.service.TimeSeriesService;


public interface IrsTimeSeriesService extends TimeSeriesService {
    /**
     * 
     * Retrieve the swap time series
     */
    TimeSeries createOrGetIrsTimeSeries (String ccy, String quoteSide, String quoteConvention, String tenor);

}
