package com.fftw.tsdb.functional.service.cds;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fftw.tsdb.domain.cds.MarkitCompositeHist;
import com.fftw.tsdb.domain.cds.MarkitCompositeHistPK;
import com.fftw.tsdb.service.arbitration.ArbitrationServiceImpl;
import com.fftw.tsdb.service.cds.CdsTimeSeriesService;
import com.fftw.tsdb.service.cds.CdsTimeSeriesServiceImpl;
import com.fftw.tsdb.service.cds.MarkitCompositeHistService;
import com.fftw.tsdb.service.cds.MarkitCompositeHistServiceImpl;

public class CdsTimeSeriesServiceImplFuncTest
{
    private MarkitCompositeHistService markitCompositeHistService;

    private CdsTimeSeriesService cdsTimeSeriesService;
    
    private ArbitrationServiceImpl arbitrationService;

    @BeforeClass(groups =
    {
        "functionalTest"
    })
    public void setUp ()
    {
        markitCompositeHistService = new MarkitCompositeHistServiceImpl();
        cdsTimeSeriesService = new CdsTimeSeriesServiceImpl();
        cdsTimeSeriesService.setUp();
        arbitrationService = new ArbitrationServiceImpl();
    }

    @Test(groups =
    {
        "functionalTest"
    })
    public void testLoadAndCreateTimeSeries () throws IOException
    {
        Calendar startTime = Calendar.getInstance();
        System.out.println("-----TestLoadAndCreateTimeSeries() called---------");
        System.out.println("Start time: " + startTime.getTime());
        Calendar calendar = Calendar.getInstance();
        calendar.set(2007, Calendar.JULY, 18);
        System.out.println(calendar.getTime());
        String ccy = "AUD";
        String docClause = "MR";
        String ticker = "ABY";
        String tier = "SNRFOR";
        MarkitCompositeHistPK id = new MarkitCompositeHistPK();
        id.setDate(new Date(calendar.getTimeInMillis()));
        id.setCcy(ccy);
        id.setDocClause(docClause);
        id.setTicker(ticker);
        id.setTier(tier);
        MarkitCompositeHist data = this.markitCompositeHistService.findByID(id);
        this.cdsTimeSeriesService.createTimeSeriesDatas(data);
//        this.cdsTimeSeriesService.bulkInsert();
        
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy"); 
        Calendar calArbitration = Calendar.getInstance();
        try
        {
            calArbitration.setTime(df.parse("1-Oct-2007"));
        }
        catch (Exception e) {}
        this.arbitrationService.cdsArbitration(calArbitration);

        Calendar endTime = Calendar.getInstance();
        System.out.println("End time: " + endTime.getTime());
        try
        {
            java.util.Date d1 = df.parse("28-oct-2007");
            endTime.setTime(d1);            
            System.out.println(com.fftw.util.Util.addWeekDay(endTime, -18).getTime());
            System.out.println(endTime.getTime());
        }
        catch (Exception e) {}
        
        /*Calendar calendar = Calendar.getInstance();
        calendar.set(2007, Calendar.AUGUST, 10);
//        calendar.set(2007, Calendar.JULY, 18);
        Calendar startTime = Calendar.getInstance();
        System.out.println("-----TestLoadAndCreateTimeSeries() called---------");
        System.out.println("Start time: " + startTime.getTime());
        System.out.println("Doing date: " + calendar.getTime());
        List<MarkitCompositeHist> cdsDatas = this.markitCompositeHistService
            .findByDate(new Date(calendar.getTimeInMillis()));
        for (MarkitCompositeHist markitCompositeHist : cdsDatas)
        {
            // Insert 14 different time series data points
            this.cdsTimeSeriesService.createTimeSeriesDatas(markitCompositeHist);
        }
        this.cdsTimeSeriesService.bulkInsert();
        Calendar endTime = Calendar.getInstance();
        System.out.println("End time: " + endTime.getTime());*/
        
        /*this.cdsTimeSeriesService.setBcpFile(new File("C:\\workspace\\bcp.1187286962861"));
        this.cdsTimeSeriesService.bulkInsert();*/
    }
}
