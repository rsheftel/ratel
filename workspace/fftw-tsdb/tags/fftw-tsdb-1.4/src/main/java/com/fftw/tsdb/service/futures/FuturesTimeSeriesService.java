package com.fftw.tsdb.service.futures;

import com.fftw.tsdb.domain.TimeSeries;
import com.fftw.tsdb.service.TimeSeriesService;

public interface FuturesTimeSeriesService extends TimeSeriesService
{
    /**
     * Retrieve the futures options time series by name if it exists, otherwise,
     * create a new futures options time series without data point and insert
     * into database
     * 
     * @param optionTypeString
     *            option type
     * @param quoteTypeString
     *            quote type
     * @param tickerString
     *            ticker
     * @param strikeString
     *            strike
     * @return
     */
    TimeSeries createOrGetFuturesOptionsTimeSeries (String optionTypeString,
        String quoteTypeString, String tickerString, String strikeString);
}
