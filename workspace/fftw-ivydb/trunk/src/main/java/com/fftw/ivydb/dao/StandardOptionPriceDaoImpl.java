package com.fftw.ivydb.dao;

import java.sql.Date;
import java.util.List;

import com.fftw.ivydb.domain.StandardOptionPrice;
import com.fftw.ivydb.domain.StandardOptionPricePK;

public class StandardOptionPriceDaoImpl extends
    GenericDaoImpl<StandardOptionPrice, StandardOptionPricePK> implements StandardOptionPriceDao
{

    public StandardOptionPriceDaoImpl (Class<StandardOptionPrice> entityClass)
    {
        super(entityClass);
    }

    public List<StandardOptionPrice> findByDate (Date date)
    {
        return (List<StandardOptionPrice>)em.createNamedQuery(
            "StandardOptionPrice.getStandardOptionPriceByDate").setParameter("date", date)
            .getResultList();
    }

}
