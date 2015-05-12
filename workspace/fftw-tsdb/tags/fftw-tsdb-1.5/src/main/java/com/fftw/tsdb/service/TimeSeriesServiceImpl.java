package com.fftw.tsdb.service;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;

import com.fftw.tsdb.dao.GenericDao;
import com.fftw.tsdb.dao.TimeSeriesDao;
import com.fftw.tsdb.domain.Attribute;
import com.fftw.tsdb.domain.DataSource;
import com.fftw.tsdb.domain.TimeSeries;
import com.fftw.tsdb.domain.TimeSeriesAttributeMap;
import com.fftw.tsdb.domain.TimeSeriesAttributeMapPK;
import com.fftw.tsdb.domain.TimeSeriesData;
import com.fftw.tsdb.domain.TimeSeriesDataPK;
import com.fftw.tsdb.factory.AbstractDaoFactory;

public class TimeSeriesServiceImpl implements TimeSeriesService
{
    private static final Log logger = LogFactory.getLog(TimeSeriesServiceImpl.class);

    protected TimeSeriesDao timeSeriesDao;

    protected GenericDao<TimeSeriesData, TimeSeriesDataPK> timeSeriesDataDao;

    protected AttributeService attributeService;

    protected AttributeValueService attributeValueService;

    protected GenericDao<TimeSeriesAttributeMap, TimeSeriesAttributeMapPK> timeSeriesAttributeMapDao;

    public TimeSeriesServiceImpl ()
    {
        AbstractDaoFactory jpaDaoFactory = AbstractDaoFactory.instance(AbstractDaoFactory.JPA);
        timeSeriesDao = jpaDaoFactory.getTimeSeriesDao();
        timeSeriesDataDao = jpaDaoFactory.getTimeSeriesDataDao();
        attributeService = new AttributeServiceImpl();
        attributeValueService = new AttributeValueServiceImpl();
        timeSeriesAttributeMapDao = jpaDaoFactory.getTimeSeriesAttributeMapDao();
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

    public TimeSeries findByID (Long id)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("findByID called with input parameter: " + id);
        }

        try
        {
            TimeSeries timeSeries = timeSeriesDao.findByID(id);
            logger.debug("Found time series by ID.");
            return timeSeries;
        }
        catch (EmptyResultDataAccessException ex)
        {
            logger.debug("Did not find time series by ID.");
            return null;
        }
    }

    public void createOrUpdateTimeSeriesData (TimeSeries timeSeries, String dataSourceName,
        Double value, Calendar observationTime)
    {
        dataSourceName = dataSourceName.toLowerCase();
        if (logger.isDebugEnabled())
        {
            logger.debug("Creating or updating a time series data point for time series "
                + timeSeries.getName() + " with value " + value + ", date " + observationTime
                + ", and data source " + dataSourceName);
        }
        try
        {
            DataSource dataSource = timeSeriesDao.findDataSourceByName(dataSourceName);
            TimeSeriesData timeSeriesData = createTimeSeriesData(timeSeries, dataSource, value,
                observationTime);
            timeSeriesDataDao.update(timeSeriesData); // this is depending on
        }
        catch (EmptyResultDataAccessException e)
        {
            logger.equals("createOrUpdateTimeSeriesData - datasource " + dataSourceName
                + " does not exist in database");
            throw new EmptyResultDataAccessException("Datasource " + dataSourceName
                + " does not exist in database.", 1);
        }
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

    public List<TimeSeriesData> findDataByDataSource (TimeSeries timeSeries, String dataSourceName)
    {
        dataSourceName = dataSourceName.toLowerCase();
        return timeSeriesDao.findDataByDataSource(timeSeries, dataSourceName);
    }

    public List<TimeSeriesData> findDataByDateRange (String timeSeriesName, String dataSourceName,
        Date startDate, Date endDate)
    {
        Calendar start = Calendar.getInstance();
        start.setTime(startDate);
        Calendar end = Calendar.getInstance();
        end.setTime(endDate);
        dataSourceName = dataSourceName.toLowerCase();
        return timeSeriesDao.findDataByDateRange(timeSeriesName, dataSourceName, start, end);
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
        else if (attributes == null)
        {
            return null;
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

    private TimeSeriesData createTimeSeriesData (TimeSeries timeSeries, DataSource dataSource,
        Double value, Calendar observationTime)
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
