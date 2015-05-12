package com.fftw.tsdb.functional.service.cds;

import java.sql.Date;
import java.util.Calendar;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fftw.tsdb.domain.cds.MarkitCompositeHist;
import com.fftw.tsdb.domain.cds.MarkitCompositeHistPK;
import com.fftw.tsdb.service.cds.CdsTimeSeriesService;
import com.fftw.tsdb.service.cds.CdsTimeSeriesServiceImpl;
import com.fftw.tsdb.service.cds.MarkitCompositeHistService;
import com.fftw.tsdb.service.cds.MarkitCompositeHistServiceImpl;

public class CdsTimeSeriesServiceImplFuncTest
{
    private MarkitCompositeHistService markitCompositeHistService;

    private CdsTimeSeriesService cdsTimeSeriesService;

    @BeforeClass(groups =
    {
        "functest"
    })
    public void setUp ()
    {
        markitCompositeHistService = new MarkitCompositeHistServiceImpl();
        cdsTimeSeriesService = new CdsTimeSeriesServiceImpl();
        cdsTimeSeriesService.setUp();
    }

    @Test(groups =
    {
        "functest"
    })
    public void testLoadAndCreateTimeSeries ()
    {
        Calendar startTime = Calendar.getInstance();
        System.out.println("-----TestLoadAndCreateTimeSeries() called---------");
        System.out.println("Start time: " + startTime.getTime());
        Calendar calendar = Calendar.getInstance();
        calendar.set(2007, 6, 18);
        Date date = new Date(calendar.getTimeInMillis());
        String ccy = "AUD";
        String docClause = "MR";
        String ticker = "ABY";
        String tier = "SNRFOR";
        MarkitCompositeHistPK id = new MarkitCompositeHistPK();
        id.setDate(date);
        id.setCcy(ccy);
        id.setDocClause(docClause);
        id.setTicker(ticker);
        id.setTier(tier);
        MarkitCompositeHist data = this.markitCompositeHistService.findByID(id);
        this.cdsTimeSeriesService.createTimeSeriesDatas(data);
        Calendar endTime = Calendar.getInstance();
        System.out.println("End time: " + endTime.getTime());
        
        /*int count = 22;
        Calendar calendar = Calendar.getInstance();
        calendar.set(2007, 6, 1);
        for (int i = 0; i < count; i++)
        {
            Calendar startTime = Calendar.getInstance();
            System.out.println("-----TestLoadAndCreateTimeSeries() called---------");
            System.out.println("Start time: " + startTime.getTime());
            calendar.add(Calendar.DATE, 1);
            System.out.println("Doing date: " + calendar.getTime());
            List<MarkitCompositeHist> cdsDatas = this.markitCompositeHistService
                .findByDate(new Date(calendar.getTimeInMillis()));
            for (MarkitCompositeHist markitCompositeHist : cdsDatas)
            {
                // Insert 14 different time series data points
                this.cdsTimeSeriesService.createTimeSeriesDatas(markitCompositeHist);
            }
            Calendar endTime = Calendar.getInstance();
            System.out.println("End time: " + endTime.getTime());
        }*/
    }
}
