package com.fftw.ivydb.service;

import java.sql.Date;
import java.util.List;

import com.fftw.ivydb.domain.SecurityPrice;
import com.fftw.ivydb.domain.SecurityPricePK;

public interface SecurityPriceService
{
    /**
     * Find a SecurityPrice by id
     * @param id
     * @return
     */
    SecurityPrice findByID(SecurityPricePK id);
    
    /**
     * Find all SecurityPrice by date
     * @param date
     * @return
     */
    List<SecurityPrice> findByDate(Date date);
}
