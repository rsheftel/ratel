package com.fftw.tsdb.service;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import com.fftw.tsdb.domain.Attribute;
import com.fftw.tsdb.domain.DataSource;
import com.fftw.tsdb.domain.TimeSeries;
import com.fftw.tsdb.domain.TimeSeriesData;
import com.fftw.tsdb.sdo.TimeSeriesSdo;

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
        Date observationTime);

    /**
     * Insert or update a list of time series data points given a time series,
     * bulk operation
     * 
     * @param timeSeries
     *            time series object
     * @param dataSourceName
     *            data source name
     * @param timesAndValues
     *            a map that holds bunch of obseration times and values for a
     *            time series
     */
    void createOrUpdateTimeSeriesDatasBulk (TimeSeries timeSeries, String dataSourceName,
        Map<Date, Double> timesAndValues);
    
    /**
     * Insert or update a list of TimeSeriesSdos so that it can flush and clear in batches
     * @param timeSeriesSdos
     */
    void createOrUpdateTimeSeriesDatasBatch(List<TimeSeriesSdo> timeSeriesSdos, DataSource dataSource);
    

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
     * Create a new time series with a data point and insert it into database
     * 
     * @param timeSeriesName
     *            time series name
     * @param attributes
     *            map of all attributes that belong to the time series, key is
     *            attribute object, value is the id of the attribute value
     *            object
     * @param dataSourceName
     *            data source name
     * @param observationTime
     *            observation time
     * @param value
     *            observation value
     * @return
     */
    TimeSeries createTimeSeriesWithData (String timeSeriesName, Map<Attribute, Long> attributes,
        String dataSourceName, Date observationTime, Double value);

    /**
     * Get the list of data points in a time series based on data source
     * 
     * @param timeSeries
     *            time series object
     * @param dataSourceName
     *            data source name
     * @return
     */
    List<TimeSeriesData> getDataByDataSource (TimeSeries timeSeries, String dataSourceName);

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
     * @param timeSeries
     * @return
     */   
    Map<Attribute, Long> getAllAttributeMaps(TimeSeries timeSeries);

}
