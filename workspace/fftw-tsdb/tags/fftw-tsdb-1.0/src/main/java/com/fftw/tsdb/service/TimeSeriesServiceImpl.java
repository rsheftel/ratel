package com.fftw.tsdb.service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;

import com.fftw.tsdb.dao.GenericDao;
import com.fftw.tsdb.dao.TimeSeriesDao;
import com.fftw.tsdb.dao.TimeSeriesDataDao;
import com.fftw.tsdb.domain.Attribute;
import com.fftw.tsdb.domain.DataSource;
import com.fftw.tsdb.domain.TimeSeries;
import com.fftw.tsdb.domain.TimeSeriesAttributeMap;
import com.fftw.tsdb.domain.TimeSeriesAttributeMapPK;
import com.fftw.tsdb.domain.TimeSeriesData;
import com.fftw.tsdb.domain.TimeSeriesDataPK;
import com.fftw.tsdb.factory.AbstractDaoFactory;
import com.fftw.tsdb.sdo.TimeSeriesSdo;

public class TimeSeriesServiceImpl implements TimeSeriesService
{
    private static final Log logger = LogFactory.getLog(TimeSeriesServiceImpl.class);

    protected TimeSeriesDao timeSeriesDao;

    protected TimeSeriesDataDao timeSeriesDataDao;

    protected AttributeService attributeService;

    protected AttributeValueService attributeValueService;

    protected GenericDao<TimeSeriesAttributeMap, TimeSeriesAttributeMapPK> timeSeriesAttributeMapDao;

    public TimeSeriesServiceImpl ()
    {
        AbstractDaoFactory daoFactory = AbstractDaoFactory.instance(AbstractDaoFactory.JPA);
        timeSeriesDao = daoFactory.getTimeSeriesDao();
        timeSeriesDataDao = daoFactory.getTimeSeriesDataDao();
        attributeService = new AttributeServiceImpl();
        attributeValueService = new AttributeValueServiceImpl();
        timeSeriesAttributeMapDao = daoFactory.getTimeSeriesAttributeMapDao();
    }

    public TimeSeries findByName (String name)
    {
        name = name.toLowerCase();
        if (logger.isDebugEnabled())
        {
            logger.debug("findByName called with input parameter: " + name);
        }

        try
        {
            TimeSeries timeSeries = timeSeriesDao.findByName(name);
            logger.debug("Found time series by name.");
            return timeSeries;
        }
        catch (EmptyResultDataAccessException ex)
        {
            logger.debug("Did not find time series by name.");
            return null;
        }
    }

    public void createOrUpdateTimeSeriesData (TimeSeries timeSeries, String dataSourceName,
        Double value, Date observationTime)
    {
        dataSourceName = dataSourceName.toLowerCase();
        /*
         * try { findDataAndUpdate(timeSeries, dataSourceName, value,
         * observationTime); } // no match for observation time, so it is a new
         * time series data point catch (EmptyResultDataAccessException ex) {
         */
        if (logger.isDebugEnabled())
        {
            logger.debug("Creating or updating a time series data point for time series "
                + timeSeries.getName() + " with value " + value + ", date " + observationTime
                + ", and data source " + dataSourceName);
        }
        // TODO reattach timeseries so that we don't get
        // LazyInitializationException, is this the right way to
        // reattaching?
        try
        {
            DataSource dataSource = timeSeriesDao.findDataSourceByName(dataSourceName);
            TimeSeriesData timeSeriesData = createTimeSeriesData(timeSeries, dataSource, value,
                observationTime);
            timeSeriesDataDao.update(timeSeriesData); // this is depending on
            // jpa merge to save or
            // update

            /*
             * //Doing my own insert first, then try to update if exists try {
             * timeSeriesDataDao.persist(timeSeriesData); } catch
             * (DataIntegrityViolationException ex) {
             * timeSeriesDataDao.update(timeSeriesData); }
             */
        }
        catch (EmptyResultDataAccessException e)
        {
            logger.equals("createOrUpdateTimeSeriesData - datasource " + dataSourceName
                + " does not exist in database");
            throw new EmptyResultDataAccessException("Datasource " + dataSourceName
                + " does not exist in database.", 1);
        }
        // }
    }

    // TODO fixed so that it doesn't cal findDataAndUpdate anymore
    public void createOrUpdateTimeSeriesDatasBulk (TimeSeries timeSeries, String dataSourceName,
        Map<Date, Double> timesAndValues)
    {
        dataSourceName = dataSourceName.toLowerCase();
        // TODO can the date be same? will it be duplicated keys? May need
        // tuning for bulk load? use a bulk insert/update method from dao
        // instead
        List<TimeSeriesData> timeSeriesDatas = new ArrayList<TimeSeriesData>();
        try
        {
            DataSource dataSource = timeSeriesDao.findDataSourceByName(dataSourceName);
            for (Date observationTime : timesAndValues.keySet())
            {
                Double value = timesAndValues.get(observationTime);
                try
                {
                    findDataAndUpdate(timeSeries, dataSourceName, value, observationTime);
                }
                catch (EmptyResultDataAccessException ex)
                {
                    timeSeriesDatas.add(createTimeSeriesData(timeSeries, dataSource, value,
                        observationTime));
                }
            }
            timeSeriesDataDao.addTimeSeriesDatasBulk(timeSeriesDatas);
        }
        catch (EmptyResultDataAccessException ex)
        {
            throw new EmptyResultDataAccessException("Datasource " + dataSourceName
                + " does not exist in database.", 1);
        }

    }

    public void createOrUpdateTimeSeriesDatasBatch (List<TimeSeriesSdo> timeSeriesSdos,
        DataSource dataSource)
    {
        for (TimeSeriesSdo timeSeriesSdo : timeSeriesSdos)
        {
            if (timeSeriesSdo.getObservationValue() != null)
            {
                TimeSeries timeSeries = createOrGetTimeSeries(timeSeriesSdo.getAttributes(),
                    timeSeriesSdo.getTimeSeriesName());
                TimeSeriesData timeSeriesData = createTimeSeriesData(timeSeries, dataSource,
                    timeSeriesSdo.getObservationValue(), timeSeriesSdo.getObservationDate());
//                timeSeriesDataDao.saveOrUpdate(timeSeriesData);
                try {
                    //TODO is there a way to suppress logging of the exception? maybe catch it at the dao layer?
                    timeSeriesDataDao.persist(timeSeriesData); 
                }
                catch(DataIntegrityViolationException ex) {
                    timeSeriesDataDao.update(timeSeriesData);
                }
                
                /*//TODO doing the try/catch entity exists exception at dao level
                timeSeriesDataDao.persist(timeSeriesData);*/
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Flushing and clearing ..............");
        }
//        timeSeriesDataDao.flush();
        timeSeriesDataDao.clear();
    }

    public TimeSeries createTimeSeries (String timeSeriesName, Map<Attribute, Long> attributes)
    {
        timeSeriesName = timeSeriesName.toLowerCase();
        TimeSeries timeSeries = new TimeSeries();
        timeSeries.setName(timeSeriesName);
        timeSeriesDao.persist(timeSeries);
        for (Attribute attribute : attributes.keySet())
        {
            TimeSeriesAttributeMapPK timeSeriesAttributeMapPK = new TimeSeriesAttributeMapPK();
            timeSeriesAttributeMapPK.setAttribute(attribute);
            TimeSeriesAttributeMap timeSeriesAttributeMap = new TimeSeriesAttributeMap();
            Long attributeValueId = attributes.get(attribute);
            timeSeriesAttributeMap.setId(timeSeriesAttributeMapPK);
            timeSeriesAttributeMap.setAttributeValueId(attributeValueId);
            timeSeriesAttributeMapPK.setTimeSeries(timeSeries);
            timeSeriesAttributeMapDao.save(timeSeriesAttributeMap);
            if (logger.isDebugEnabled())
            {
                logger.debug("Creating time series with name " + timeSeriesName + ", attribute "
                    + attribute.getName() + " ,and attribute value id " + attributeValueId);
            }
        }
        timeSeriesAttributeMapDao.flush();
        timeSeriesAttributeMapDao.clear();

        return timeSeries;
    }

    /**
     * Do we really need this?
     */
    public TimeSeries createTimeSeriesWithData (String timeSeriesName,
        Map<Attribute, Long> attributes, String dataSourceName, Date observationTime, Double value)
    {
        timeSeriesName = timeSeriesName.toLowerCase();
        dataSourceName = dataSourceName.toLowerCase();
        TimeSeries timeSeries = createTimeSeries(timeSeriesName, attributes);
        createOrUpdateTimeSeriesData(timeSeries, dataSourceName, value, observationTime);
        return timeSeries;
    }

    public List<TimeSeriesData> getDataByDataSource (TimeSeries timeSeries, String dataSourceName)
    {
        dataSourceName = dataSourceName.toLowerCase();
        return timeSeriesDao.findDataByDataSource(timeSeries, dataSourceName);
    }

    public List<TimeSeries> findByAttributes (Map<String, String> attributes, String dataSourceName)
    {
        dataSourceName = dataSourceName.toLowerCase();
        return timeSeriesDao.findByAttributes(attributes, dataSourceName);
    }

    public Map<Attribute, Long> getAllAttributeMaps (TimeSeries timeSeries)
    {
        List<TimeSeriesAttributeMap> attributeMappingList = timeSeriesDao
            .getAllAttributeMaps(timeSeries);
        Map<Attribute, Long> attributeMap = new HashMap<Attribute, Long>();
        Iterator<TimeSeriesAttributeMap> mappingIterator = attributeMappingList.iterator();
        while (mappingIterator.hasNext())
        {
            TimeSeriesAttributeMap mapping = mappingIterator.next();
            attributeMap.put(mapping.getId().getAttribute(), mapping.getAttributeValueId());
        }
        return attributeMap;
    }

    /* *************************** private methods **************************** */
    protected TimeSeries createOrGetTimeSeries (Map<Attribute, Long> attributes,
        String timeSeriesName)
    {
        TimeSeries timeSeries = findByName(timeSeriesName);
        if (timeSeries != null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Found time series " + timeSeriesName);
            }
            return timeSeries;
        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Did not find time series, creating a new time series name "
                    + timeSeriesName);
            }
            return createTimeSeries(timeSeriesName, attributes);
        }
    }

    private void findDataAndUpdate (TimeSeries timeSeries, String dataSourceName, Double value,
        Date observationTime)
    {
        TimeSeriesData timeSeriesData = timeSeriesDao.findDataByTimeAndName(timeSeries,
            observationTime, dataSourceName);
        timeSeriesData.setObservationValue(value);
        if (logger.isDebugEnabled())
        {
            logger.debug("Updating time series data..........");
        }
        timeSeriesDataDao.update(timeSeriesData);
    }

    private TimeSeriesData createTimeSeriesData (TimeSeries timeSeries, DataSource dataSource,
        Double value, Date observationTime)
    {
        TimeSeriesDataPK timeSeriesDataPK = new TimeSeriesDataPK();
        timeSeriesDataPK.setTimeSeries(timeSeries);
        timeSeriesDataPK.setDataSource(dataSource);
        timeSeriesDataPK.setObservationTime(observationTime);
        TimeSeriesData timeSeriesData = new TimeSeriesData();
        timeSeriesData.setId(timeSeriesDataPK);
        timeSeriesData.setObservationValue(value);
        return timeSeriesData;

    }

}
