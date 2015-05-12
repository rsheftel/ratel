package com.fftw.positions;


import static org.testng.Assert.*;


import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.fftw.bloomberg.types.BBFuturesCategory;
import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.bloomberg.types.BBSecurityIDFlag;
import com.fftw.bloomberg.types.BBSecurityType;
import com.fftw.util.AbstractBaseTest;

public class SecurityTest extends AbstractBaseTest {

    @Test(groups = { "unittest" })
    public void testDefaultSecurityCopyWithOverRide() {
        ISecurity security =  new DefaultSecurity("Test Security", BBProductCode.Equity, "ZVZZT",
            BBSecurityIDFlag.Equity, BBSecurityType.CommonStock, "ZVZZT");
        
        Map<String, Object> overRideValues = new HashMap<String, Object>();
        
        overRideValues.put("securityId", "ZWZZT");
        overRideValues.put("name", "Test Security Too");
        overRideValues.put("ticker", "ZWZZT");
        
        ISecurity copiedSecurity = security.copy(overRideValues);
        assertTrue(copiedSecurity instanceof DefaultSecurity);
        assertNotNull(copiedSecurity);
        assertEquals(copiedSecurity.getName(), "Test Security Too");
        assertEquals(copiedSecurity.getSecurityId(), "ZWZZT");
        assertEquals(copiedSecurity.getTicker(), "ZWZZT");
    }

    @Test(groups = { "unittest" })
    public void testEquitySecurityCopyWithOverRide() {
        ISecurity security =  new Equity("Test Security", BBProductCode.Equity, "ZVZZT",
            BBSecurityIDFlag.Equity, BBSecurityType.CommonStock, "ZVZZT");
        
        Map<String, Object> overRideValues = new HashMap<String, Object>();
        
        overRideValues.put("securityId", "ZWZZT");
        overRideValues.put("name", "Test Security Too");
        overRideValues.put("ticker", "ZWZZT");
        
        ISecurity copiedSecurity = security.copy(overRideValues);
        
        assertTrue(copiedSecurity instanceof Equity);
        assertNotNull(copiedSecurity);
        assertEquals(copiedSecurity.getName(), "Test Security Too");
        assertEquals(copiedSecurity.getSecurityId(), "ZWZZT");
        assertEquals(copiedSecurity.getTicker(), "ZWZZT");
    }

    @Test(groups = { "unittest" })
    public void testDefaultFuturesSecurityCopyWithOverRide() {
        ISecurity security =  new DefaultFuturesSecurity("WTI CRUDE FUTURE  Feb09", BBProductCode.Commodity, "CLG9",
            BBSecurityIDFlag.BBID, BBSecurityType.Futures, "CLG9", BBFuturesCategory.Energies);
        
        Map<String, Object> overRideValues = new HashMap<String, Object>();
        
        overRideValues.put("securityId", "ZWZZT");
        overRideValues.put("name", "Test Security Too");
        overRideValues.put("ticker", "ZWZZT");
        
        ISecurity copiedSecurity = security.copy(overRideValues);
        
        assertTrue(copiedSecurity instanceof DefaultFuturesSecurity);
        assertNotNull(copiedSecurity);
        assertEquals(copiedSecurity.getName(), "Test Security Too");
        assertEquals(copiedSecurity.getSecurityId(), "ZWZZT");
        assertEquals(copiedSecurity.getTicker(), "ZWZZT");
    }

}
