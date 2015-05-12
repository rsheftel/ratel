package com.fftw.ivydb.factory;

import org.springframework.context.ApplicationContext;

import com.fftw.ivydb.dao.SecurityPriceDao;
import com.fftw.ivydb.dao.StandardOptionPriceDao;

public class JpaDaoFactory extends AbstractDaoFactory
{
    private ApplicationContext context;

    public JpaDaoFactory (ApplicationContext context)
    {
        this.context = context;
    }

    @Override
    public SecurityPriceDao getSecurityPriceDao ()
    {
        return (SecurityPriceDao)context.getBean("SecurityPriceDao");
    }

    @Override
    public StandardOptionPriceDao getStandardOptionPriceDao ()
    {
        return (StandardOptionPriceDao)context.getBean("StandardOptionPriceDao");
    }

    

}
