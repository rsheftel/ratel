package com.fftw.db.extract;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

public class ExtractDataFromDB
{

    /**
     * @param args
     */
    public static void main (String[] args) throws Exception
    {
        // Start Spring and load the embedded broker
        BeanFactory factory = getBeanFactory();
        DBUnitDataExtractor extractor = (DBUnitDataExtractor)factory.getBean("dbUnitDataExtractor");
        
        extractor.extract();

    }

    private static BeanFactory getBeanFactory ()
    {
        XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource("com/fftw/db/extract/extractDbContext.xml"));

        return factory;
    }
}
