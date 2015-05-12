package com.fftw.tsdb.dao.cds;

import java.sql.Date;
import java.util.List;

import com.fftw.tsdb.dao.GenericDaoImpl;
import com.fftw.tsdb.domain.cds.CreditRating;
import com.fftw.tsdb.domain.cds.MarkitCompositeHist;
import com.fftw.tsdb.domain.cds.MarkitCompositeHistPK;

public class MarkitCompositeHistDaoImpl extends GenericDaoImpl<MarkitCompositeHist, MarkitCompositeHistPK> implements MarkitCompositeHistDao
{

    public MarkitCompositeHistDaoImpl (Class<MarkitCompositeHist> entityClass)
    {
        super(entityClass);
    }

    public List<MarkitCompositeHist> findByDate (Date date)
    {
        return (List<MarkitCompositeHist>)em.createNamedQuery("MarkitCompositeHist.getCdsHistByDate").setParameter("date", date).getResultList();
    }

    public CreditRating getCreditRating (String snpRating)
    {
        return (CreditRating)em.createNamedQuery("CreditRating.getCreditRating").setParameter("name", snpRating).getSingleResult();
    }

    
    

}
