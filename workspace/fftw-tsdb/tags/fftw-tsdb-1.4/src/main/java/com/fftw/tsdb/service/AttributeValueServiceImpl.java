package com.fftw.tsdb.service;

import org.springframework.dao.EmptyResultDataAccessException;

import com.fftw.tsdb.dao.GenericDao;
import com.fftw.tsdb.domain.Ccy;
import com.fftw.tsdb.domain.GeneralAttributeValue;
import com.fftw.tsdb.domain.Ticker;
import com.fftw.tsdb.domain.cds.CdsTicker;
import com.fftw.tsdb.factory.AbstractDaoFactory;

public class AttributeValueServiceImpl implements AttributeValueService
{
    private GenericDao<Ticker, Long> tickerDao;

    private GenericDao<CdsTicker, Long> cdsTickerDao;

    private GenericDao<Ccy, Long> ccyDao;

    private GenericDao<GeneralAttributeValue, Long> generalAttributeValueDao;

    public AttributeValueServiceImpl ()
    {
        AbstractDaoFactory daoFactory = AbstractDaoFactory.instance(AbstractDaoFactory.JPA);
        tickerDao = daoFactory.getTickerDao();
        cdsTickerDao = daoFactory.getCdsTickerDao();
        ccyDao = daoFactory.getCcyDao();
        generalAttributeValueDao = daoFactory.getGeneralAttributeValueDao();
    }

    public GenericDao<Ticker, Long> getTickerDao ()
    {
        return tickerDao;
    }

    public GenericDao<CdsTicker, Long> getCdsTickerDao ()
    {
        return cdsTickerDao;
    }

    public GenericDao<Ccy, Long> getCcyDao ()
    {
        return ccyDao;
    }

    public GenericDao<GeneralAttributeValue, Long> getGeneralAttributeValueDao ()
    {
        return generalAttributeValueDao;
    }

    public Ticker createOrGetTicker (String value, String description)
    {
        value = value.toLowerCase();
        try
        {
            Ticker ticker = tickerDao.findByName(value);
            return ticker;
        }
        catch (EmptyResultDataAccessException ex)
        {
            Ticker ticker = new Ticker();
            ticker.setName(value);
            ticker.setDescription(description);
            tickerDao.persist(ticker);
            return ticker;
        }

    }

    public CdsTicker createOrGetCdsTicker (String value, Ccy ccy, GeneralAttributeValue docClause,
        Ticker ticker, GeneralAttributeValue tier)
    {
        value = value.toLowerCase();
        try
        {
            CdsTicker cdsTicker = cdsTickerDao.findByName(value);
            return cdsTicker;
        }
        catch (EmptyResultDataAccessException ex)
        {
            CdsTicker cdsTicker = new CdsTicker();
            cdsTicker.setName(value);
            cdsTicker.setCcy(ccy);
            cdsTicker.setDocClause(docClause);
            cdsTicker.setTicker(ticker);
            cdsTicker.setTier(tier);
            cdsTickerDao.persist(cdsTicker);
            return cdsTicker;
        }
    }

    public Ccy getCcy (String value)
    {
        value = value.toLowerCase();
        return ccyDao.findByName(value);
    }

    public GeneralAttributeValue createOrGetGeneralAttributeValue (String value)
    {
        value = value.toLowerCase();
        try
        {
            GeneralAttributeValue generalAttributeValue = generalAttributeValueDao
                .findByName(value);
            return generalAttributeValue;
        }
        catch (EmptyResultDataAccessException ex)
        {
            GeneralAttributeValue generalAttributeValue = new GeneralAttributeValue();
            generalAttributeValue.setName(value);
            generalAttributeValueDao.persist(generalAttributeValue);
            return generalAttributeValue;
        }
    }

    public GeneralAttributeValue getGeneralAttributeValue (String value)
    {
        value = value.toLowerCase();
        return generalAttributeValueDao.findByName(value);
    }

}
