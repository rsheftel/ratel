package com.fftw.ivydb.factory;

import java.lang.reflect.Constructor;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.fftw.ivydb.dao.SecurityPriceDao;
import com.fftw.ivydb.dao.StandardOptionPriceDao;

public abstract class AbstractDaoFactory
{
    public static final Class<JpaDaoFactory> JPA = com.fftw.ivydb.factory.JpaDaoFactory.class;

    private static ApplicationContext context;

    static
    {
        context = new ClassPathXmlApplicationContext(new String[]
        {
            "classpath:IvyDB-Jpa-ApplicationContext.xml"
        });
    }

    public static AbstractDaoFactory instance (Class factory)
    {
        try
        {
            Constructor constructor = factory.getDeclaredConstructor(ApplicationContext.class);
            return (AbstractDaoFactory)constructor.newInstance(context);
        }
        catch (SecurityException e)
        {
            throw new RuntimeException("Couldn't create DaoFactory " + factory + ": " + e.getMessage());
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException("Couldn't create DaoFactory " + factory + ": " + e.getMessage());
        }
        catch (Exception e)
        {
            throw new RuntimeException("Couldn't create DaoFactory " + factory + ": " + e.getMessage());
        }
    }

    public abstract SecurityPriceDao getSecurityPriceDao ();

    public abstract StandardOptionPriceDao getStandardOptionPriceDao ();

}
