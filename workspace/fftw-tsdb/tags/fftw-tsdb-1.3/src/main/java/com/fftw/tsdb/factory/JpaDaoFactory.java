package com.fftw.tsdb.factory;

import org.springframework.context.ApplicationContext;

import com.fftw.tsdb.dao.GenericDao;
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

public class JpaDaoFactory extends AbstractDaoFactory
{
    private ApplicationContext context;

    public JpaDaoFactory (ApplicationContext context)
    {
        this.context = context;
        /*this.context = new ClassPathXmlApplicationContext(new String[]
        {
            "classpath:ApplicationContext.xml"
        });*/
    }

    @Override
    public GenericDao<Attribute, Long> getAttributeDao ()
    {
        return (GenericDao<Attribute, Long>)context.getBean("AttributeDao");
    }

    @Override
    public GenericDao<GeneralAttributeValue, Long> getGeneralAttributeValueDao ()
    {
        return (GenericDao<GeneralAttributeValue, Long>)context.getBean("GeneralAttributeValueDao");
    }

    @Override
    public GenericDao<Ticker, Long> getTickerDao ()
    {
        return (GenericDao<Ticker, Long>)context.getBean("TickerDao");
    }

    @Override
    public GenericDao<CdsTicker, Long> getCdsTickerDao ()
    {
        return (GenericDao<CdsTicker, Long>)context.getBean("CdsTickerDao");
    }

    @Override
    public GenericDao<Ccy, Long> getCcyDao ()
    {
        return (GenericDao<Ccy, Long>)context.getBean("CcyDao");
    }

    @Override
    public GenericDao<TimeSeriesData, TimeSeriesDataPK> getTimeSeriesDataDao ()
    {
        return (GenericDao<TimeSeriesData, TimeSeriesDataPK>)context.getBean("TimeSeriesDataDao");
    }

    @Override
    public TimeSeriesDao getTimeSeriesDao ()
    {
        return (TimeSeriesDao)context.getBean("TimeSeriesDao");
    }

    @Override
    public GenericDao<TimeSeriesAttributeMap, TimeSeriesAttributeMapPK> getTimeSeriesAttributeMapDao ()
    {
        return (GenericDao<TimeSeriesAttributeMap, TimeSeriesAttributeMapPK>)context
            .getBean("TimeSeriesAttributeMapDao");
    }

    @Override
    public MarkitCompositeHistDao getMarkitCompositeHistDao ()
    {
        return (MarkitCompositeHistDao)context.getBean("MarkitCompositeHistDao");
    }

}
