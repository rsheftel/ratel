package com.fftw.tsdb.service.cds;

import java.sql.Date;
import java.util.List;

import com.fftw.tsdb.domain.cds.CreditRating;
import com.fftw.tsdb.domain.cds.MarkitCompositeHist;
import com.fftw.tsdb.domain.cds.MarkitCompositeHistPK;

public interface MarkitCompositeHistService
{
    /**
     * Find a MarkitCompositeHist by id
     * @param id
     * @return
     */
    MarkitCompositeHist findByID (MarkitCompositeHistPK id);

    /**
     * Find all MarkitCompositeHist by date
     * @param date
     * @return
     */
    List<MarkitCompositeHist> findByDate (Date date);

    /**
     * Get the credit rating value based on the avRating 
     * @param snpRating
     * @return
     */
    CreditRating getCreditRating (String snpRating);
}
