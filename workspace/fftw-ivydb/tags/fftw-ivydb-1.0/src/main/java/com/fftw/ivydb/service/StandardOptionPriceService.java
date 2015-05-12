package com.fftw.ivydb.service;

import java.sql.Date;
import java.util.List;

import com.fftw.ivydb.domain.StandardOptionPrice;
import com.fftw.ivydb.domain.StandardOptionPricePK;

public interface StandardOptionPriceService
{
    /**
     * Find a StandardOptionPrice by id
     * @param id
     * @return
     */
    StandardOptionPrice findByID (StandardOptionPricePK id);

    /**
     * Find all StandardOptionPrice by date
     * @param date
     * @return
     */
    List<StandardOptionPrice> findByDate (Date date);
}
