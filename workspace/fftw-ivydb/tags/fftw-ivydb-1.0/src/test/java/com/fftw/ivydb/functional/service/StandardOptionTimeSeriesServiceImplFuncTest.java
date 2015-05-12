package com.fftw.ivydb.functional.service;

import java.io.IOException;
import java.sql.Date;
import java.util.Calendar;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fftw.ivydb.domain.StandardOptionPrice;
import com.fftw.ivydb.domain.StandardOptionPricePK;
import com.fftw.ivydb.service.StandardOptionPriceService;
import com.fftw.ivydb.service.StandardOptionPriceServiceImpl;
import com.fftw.ivydb.service.StandardOptionTimeSeriesService;
import com.fftw.ivydb.service.StandardOptionTimeSeriesServiceImpl;

public class StandardOptionTimeSeriesServiceImplFuncTest
{
    private StandardOptionPriceService standardOptionPriceService;
    private StandardOptionTimeSeriesService standardOptionTimeSeriesService;
    
    @BeforeClass(groups =
    {
        "functest"
    })
    public void setUp () {
        standardOptionPriceService = new StandardOptionPriceServiceImpl();
        standardOptionTimeSeriesService = new StandardOptionTimeSeriesServiceImpl();
        standardOptionTimeSeriesService.setUp();
    }

    @Test(groups =
    {
        "functest"
    })
    public void testLoadAndCreateTimeSeries () throws IOException
    {
        /*Calendar startTime = Calendar.getInstance();
        System.out.println("-----TestLoadAndCreateTimeSeries() called---------");
        System.out.println("Start time: " + startTime.getTime());
        Calendar calendar = Calendar.getInstance();
        calendar.set(1996, Calendar.JANUARY, 4);
        System.out.println(calendar.getTime());
        StandardOptionPricePK id = new StandardOptionPricePK();
        id.setDate(new Date(calendar.getTimeInMillis()));
        id.setSecurityId(new Long(109117));
        id.setDays(new Double(91));
        id.setCallPutFlag("C");
        StandardOptionPrice data = this.standardOptionPriceService.findByID(id);
        this.standardOptionTimeSeriesService.createTimeSeriesDatas(data);
//        this.standardOptionTimeSeriesService.bulkInsert();
        Calendar endTime = Calendar.getInstance();
        System.out.println("End time: " + endTime.getTime());*/
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(1996, Calendar.JANUARY, 4);
        Calendar startTime = Calendar.getInstance();
        System.out.println("-----TestLoadAndCreateTimeSeries() called---------");
        System.out.println("Start time: " + startTime.getTime());
        System.out.println("Doing date: " + calendar.getTime());
        List<StandardOptionPrice> datas = this.standardOptionPriceService.findByDate(new Date(calendar.getTimeInMillis()));
        for (StandardOptionPrice standardOptionPrice : datas)
        {
            this.standardOptionTimeSeriesService.createTimeSeriesDatas(standardOptionPrice);
        }
        this.standardOptionTimeSeriesService.bulkInsert();
        Calendar endTime = Calendar.getInstance();
        System.out.println("End time: " + endTime.getTime());
    }
}
