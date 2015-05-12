package com.fftw.ivydb.dao;

import java.sql.Date;
import java.util.List;

import com.fftw.ivydb.domain.SecurityPrice;
import com.fftw.ivydb.domain.SecurityPricePK;

public class SecurityPriceDaoImpl extends GenericDaoImpl<SecurityPrice, SecurityPricePK> implements
    SecurityPriceDao
{

    public SecurityPriceDaoImpl (Class<SecurityPrice> entityClass)
    {
        super(entityClass);
    }

    public List<SecurityPrice> findByDate (Date date)
    {
        return (List<SecurityPrice>)em.createNamedQuery("SecurityPrice.getSecurityPriceByDate")
            .setParameter("date", date).getResultList();
    }

}
