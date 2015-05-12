package com.fftw.tsdb.unit.service;

import java.sql.SQLException;

import org.dbunit.DatabaseUnitException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fftw.tsdb.domain.Ticker;
import com.fftw.tsdb.service.AttributeValueService;
import com.fftw.tsdb.service.AttributeValueServiceImpl;
import com.fftw.tsdb.unit.dbunit.BaseUnitTest;

public class AttributeValueServiceImplUnitTest extends BaseUnitTest
{
    private AttributeValueService attributeValueService;
    
    @Override
    @BeforeClass(groups = {"unittest"})
    public void setUp() throws DatabaseUnitException, SQLException, Exception {
        super.setUp();
        attributeValueService = new AttributeValueServiceImpl();
    }
    
    @Test(groups =
    {
        "unittest"
    })
    public void testCreateTicker() {
        System.out.println("-----TestCreateTicker() called---------");
        Ticker ticker = attributeValueService.createOrGetTicker("ibm", "Internation Business Machines");
        Ticker retrievedTicker = attributeValueService.getTickerDao().findByID(ticker.getId());
        assert retrievedTicker.getName() == "ibm";
        
    }
}
