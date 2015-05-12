package com.fftw.tsdb.factory;

import org.springframework.context.ApplicationContext;

import com.fftw.tsdb.dao.JdbcTimeSeriesDao;

public class JdbcDaoFactory extends AbstractDaoFactory
{
    private ApplicationContext context;

    public JdbcDaoFactory (ApplicationContext context)
    {
        this.context = context;
        /*this.context = new ClassPathXmlApplicationContext(new String[]
        {
            "classpath:Jdbc-ApplicationContext.xml"
        });*/
    }

    @Override
    public JdbcTimeSeriesDao getJdbcTimeSeriesDao ()
    {
        return (JdbcTimeSeriesDao)context.getBean("JdbcTimeSeriesDao");
    }
    
    

}
