package com.fftw.tsdb.factory;

import com.fftw.tsdb.dao.GenericDao;
import com.fftw.tsdb.dao.TimeSeriesDao;
import com.fftw.tsdb.dao.TimeSeriesDataDao;
import com.fftw.tsdb.dao.cds.MarkitCompositeHistDao;
import com.fftw.tsdb.domain.Attribute;
import com.fftw.tsdb.domain.GeneralAttributeValue;
import com.fftw.tsdb.domain.Ticker;
import com.fftw.tsdb.domain.TimeSeriesAttributeMap;
import com.fftw.tsdb.domain.TimeSeriesAttributeMapPK;
import com.fftw.tsdb.domain.cds.CdsTicker;

public abstract class AbstractDaoFactory
{
    public static final Class<JpaDaoFactory> JPA = com.fftw.tsdb.factory.JpaDaoFactory.class;
    
    public static AbstractDaoFactory instance(Class factory) {
        try {
            return (AbstractDaoFactory) factory.newInstance();
        }
        catch(Exception ex) {
            throw new RuntimeException("Couldn't create DaoFactory: " + factory);
        }
    }
    
    public abstract GenericDao<Attribute, Long> getAttributeDao();
    public abstract GenericDao<Ticker, Long> getTickerDao();
    public abstract GenericDao<CdsTicker, Long> getCdsTickerDao();
    public abstract GenericDao<GeneralAttributeValue, Long> getGeneralAttributeValueDao();
    public abstract TimeSeriesDataDao getTimeSeriesDataDao();
    public abstract GenericDao<TimeSeriesAttributeMap, TimeSeriesAttributeMapPK> getTimeSeriesAttributeMapDao();
    public abstract TimeSeriesDao getTimeSeriesDao();
    public abstract MarkitCompositeHistDao getMarkitCompositeHistDao();
}
