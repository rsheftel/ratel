package com.fftw.tsdb.service;

import com.fftw.tsdb.dao.GenericDao;
import com.fftw.tsdb.domain.Attribute;
import com.fftw.tsdb.factory.AbstractDaoFactory;

public class AttributeServiceImpl implements AttributeService
{
    private GenericDao<Attribute, Long> attributeDao;
    
    
    public AttributeServiceImpl ()
    {
        AbstractDaoFactory daoFactory = AbstractDaoFactory.instance(AbstractDaoFactory.JPA);
        attributeDao = daoFactory.getAttributeDao();
    }


    public Attribute findByName (String name)
    {
        name = name.toLowerCase();
        return attributeDao.findByName(name);
    }

}
