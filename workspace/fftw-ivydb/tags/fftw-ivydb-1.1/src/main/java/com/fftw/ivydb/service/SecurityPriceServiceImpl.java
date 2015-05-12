package com.fftw.ivydb.service;

import java.sql.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fftw.ivydb.dao.SecurityPriceDao;
import com.fftw.ivydb.domain.SecurityPrice;
import com.fftw.ivydb.domain.SecurityPricePK;
import com.fftw.ivydb.factory.AbstractDaoFactory;

public class SecurityPriceServiceImpl implements SecurityPriceService
{
    private static final Log logger = LogFactory.getLog(SecurityPriceServiceImpl.class);

    private SecurityPriceDao securityPriceDao;
    
    
    public SecurityPriceServiceImpl ()
    {
        AbstractDaoFactory daoFactory = AbstractDaoFactory.instance(AbstractDaoFactory.JPA);
        securityPriceDao = daoFactory.getSecurityPriceDao();
    }

    public List<SecurityPrice> findByDate (Date date)
    {
        logger.info("findByDate called with date: " + date);
        return securityPriceDao.findByDate(date);
    }

    public SecurityPrice findByID (SecurityPricePK id)
    {
        return securityPriceDao.findByID(id);
    }
    
}
