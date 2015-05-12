package com.fftw.tsdb.dao;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fftw.tsdb.domain.Attribute;
import com.fftw.tsdb.domain.DataSource;
import com.fftw.tsdb.domain.TimeSeries;
import com.fftw.tsdb.domain.TimeSeriesAttributeMap;
import com.fftw.tsdb.domain.TimeSeriesData;

@Repository
@Transactional
public class TimeSeriesDaoImpl extends GenericDaoImpl<TimeSeries, Long> implements TimeSeriesDao
{
    private static final Log logger = LogFactory.getLog(TimeSeriesDaoImpl.class);

    public TimeSeriesDaoImpl (Class<TimeSeries> entityClass)
    {
        super(entityClass);
    }

    public TimeSeriesData findDataByTimeAndName (TimeSeries timeSeries, Date observationTime,
        String dataSourceName)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("findDataByTimeAndName called with time series - " + timeSeries.getName()
                + ", date - " + observationTime + ", and data source name - " + dataSourceName);
        }

        return (TimeSeriesData)em.createNamedQuery("TimeSeriesData.findDataByTimeAndName")
            .setParameter("date", observationTime).setParameter("name", dataSourceName)
            .setParameter("timeSeries", timeSeries).getSingleResult();
    }

   /* public void addTimeSeriesData (TimeSeries timeSeries, TimeSeriesData timeSeriesData,
        Date observationTime, Double value, String dataSourceName)
    {
        timeSeries = em.getReference(TimeSeries.class, timeSeries.getId());
        timeSeries.addTimeSeriesData(timeSeriesData);
        persist(timeSeries);
    }

    public void addTimeSeriesDatasBulk (TimeSeries timeSeries, List<TimeSeriesData> timeSeriesDatas)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("addTimeSeriesDatasBulk is called");
        }
        timeSeries = em.getReference(TimeSeries.class, timeSeries.getId());
        for (int i = 0; i < timeSeriesDatas.size(); i++)
        {
            timeSeries.addTimeSeriesData(timeSeriesDatas.get(i));
            save(timeSeries);
            if (i % 20 == 0)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("flushing and clearing in memory data. Counter i is: " + i);
                }
                flush();
                clear();
            }
        }
    }*/

    public List<TimeSeriesData> findDataByDataSource (TimeSeries timeSeries, String dataSourceName)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("findDataByDataSource called with time series - " + timeSeries.getName()
                + " and data source name - " + dataSourceName);
        }
        List<TimeSeriesData> timeSeriesDatas = (List<TimeSeriesData>)em.createNamedQuery(
            "TimeSeriesData.findDataByDataSource").setParameter("name", dataSourceName).
            setParameter("timeSeries", timeSeries).getResultList();
        return timeSeriesDatas;
    }

    public List<TimeSeriesAttributeMap> getAllAttributeMaps (TimeSeries timeSeries)
    {
        List<TimeSeriesAttributeMap> timeSeriesAttributeMaps = (List<TimeSeriesAttributeMap>)em
            .createNamedQuery("TimeSeries.findAllAttributeMaps").setParameter("timeSeries",
                timeSeries).getResultList();
        return timeSeriesAttributeMaps;
    }

    // TODO add exception handling and add handling of cds_ticker attribute
    public List<TimeSeries> findByAttributes (Map<String, String> attributes, String dataSourceName)
    {
        if (logger.isDebugEnabled()) {
            logger.debug("findByAttributes called ...");
        }
        String value;
        Map<Long, Long> attributeMapIds = new HashMap<Long, Long>();
        Query query;
        for (String key : attributes.keySet())
        {
            value = attributes.get(key);
            if (logger.isDebugEnabled()) {
                logger.debug("attribute key is " + key + ", value is " + value);
            }
            Attribute attribute = (Attribute)em.createNamedQuery("Attribute.getAttributeByName")
                .setParameter("name", key).getSingleResult();
            String entity = getEntityFromTableName(attribute.getTableName());
            if (entity != null)
            {
                query = em.createQuery("select e.id from " + entity
                    + " as e where e.value = :value");
                query.setParameter("value", value);
                Long attributeValueID = (Long)query.getSingleResult();
                attributeMapIds.put(attribute.getId(), attributeValueID);
            }
        }
        // self join to get TimeSeries
        if (attributeMapIds != null && attributeMapIds.size() > 0)
        {
            String selfJoinQuery = "select tsam1.id.timeSeries.id ";
            StringBuffer fromClause = new StringBuffer("from ");
            StringBuffer whereClause = new StringBuffer("where ");
            int count = 1;
            for (Long key : attributeMapIds.keySet())
            {
                fromClause.append("TimeSeriesAttributeMap tsam" + count + ", ");
                whereClause.append("tsam" + count + ".id.attribute.id = " + key + " and tsam"
                    + count + ".attributeValueId = " + attributeMapIds.get(key) + " and ");
                count++;
            }

            for (int i = 1; i < attributeMapIds.size(); i++)
            {
                whereClause.append("tsam" + i + ".id.timeSeries.id = tsam" + (i + 1)
                    + ".id.timeSeries.id and ");
            }
            // remove the ',' from end of from clause and 'and from end of where
            // clause
            selfJoinQuery += fromClause.substring(0, fromClause.length() - 2) + " "
                + whereClause.substring(0, whereClause.length() - 4);
            // System.out.println("*********************************:\n" +
            // selfJoinQuery);
            query = em
                .createQuery("select t from TimeSeries as t left join fetch t.timeSeriesDatas datas where t.id in ("
                    + selfJoinQuery + ") and datas.id.dataSource.name = :dataSource");
            query.setParameter("dataSource", dataSourceName);
            return query.getResultList();
        }
        return null;
    }

    public DataSource findDataSourceByName (String dataSourceName)
    {
        return (DataSource)em.createNamedQuery("DataSource.getDataSourceByName").setParameter(
            "name", dataSourceName).getSingleResult();
    }

    /* *************************** private methods **************************** */

    private String getEntityFromTableName (String tableName) throws IllegalArgumentException
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("getEntityFromTableName called with param: " + tableName);
        }
        if ("general_attribute_value".equalsIgnoreCase(tableName))
        {
            logger.debug("returning value 'GeneralAttributeValue'");
            return "GeneralAttributeValue";
        }
        else if ("ticker".equalsIgnoreCase(tableName))
        {
            logger.debug("returning value 'Ticker'");
            return "Ticker";
        }
        else
        {
            logger.debug("returning value null");
            return null;
        }
    }
}
