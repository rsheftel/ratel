package com.fftw.tsdb.unit.service.irs;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.dbunit.DatabaseUnitException;
import org.dbunit.operation.DatabaseOperation;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fftw.tsdb.domain.TimeSeries;
import com.fftw.tsdb.domain.Attribute;
import com.fftw.tsdb.service.irs.IrsTimeSeriesService;
import com.fftw.tsdb.service.AttributeService;
import com.fftw.tsdb.service.AttributeServiceImpl;
import com.fftw.tsdb.service.AttributeValueService;
import com.fftw.tsdb.service.AttributeValueServiceImpl;
import com.fftw.tsdb.service.irs.IrsTimeSeriesServiceImpl;
import com.fftw.tsdb.unit.dbunit.BaseUnitTest;

public class IrsTimeSeriesServiceImplUnitTest extends BaseUnitTest
{
    private IrsTimeSeriesService irsTimeSeriesService;
    private AttributeService attributeService;
    private AttributeValueService attributeValueService;
    
    //TODO re-do this test and re-do the service implementation

    /*@Override
    @BeforeClass(groups =
    {
        "unittest"
    })
    protected void setUp () throws DatabaseUnitException, SQLException, Exception
    {
        super.setUp();
        this.irsTimeSeriesService = new IrsTimeSeriesServiceImpl();
        this.attributeService = new AttributeServiceImpl();
        this.attributeValueService = new AttributeValueServiceImpl();
        DatabaseOperation.CLEAN_INSERT.execute(getConnection(), getDataSet());
    }

    @Test(groups =
    {
        "unittest"
    })
    public void testLoadAndCreateTimeSeries ()
    {
        System.out.println("-----TestLoadAndCreateTimeSeries() called---------");
        
    	String ccy = "usd";
    	String quoteSide = "mid";
    	String quoteConvention = "rate";
    	String[] tenors = { "18m", "1y", "2y", "3y", "4y", "5y", "6y", "7y", "8y", "9y", "10y", "12y", "15y", "20y", "25y", "30y", "40y" };
    	
    	Iterator i = Arrays.asList(tenors).iterator();
    	while(i.hasNext()) {
    		String tenor = (String) i.next();
    		TimeSeries nextTimeSeries = this.irsTimeSeriesService.createOrGetIrsTimeSeries(ccy, quoteSide, quoteConvention, tenor);
    		Map<Attribute, Long> attributeMap  = irsTimeSeriesService.getAllAttributeMaps(nextTimeSeries);
    		checkAttribute(attributeMap, "ccy", "usd");
            checkAttribute(attributeMap, "quote_side", "mid");
            checkAttribute(attributeMap, "quote_convention", "rate");
            checkAttribute(attributeMap, "quote_type", "close");
            checkAttribute(attributeMap, "tenor", tenor);
            checkAttribute(attributeMap, "instrument", "irs");
    	}
        
        TimeSeries timeSeries = this.irsTimeSeriesService.findByName("irs_usd_rate_15y_mid");
        checkAttribute(irsTimeSeriesService.getAllAttributeMaps(timeSeries), "tenor", "15y");
    }

    private void checkAttribute (Map<Attribute, Long> attributeMap, String attributeName, String valueName)
    {
        assert attributeMap.get(attributeService.findByName(attributeName)).equals(attributeValueService.createOrGetGeneralAttributeValue(valueName).getId());
    }*/
    
    

}
