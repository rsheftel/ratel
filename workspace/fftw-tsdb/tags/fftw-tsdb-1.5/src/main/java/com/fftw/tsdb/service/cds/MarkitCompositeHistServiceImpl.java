package com.fftw.tsdb.service.cds;

import java.sql.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fftw.tsdb.dao.cds.MarkitCompositeHistDao;
import com.fftw.tsdb.domain.cds.CreditRating;
import com.fftw.tsdb.domain.cds.MarkitCompositeHist;
import com.fftw.tsdb.domain.cds.MarkitCompositeHistPK;
import com.fftw.tsdb.factory.AbstractDaoFactory;

public class MarkitCompositeHistServiceImpl implements MarkitCompositeHistService
{
    private static final Log logger = LogFactory.getLog(MarkitCompositeHistServiceImpl.class);
    
    private MarkitCompositeHistDao markitCompositeHistDao;
    
    public MarkitCompositeHistServiceImpl ()
    {
        AbstractDaoFactory daoFactory = AbstractDaoFactory.instance(AbstractDaoFactory.JPA);
        markitCompositeHistDao = daoFactory.getMarkitCompositeHistDao();
    }

    public MarkitCompositeHist findByID (MarkitCompositeHistPK id)
    {
        return markitCompositeHistDao.findByID(id);
    }
    
    public List<MarkitCompositeHist> findByDate(Date date){
        logger.info("findByDate called with date " + date);
        return markitCompositeHistDao.findByDate(date);
    }

    public CreditRating getCreditRating (String snpRating)
    {
        return markitCompositeHistDao.getCreditRating(snpRating);
    }

    
}
