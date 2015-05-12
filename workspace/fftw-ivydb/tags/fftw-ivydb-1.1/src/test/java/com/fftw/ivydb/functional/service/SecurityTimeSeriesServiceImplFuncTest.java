package com.fftw.ivydb.functional.service;

import java.io.IOException;
import java.sql.Date;
import java.util.Calendar;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fftw.ivydb.domain.SecurityPrice;
import com.fftw.ivydb.domain.SecurityPricePK;
import com.fftw.ivydb.service.SecurityPriceService;
import com.fftw.ivydb.service.SecurityPriceServiceImpl;
import com.fftw.ivydb.service.SecurityTimeSeriesService;
import com.fftw.ivydb.service.SecurityTimeSeriesServiceImpl;

public class SecurityTimeSeriesServiceImplFuncTest
{
    private SecurityPriceService securityPriceService;
    private SecurityTimeSeriesService securityTimeSeriesService;
    

    @BeforeClass(groups =
    {
        "functionalTest"
    })
    public void setUp () {
        securityPriceService = new SecurityPriceServiceImpl();
        securityTimeSeriesService = new SecurityTimeSeriesServiceImpl();
        securityTimeSeriesService.setUp();
    }
    
    @Test(groups =
    {
        "functionalTest"
    })
    public void testLoadAndCreateTimeSeries () throws IOException
    {
        /*Calendar startTime = Calendar.getInstance();
        System.out.println("-----TestLoadAndCreateTimeSeries() called---------");
        System.out.println("Start time: " + startTime.getTime());
        Calendar calendar = Calendar.getInstance();
        calendar.set(1996, Calendar.JANUARY, 4);
        System.out.println(calendar.getTime());
        SecurityPricePK id = new SecurityPricePK();
        id.setDate(new Date(calendar.getTimeInMillis()));
        id.setSecurityId(new Long(5317));
        SecurityPrice data = this.securityPriceService.findByID(id);
        this.securityTimeSeriesService.createTimeSeriesDatas(data);
//        this.securityTimeSeriesService.bulkInsert();
        Calendar endTime = Calendar.getInstance();
        System.out.println("End time: " + endTime.getTime());*/
        
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(1996, Calendar.JANUARY, 4);
        Calendar startTime = Calendar.getInstance();
        System.out.println("-----TestLoadAndCreateTimeSeries() called---------");
        System.out.println("Start time: " + startTime.getTime());
        System.out.println("Doing date: " + calendar.getTime());
        List<SecurityPrice> datas = this.securityPriceService.findByDate(new Date(calendar.getTimeInMillis()));
        for (SecurityPrice securityPrice : datas)
        {
            this.securityTimeSeriesService.createTimeSeriesDatas(securityPrice);
        }
        this.securityTimeSeriesService.bulkInsert();
        Calendar endTime = Calendar.getInstance();
        System.out.println("End time: " + endTime.getTime());
        
    }
    
}
