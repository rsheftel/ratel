package com.fftw.tsdb.functional.service;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.fftw.tsdb.domain.TimeSeries;
import com.fftw.tsdb.domain.TimeSeriesData;
import com.fftw.tsdb.service.TimeSeriesService;
import com.fftw.tsdb.service.TimeSeriesServiceImpl;
import com.fftw.tsdb.service.batch.TimeSeriesBatchService;
import com.fftw.tsdb.service.batch.TimeSeriesBatchServiceImpl;

public class TimeSeriesServiceImplFuncTest
{
    /*@Test(groups = {"functest"})
    public void testLoadTimeSeriesByAttributes ()
    {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("quote_type", "underlying_price");
        attributes.put("ticker", "edk07");
        attributes.put("option_type", "straddle");
        attributes.put("strike", "94.625");
        TimeSeriesService timeSeriesService = new TimeSeriesServiceImpl();
        List<TimeSeries> timeSeriesResults = timeSeriesService.loadTimeSeriesByAttributes(
            attributes, "goldman");
        for (TimeSeries timeSeries : timeSeriesResults)
        {
            for (TimeSeriesData timeSeriesData : timeSeries.getTimeSeriesDatas())
            {
                System.out.println("Time Series data " + timeSeriesData.getObservationValue());
            }
        }
    }*/
    
    /*@Test(groups = {"functest"})
    public void testCreateTimeSeriesByName() {
        Calendar cal = Calendar.getInstance();
        System.out.println("-----------time is: " + cal.getTimeInMillis());
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("quote_type", "underlying_price");
        attributes.put("ticker", "edm07");
        attributes.put("option_type", "straddle");
        attributes.put("strike", "91.629");
        TimeSeriesService timeSeriesService = new TimeSeriesServiceImpl();
        timeSeriesService.createOrUpdateTimeSeriesByName("edm07 str 91.629 underlying_price", "goldman", new Double(95.333), cal.getTime(), attributes);
    }*/
    
    /*@Test(groups = {"functest"})
    public void testUpdateTimeSeriesByName() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JUNE, 15, 14, 17, 15);
        cal.set(Calendar.MILLISECOND, 953);
        Date observationTime = cal.getTime();
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("quote_type", "underlying_price");
        attributes.put("ticker", "edm07");
        attributes.put("option_type", "straddle");
        attributes.put("strike", "91.629");
        TimeSeriesService timeSeriesService = new TimeSeriesServiceImpl();
        timeSeriesService.createOrUpdateTimeSeriesByName("edm07 str 91.629 underlying_price", "goldman", new Double(96.000), observationTime, attributes);
    }*/
    
}
