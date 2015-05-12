package com.fftw.tsdb.dao.cds;

import java.sql.Date;
import java.util.List;

import com.fftw.tsdb.dao.GenericDao;
import com.fftw.tsdb.domain.cds.CreditRating;
import com.fftw.tsdb.domain.cds.MarkitCompositeHist;
import com.fftw.tsdb.domain.cds.MarkitCompositeHistPK;

public interface MarkitCompositeHistDao extends GenericDao<MarkitCompositeHist, MarkitCompositeHistPK>
{
    List<MarkitCompositeHist> findByDate(Date date);
    CreditRating getCreditRating(String snpRating); 
    //TODO  method to join with credit_rating to get snp rating
}
