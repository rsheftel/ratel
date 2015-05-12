package com.fftw.tsdb.unit.service;

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
import com.fftw.tsdb.service.TimeSeriesService;
import com.fftw.tsdb.service.TimeSeriesServiceImpl;
import com.fftw.tsdb.service.futures.FuturesTimeSeriesService;
import com.fftw.tsdb.service.futures.FuturesTimeSeriesServiceImpl;
import com.fftw.tsdb.unit.dbunit.BaseUnitTest;

//TODO Maybe should just refactor this to FuturesTimeSeriesImplUnitTest, don't really need the TimeSeriesService to access methods
public class TimeSeriesServiceImplUnitTest extends BaseUnitTest
{
    private TimeSeriesService timeSeriesService;
    private FuturesTimeSeriesService futuresTimeSeriesService;

    @Override
    @BeforeClass(groups =
    {
        "unittest"
    })
    protected void setUp () throws DatabaseUnitException, SQLException, Exception
    {
        super.setUp();
        this.timeSeriesService = new TimeSeriesServiceImpl();
        this.futuresTimeSeriesService = new FuturesTimeSeriesServiceImpl();
        DatabaseOperation.CLEAN_INSERT.execute(getConnection(), getDataSet());
    }

    @Test(groups =
    {
        "unittest"
    })
    public void testCreateTimeSeriesByName () throws DatabaseUnitException, SQLException, Exception
    {
        System.out.println("-----TestCreateTimeSeriesByName() called---------");
        // System.out.println("-----------time is: " + cal.getTimeInMillis());
        TimeSeries timeSeries = this.futuresTimeSeriesService.createOrGetFuturesOptionsTimeSeries("straddle",
            "underlying_price", "edm07", "91.629");
        TimeSeries timeSeriesRetrieved = this.timeSeriesService
            .findByName("edm07 straddle 91.629 underlying_price");
        assert timeSeriesRetrieved.getName().equals(timeSeries.getName());
    }

    @Test(groups =
    {
        "unittest"
    }, dependsOnMethods =
    {
        "testCreateTimeSeriesByName"
    })
    public void testCreateUpdateTimeSeriesData ()
    {
        System.out.println("-----TestCreateUpdateTimeSeriesData() called---------");
        Calendar cal = Calendar.getInstance();
        TimeSeries timeSeries = this.timeSeriesService
            .findByName("edm07 straddle 91.629 underlying_price");
        this.timeSeriesService.createOrUpdateTimeSeriesData(timeSeries, "jp morgan",
            new Double(100), cal);

        TimeSeriesData timeSeriesData = this.timeSeriesService.findDataByDataSource(timeSeries,
            "jp morgan").get(0);
        assert timeSeriesData.getObservationValue().doubleValue() == new Double(100).doubleValue();
    }
    
    @Test(groups = {"unittest"})
    public void testFindDataByDateRange() {
        System.out.println("-----TestFindDataByDateRange() called---------");
        Calendar calendar = Calendar.getInstance();
        calendar.set(2007, Calendar.JUNE, 14);
        java.util.Date startDate = calendar.getTime();
        calendar.set(2007, Calendar.JUNE, 20);
        java.util.Date endDate = calendar.getTime();
        String timeSeriesName = "a_snrfor_aud_mr_composite_depth_5y";
        String dataSourceName = "markit";
        List<TimeSeriesData> datas = timeSeriesService.findDataByDateRange(timeSeriesName, dataSourceName, startDate, endDate);
        assert datas.size() == 3;
        Double firstDataValue = datas.get(0).getObservationValue();
        assert firstDataValue.doubleValue() == 1.0;
        Double secondDataValue = datas.get(1).getObservationValue();
        assert secondDataValue.doubleValue() == 2.0;
        Double thirdDataValue = datas.get(2).getObservationValue();
        assert thirdDataValue.doubleValue() == 3.0;
    }
    // TODO test createUpdateTimeSeriesData for the NoResultException, and also
    // see if dbunit will load new data for every test
    // or reuses the data
}
