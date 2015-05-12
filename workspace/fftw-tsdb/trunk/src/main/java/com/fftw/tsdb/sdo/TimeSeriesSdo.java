package com.fftw.tsdb.sdo;

import java.util.Calendar;
import java.util.Map;

import com.fftw.tsdb.domain.Attribute;

public class TimeSeriesSdo
{
    private String timeSeriesName;

    private String dataSourceName;

    private Calendar observationDate;

    private Double observationValue;

    private Map<Attribute, Long> attributes;

    public TimeSeriesSdo (String timeSeriesName, String dataSourceName, Calendar observationDate,
        Double observationValue, Map<Attribute, Long> attributes)
    {
        this.timeSeriesName = timeSeriesName.toLowerCase();
        this.dataSourceName = dataSourceName.toLowerCase();
        this.observationDate = observationDate;
        this.observationValue = observationValue;
        this.attributes = attributes;
    }

    public Map<Attribute, Long> getAttributes ()
    {
        return attributes;
    }

    public void setAttributes (Map<Attribute, Long> attributes)
    {
        this.attributes = attributes;
    }

    public String getDataSourceName ()
    {
        return dataSourceName;
    }

    public void setDataSourceName (String dataSourceName)
    {
        this.dataSourceName = dataSourceName;
    }

    public Calendar getObservationDate ()
    {
        return observationDate;
    }

    public void setObservationDate (Calendar observationDate)
    {
        this.observationDate = observationDate;
    }

    public Double getObservationValue ()
    {
        return observationValue;
    }

    public void setObservationValue (Double observationValue)
    {
        this.observationValue = observationValue;
    }

    public String getTimeSeriesName ()
    {
        return timeSeriesName;
    }

    public void setTimeSeriesName (String timeSeriesName)
    {
        this.timeSeriesName = timeSeriesName;
    }
    
    

}
