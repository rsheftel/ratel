package com.fftw.tsdb.service;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.fftw.tsdb.domain.Attribute;
import com.fftw.tsdb.domain.TimeSeries;
import com.fftw.tsdb.domain.TimeSeriesData;

public interface TimeSeriesService
{
    /**
     * Find a time series by name
     * 
     * @param name
     *            time series name
     * @return
     */
    TimeSeries findByName (String name);

    /**
     * Insert or update a time series data point given a time series
     * 
     * @param timeSeries
     *            time series object
     * @param dataSourceName
     *            data source name
     * @param value
     *            observation value
     * @param observationTime
     *            observation time
     */
    void createOrUpdateTimeSeriesData (TimeSeries timeSeries, String dataSourceName, Double value,
        Calendar observationTime);

    /**
     * Create a new time series without any data points and insert it into
     * database
     * 
     * @param timeSeriesName
     *            time series name
     * @param attributes
     *            map of all attributes that belong to the time series, key is
     *            attribute object, value is the id of the attribute value
     *            object
     * @return
     */
    TimeSeries createTimeSeries (String timeSeriesName, Map<Attribute, Long> attributes);

    /**
     * Get the list of data points in a time series based on data source
     * 
     * @param timeSeries
     *            time series object
     * @param dataSourceName
     *            data source name
     * @return
     */
    List<TimeSeriesData> findDataByDataSource (TimeSeries timeSeries, String dataSourceName);

    /**
     * Find all the time series data points given, a time series, a data source
     * name, and a date range, the return results will be sorted by obseravtion
     * time in ascending order
     * 
     * @param timeSeries
     * @param dataSourceName
     * @param startDate
     *            takes in java.util.Date
     * @param endDate
     *            takes in java.util.Date
     * @return
     */
    List<TimeSeriesData> findDataByDateRange (String timeSeriesName, String dataSourceName,
        java.util.Date startDate, java.util.Date endDate);

    /**
     * Find a time series given the attributes and datasource
     * 
     * @param attributes
     *            map of attributes that belong to the time series, key is
     *            attribute name, value is attribute value
     * @param dataSourceName
     *            datasource name
     * @return
     */
    List<TimeSeries> findByAttributes (Map<String, String> attributes, String dataSourceName);

    /**
     * Get all the TimeSeriesAttributeMap objects associated with the TimeSeries
     * 
     * @param timeSeries
     * @return
     */
    Map<Attribute, Long> getAllAttributeMaps (TimeSeries timeSeries);
    
}
