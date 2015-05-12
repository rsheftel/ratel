package com.fftw.ivydb.dao;

import java.sql.Date;
import java.util.List;

import com.fftw.ivydb.domain.StandardOptionPrice;
import com.fftw.ivydb.domain.StandardOptionPricePK;

public interface StandardOptionPriceDao extends
    GenericDao<StandardOptionPrice, StandardOptionPricePK>
{
    List<StandardOptionPrice> findByDate(Date date);
}
