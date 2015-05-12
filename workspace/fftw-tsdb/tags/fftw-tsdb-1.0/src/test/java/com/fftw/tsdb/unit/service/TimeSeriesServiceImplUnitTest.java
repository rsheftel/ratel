package com.fftw.tsdb.unit.service;

import java.sql.Date;
import java.sql.SQLException;
import java.util.Calendar;

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
            new Double(100), new Date(cal.getTimeInMillis()));

        TimeSeriesData timeSeriesData = this.timeSeriesService.getDataByDataSource(timeSeries,
            "jp morgan").get(0);
        assert timeSeriesData.getObservationValue().doubleValue() == new Double(100).doubleValue();
    }

    // TODO test createUpdateTimeSeriesData for the NoResultException, and also
    // see if dbunit will load new data for every test
    // or reuses the data
}
