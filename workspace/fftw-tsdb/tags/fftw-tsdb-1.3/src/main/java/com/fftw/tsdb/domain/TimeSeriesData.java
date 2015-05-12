package com.fftw.tsdb.domain;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "time_series_data")
@NamedQueries(
{
    @NamedQuery(name = "TimeSeriesData.findDataByTimeAndName", query = "select data from TimeSeriesData as data where data.id.observationTime = :date and data.id.dataSource.name = :name and data.id.timeSeries = :timeSeries"),
    @NamedQuery(name = "TimeSeriesData.findDataByDataSource", query = "select data from TimeSeriesData as data where data.id.dataSource.name = :name and data.id.timeSeries = :timeSeries"),
    @NamedQuery(name = "TimeSeriesData.findDataByDateRange", query = "select data from TimeSeriesData as data where data.id.dataSource.name = :name and data.id.timeSeries.name = :timeSeriesName and data.id.observationTime between :startDate and :endDate order by data.id.observationTime")
})
public class TimeSeriesData implements Serializable
{
    private static final long serialVersionUID = -4392760567583967238L;

    private TimeSeriesDataPK id;

    private Double observationValue;

    public TimeSeriesData ()
    {
    }

    @Id
    public TimeSeriesDataPK getId ()
    {
        return id;
    }

    public void setId (TimeSeriesDataPK timeSeriesDataPK)
    {
        this.id = timeSeriesDataPK;
    }

    @Column(name = "observation_value")
    public Double getObservationValue ()
    {
        return observationValue;
    }

    public void setObservationValue (Double observationValue)
    {
        this.observationValue = observationValue;
    }

    @Transient
    public Calendar getObservationTime ()
    {
        return id.getObservationTime();
    }
}
