package com.fftw.tsdb.dao;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fftw.tsdb.domain.TimeSeriesData;
import com.fftw.tsdb.domain.TimeSeriesDataPK;

public class TimeSeriesDataDaoImpl extends GenericDaoImpl<TimeSeriesData, TimeSeriesDataPK>
    implements TimeSeriesDataDao
{
    private static final Log logger = LogFactory.getLog(TimeSeriesDataDaoImpl.class);

    public TimeSeriesDataDaoImpl (Class<TimeSeriesData> entityClass)
    {
        super(entityClass);
    }

    public void addTimeSeriesDatasBulk (List<TimeSeriesData> timeSeriesDatas)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("addTimeSeriesDatasBulk is called");
        }
        for (int i = 0; i < timeSeriesDatas.size(); i++)
        {
            save(timeSeriesDatas.get(i));
            if (i % 20 == 0)
            {
                if (logger.isDebugEnabled())
                {
                    logger
                        .debug("flushing and clearing in memory data for time series data. Counter i is: "
                            + i);
                }
                flush();
                clear();
            }
        }

    }

   /* @Override
    public void persist (TimeSeriesData timeSeriesData)
    {
        try
        {
            super.persist(timeSeriesData);
        }
        catch (DataIntegrityViolationException ex)
        {
            super.saveOrUpdate(timeSeriesData);
        }
        catch (EntityExistsException ex)
        {
            super.saveOrUpdate(timeSeriesData);
        }
        
    }*/

}
