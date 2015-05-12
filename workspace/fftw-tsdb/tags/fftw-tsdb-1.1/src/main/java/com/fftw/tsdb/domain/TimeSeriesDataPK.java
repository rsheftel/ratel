package com.fftw.tsdb.domain;

import java.io.Serializable;
import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class TimeSeriesDataPK implements Serializable
{
    private static final long serialVersionUID = -6391825569279811954L;

    private TimeSeries timeSeries;

    private DataSource dataSource;

    private Date observationTime;

    public TimeSeriesDataPK ()
    {
    }

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "time_series_id", nullable = false)
    public TimeSeries getTimeSeries ()
    {
        return timeSeries;
    }

    public void setTimeSeries (TimeSeries timeSeries)
    {
        this.timeSeries = timeSeries;
    }

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "data_source_id")
    public DataSource getDataSource ()
    {
        return dataSource;
    }

    public void setDataSource (DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    @Column(name = "observation_time")
    public Date getObservationTime ()
    {
        return observationTime;
    }

    public void setObservationTime (Date observationTime)
    {
        this.observationTime = observationTime;
    }

    @Override
    public int hashCode ()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((observationTime == null) ? 0 : observationTime.hashCode());
        result = PRIME * result + ((dataSource == null) ? 0 : dataSource.hashCode());
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
        final TimeSeriesDataPK other = (TimeSeriesDataPK)obj;
        if (observationTime == null)
        {
            if (other.observationTime != null)
            {
                return false;
            }
        }
        else if (!observationTime.equals(other.observationTime))
        {
            return false;
        }
        if (dataSource == null)
        {
            if (other.dataSource != null)
            {
                return false;
            }
        }
        else if (!dataSource.equals(other.dataSource))
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
