package com.fftw.tsdb.functional.service.cds;

import java.sql.Date;
import java.util.Calendar;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fftw.tsdb.domain.cds.CreditRating;
import com.fftw.tsdb.domain.cds.MarkitCompositeHist;
import com.fftw.tsdb.domain.cds.MarkitCompositeHistPK;
import com.fftw.tsdb.service.cds.MarkitCompositeHistService;
import com.fftw.tsdb.service.cds.MarkitCompositeHistServiceImpl;

public class MarkitCompositeHistServiceFuncTest
{
    private MarkitCompositeHistService markitCompositeHistService;

    @BeforeClass(groups =
    {
        "functest"
    })
    public void setUp ()
    {
        markitCompositeHistService = new MarkitCompositeHistServiceImpl();
    }

    @Test(groups =
    {
        "functest"
    })
    public void testFindByID ()
    {
        System.out.println("-----TestFindByID() called---------");
        Calendar calendar = Calendar.getInstance();
        calendar.set(2007, 5, 25);
        MarkitCompositeHistPK markitCompositeHistPK = new MarkitCompositeHistPK();
        markitCompositeHistPK.setCcy("EUR");
        System.out.println("date is: " + calendar.getTime());
        markitCompositeHistPK.setDate(new Date(calendar.getTimeInMillis()));
        markitCompositeHistPK.setDocClause("CR");
        markitCompositeHistPK.setTicker("AAGM-AGD");
        markitCompositeHistPK.setTier("SNRFOR");
        MarkitCompositeHist markitCompositeHist = markitCompositeHistService
            .findByID(markitCompositeHistPK);
        assert markitCompositeHist.getAvRating() == null;
        assert markitCompositeHist.getSpread1y().doubleValue() == 0.00126583333333333;
    }
    
    @Test(groups =
    {
        "functest"
    })
    public void testGetCreditRating ()
    {
        System.out.println("-----TestGetCreditRating() called---------");
        Calendar calendar = Calendar.getInstance();
        calendar.set(2007, 5, 25);
        MarkitCompositeHistPK markitCompositeHistPK = new MarkitCompositeHistPK();
        markitCompositeHistPK.setCcy("AUD");
        markitCompositeHistPK.setDate(new Date(calendar.getTimeInMillis()));
        markitCompositeHistPK.setDocClause("MR");
        markitCompositeHistPK.setTicker("A");
        markitCompositeHistPK.setTier("SNRFOR");
        MarkitCompositeHist markitCompositeHist = markitCompositeHistService
            .findByID(markitCompositeHistPK);
        CreditRating creditRating = markitCompositeHistService.getCreditRating(markitCompositeHist.getAvRating());
        assert creditRating.getRatingValue().doubleValue() == 12;
    }

    @Test(groups =
    {
        "functest"
    })
    public void testFindByDate ()
    {
        System.out.println("-----TestFindByDate() called---------");
        Calendar calendar = Calendar.getInstance();
        calendar.set(2007, 5, 25);
        Date date = new Date(calendar.getTimeInMillis());
        List<MarkitCompositeHist> results = markitCompositeHistService.findByDate(date);
        System.out.println("total count: " + results.size());
    }
}
