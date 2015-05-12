package com.fftw.ivydb.dao;

import java.sql.Date;
import java.util.List;

import com.fftw.ivydb.domain.SecurityPrice;
import com.fftw.ivydb.domain.SecurityPricePK;

public interface SecurityPriceDao extends GenericDao<SecurityPrice, SecurityPricePK>
{
    List<SecurityPrice> findByDate(Date date);
}
