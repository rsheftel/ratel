package com.fftw.tsdb.unit.service.cds;

import java.sql.Date;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import org.dbunit.DatabaseUnitException;
import org.dbunit.operation.DatabaseOperation;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fftw.tsdb.domain.TimeSeries;
import com.fftw.tsdb.domain.TimeSeriesData;
import com.fftw.tsdb.domain.cds.MarkitCompositeHist;
import com.fftw.tsdb.service.cds.CdsTimeSeriesService;
import com.fftw.tsdb.service.cds.CdsTimeSeriesServiceImpl;
import com.fftw.tsdb.service.cds.MarkitCompositeHistService;
import com.fftw.tsdb.service.cds.MarkitCompositeHistServiceImpl;
import com.fftw.tsdb.unit.dbunit.BaseUnitTest;

public class CdsTimeSeriesServiceImplUnitTest extends BaseUnitTest
{
    private MarkitCompositeHistService markitCompositeHistService;

    private CdsTimeSeriesService cdsTimeSeriesService;

    @Override
    @BeforeClass(groups =
    {
        "unittest"
    })
    protected void setUp () throws DatabaseUnitException, SQLException, Exception
    {
        super.setUp();
        this.markitCompositeHistService = new MarkitCompositeHistServiceImpl();
        this.cdsTimeSeriesService = new CdsTimeSeriesServiceImpl();
        DatabaseOperation.CLEAN_INSERT.execute(getConnection(), getDataSet());
        this.cdsTimeSeriesService.setUp();
    }

    @Test(groups =
    {
        "unittest"
    })
    public void testLoadAndCreateTimeSeries ()
    {
        System.out.println("-----TestLoadAndCreateTimeSeries() called---------");
        Calendar calendar = Calendar.getInstance();
        calendar.set(2007, 5, 25);
        List<MarkitCompositeHist> cdsDatas = this.markitCompositeHistService.findByDate(new Date(
            calendar.getTimeInMillis()));
        for (MarkitCompositeHist markitCompositeHist : cdsDatas)
        {
            //Insert 14 different time series data points
            this.cdsTimeSeriesService.createTimeSeriesDatas(markitCompositeHist);
        }
        TimeSeries timeSeries = this.cdsTimeSeriesService.findByName("a_snrfor_aud_mr_av_rating");
        List<TimeSeriesData> timeSeriesDatas = this.cdsTimeSeriesService.findDataByDataSource(timeSeries, "markit");
        TimeSeriesData timeSeriesData = timeSeriesDatas.get(0);
        assert timeSeriesData.getObservationValue().doubleValue() == 1.0;
    }
    
    @Test(groups =
    {
        "unittest"
    }, dependsOnMethods =
    {
        "testLoadAndCreateTimeSeries"
    })
    public void testUpdateTimeSeriesData() {
        System.out.println("-----TestUpdateTimeSeriesData() called---------");
        Calendar calendar = Calendar.getInstance();
        calendar.set(2007, 5, 25);
        TimeSeries timeSeries = this.cdsTimeSeriesService.findByName("a_snrfor_aud_mr_av_rating");
        this.cdsTimeSeriesService.createOrUpdateTimeSeriesData(timeSeries, "markit", new Double(100), new Date(calendar.getTimeInMillis()));
        List<TimeSeriesData> timeSeriesDatas = this.cdsTimeSeriesService.findDataByDataSource(timeSeries, "markit");
        TimeSeriesData timeSeriesData = timeSeriesDatas.get(0);
        assert timeSeriesData.getObservationValue().doubleValue() == 100.0;
    }

}
