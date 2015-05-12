package com.fftw.ivydb.service;

import java.sql.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fftw.ivydb.dao.StandardOptionPriceDao;
import com.fftw.ivydb.domain.StandardOptionPrice;
import com.fftw.ivydb.domain.StandardOptionPricePK;
import com.fftw.ivydb.factory.AbstractDaoFactory;

public class StandardOptionPriceServiceImpl implements StandardOptionPriceService
{
    private static final Log logger = LogFactory.getLog(StandardOptionPriceServiceImpl.class);

    private StandardOptionPriceDao standardOptionPriceDao;

    
    public StandardOptionPriceServiceImpl ()
    {
        AbstractDaoFactory daoFactory = AbstractDaoFactory.instance(AbstractDaoFactory.JPA);
        standardOptionPriceDao = daoFactory.getStandardOptionPriceDao();
    }

    public List<StandardOptionPrice> findByDate (Date date)
    {
        logger.info("findByDate called with date: " + date);
        return standardOptionPriceDao.findByDate(date);
    }

    public StandardOptionPrice findByID (StandardOptionPricePK id)
    {
        return standardOptionPriceDao.findByID(id);
    }

}
