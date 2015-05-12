package com.fftw.tsdb.dao;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import com.fftw.tsdb.domain.DataSource;
import com.fftw.tsdb.domain.TimeSeries;
import com.fftw.tsdb.domain.TimeSeriesAttributeMap;
import com.fftw.tsdb.domain.TimeSeriesData;

public interface TimeSeriesDao extends GenericDao<TimeSeries, Long>
{
    /**
     * Find the time series data point given a time series, observation time and
     * the data source name
     * 
     * @param timeSeries
     * @param observationTime
     * @param dataSourceName
     * @return
     */
    TimeSeriesData findDataByTimeAndName (TimeSeries timeSeries, Date observationTime,
        String dataSourceName);

    /**
     * Find all the time series data points with the same data source name in a
     * time series
     * 
     * @param timeSeries
     * @param dataSourceName
     * @return
     */
    List<TimeSeriesData> findDataByDataSource (TimeSeries timeSeries, String dataSourceName);

    /**
     * Find a time series given the attributes and datasource
     * 
     * @param attributes
     * @param dataSourceName
     * @return
     */
    List<TimeSeries> findByAttributes (Map<String, String> attributes, String dataSourceName);

    /**
     * Find the data source object based on name
     * 
     * @param dataSourceName
     * @return
     */
    DataSource findDataSourceByName (String dataSourceName);
    
    List<TimeSeriesAttributeMap> getAllAttributeMaps(TimeSeries timeSeries);
}
