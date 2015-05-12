package com.fftw.metadb.domain;

import java.util.Date;
//TODO Need a TSDB class to map to TSDB table, get time series data, sort by date
//put results in here.
public class TimeSeriesResults
{
    private Date timestamp;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Double volume;
    private Double openInterest;
    
    public TimeSeriesResults (Date timestamp, Double open, Double high, Double low, Double close, 
        Double volume, Double openInterest)
    {
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.openInterest = openInterest;
    }
    
    public TimeSeriesResults()
    {
    }
    
    public Date getTimestamp ()
    {
        return timestamp;
    }

    public Double getClose ()
    {
        return close;
    }
    public void setClose (Double close)
    {
        this.close = close;
    }
    public Double getHigh ()
    {
        return high;
    }
    public void setHigh (Double high)
    {
        this.high = high;
    }
    public Double getLow ()
    {
        return low;
    }
    public void setLow (Double low)
    {
        this.low = low;
    }
    public Double getOpen ()
    {
        return open;
    }
    public void setOpen (Double open)
    {
        this.open = open;
    }
    public Double getOpenInterest ()
    {
        return openInterest;
    }
    public void setOpenInterest (Double openInterest)
    {
        this.openInterest = openInterest;
    }
    public Double getVolume ()
    {
        return volume;
    }
    public void setVolume (Double volume)
    {
        this.volume = volume;
    }  
}
