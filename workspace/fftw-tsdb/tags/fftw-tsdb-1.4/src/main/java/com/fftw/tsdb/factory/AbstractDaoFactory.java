package com.fftw.tsdb.factory;

import java.lang.reflect.Constructor;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.fftw.tsdb.dao.GenericDao;
import com.fftw.tsdb.dao.JdbcTimeSeriesDao;
import com.fftw.tsdb.dao.TimeSeriesDao;
import com.fftw.tsdb.dao.cds.MarkitCompositeHistDao;
import com.fftw.tsdb.domain.Attribute;
import com.fftw.tsdb.domain.Ccy;
import com.fftw.tsdb.domain.GeneralAttributeValue;
import com.fftw.tsdb.domain.Ticker;
import com.fftw.tsdb.domain.TimeSeriesAttributeMap;
import com.fftw.tsdb.domain.TimeSeriesAttributeMapPK;
import com.fftw.tsdb.domain.TimeSeriesData;
import com.fftw.tsdb.domain.TimeSeriesDataPK;
import com.fftw.tsdb.domain.cds.CdsTicker;

public abstract class AbstractDaoFactory
{
    public static final Class<JpaDaoFactory> JPA = com.fftw.tsdb.factory.JpaDaoFactory.class;

    public static final Class<JdbcDaoFactory> JDBC = com.fftw.tsdb.factory.JdbcDaoFactory.class;

    private static ApplicationContext jpaContext;

    private static ApplicationContext jdbcContext;

    static
    {
        jpaContext = new ClassPathXmlApplicationContext(new String[]
        {
            "classpath:TSDB-Jpa-ApplicationContext.xml"
        });
        jdbcContext = new ClassPathXmlApplicationContext(new String[]
        {
            "classpath:TSDB-Jdbc-ApplicationContext.xml"
        });
    }

    public static AbstractDaoFactory instance (Class factory)
    {
        try
        {
            Constructor constructor = factory.getDeclaredConstructor(ApplicationContext.class);
            if (factory.getName().equals(JPA.getName()))
            {
                return (AbstractDaoFactory)constructor.newInstance(jpaContext);
            }
            else
            {
                return (AbstractDaoFactory)constructor.newInstance(jdbcContext);
            }
        }
        catch (SecurityException e)
        {
            throw new RuntimeException("Couldn't create DaoFactory " + factory + ": "
                + e.getMessage());
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException("Couldn't create DaoFactory " + factory + ": "
                + e.getMessage());
        }
        catch (Exception e)
        {
            throw new RuntimeException("Couldn't create DaoFactory " + factory + ": "
                + e.getMessage());
        }
    }

    public JdbcTimeSeriesDao getJdbcTimeSeriesDao ()
    {
        return null;
    }

    public GenericDao<Attribute, Long> getAttributeDao ()
    {
        return null;
    }

    public GenericDao<Ticker, Long> getTickerDao ()
    {
        return null;
    }

    public GenericDao<CdsTicker, Long> getCdsTickerDao ()
    {
        return null;
    }

    public GenericDao<Ccy, Long> getCcyDao ()
    {
        return null;
    }

    public GenericDao<GeneralAttributeValue, Long> getGeneralAttributeValueDao ()
    {
        return null;
    }

    public GenericDao<TimeSeriesData, TimeSeriesDataPK> getTimeSeriesDataDao ()
    {
        return null;
    }

    public GenericDao<TimeSeriesAttributeMap, TimeSeriesAttributeMapPK> getTimeSeriesAttributeMapDao ()
    {
        return null;
    }

    public TimeSeriesDao getTimeSeriesDao ()
    {
        return null;
    }

    public MarkitCompositeHistDao getMarkitCompositeHistDao ()
    {
        return null;
    }
}
