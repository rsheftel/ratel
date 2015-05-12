package com.fftw.tsdb.domain;

import java.io.Serializable;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

public class TimeSeriesAttributeMapPK implements Serializable
{
    private static final long serialVersionUID = 4922927632467626679L;

    private TimeSeries timeSeries;

    private Attribute attribute;

    public TimeSeriesAttributeMapPK ()
    {
    }

    @ManyToOne
    @JoinColumn(name = "time_series_id", nullable = false)
    public TimeSeries getTimeSeries ()
    {
        return timeSeries;
    }

    public void setTimeSeries (TimeSeries timeSeries)
    {
        this.timeSeries = timeSeries;
    }

    @ManyToOne
    @JoinColumn(name = "attribute_id")
    public Attribute getAttribute ()
    {
        return attribute;
    }

    public void setAttribute (Attribute attribute)
    {
        this.attribute = attribute;
    }

    @Override
    public int hashCode ()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((attribute == null) ? 0 : attribute.hashCode());
        result = PRIME * result + ((timeSeries == null) ? 0 : timeSeries.hashCode());
        return result;
    }

    @Override
    public boolean equals (Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final TimeSeriesAttributeMapPK other = (TimeSeriesAttributeMapPK)obj;
        if (attribute == null)
        {
            if (other.attribute != null)
            {
                return false;
            }
        }
        else if (!attribute.equals(other.attribute))
        {
            return false;
        }
        if (timeSeries == null)
        {
            if (other.timeSeries != null)
            {
                return false;
            }
        }
        else if (!timeSeries.equals(other.timeSeries))
        {
            return false;
        }
        return true;
    }

}
