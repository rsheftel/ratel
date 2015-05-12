package com.fftw.positions.cache;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.List;

import org.testng.annotations.Test;

import com.fftw.bloomberg.batch.messages.BatchPosition;
import com.fftw.bloomberg.batch.messages.BatchPositionTest;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePositionTest;
import com.fftw.bloomberg.types.BBSecurityIDFlag;
import com.fftw.bloomberg.types.BBSecurityType;
import com.fftw.positions.CommodityFutures;
import com.fftw.positions.CurrencyFutures;
import com.fftw.positions.DefaultFuturesSecurity;
import com.fftw.positions.ISecurity;
import com.fftw.positions.IndexFutures;
import com.fftw.positions.Position;
import com.fftw.util.AbstractBaseTest;

public class SecurityRootPositionAggregationStrategyTest extends AbstractBaseTest {

    @Test(groups = { "unittest" })
    public void testBatchFileDecember() {
        List<BatchPosition> batchPositions = BatchPositionTest.loadFromFile("/ActiveMQAf200-2008-12-09.txt");
        //List<BatchPosition> batchPositions = BatchPositionTest.loadFromFile("/BB200-2009-04-07.txt");
        
        SecurityRootPositionAggregationStrategy srpas = new SecurityRootPositionAggregationStrategy();

        for (BatchPosition batchPosition : batchPositions) {
            if (batchPosition.getSecurityId().equals("01F0526B3")) {
                System.err.println(batchPosition);
            }
            Position origPosition = batchPosition.getPosition();
            Position position = srpas.convertToAggregate(origPosition);

            assertEquals(position.getAccount(), origPosition.getAccount());
            assertEquals(position.getCurrentPosition(), origPosition.getCurrentPosition());
            assertEquals(position.getIntradayPosition(), origPosition.getIntradayPosition());
            assertEquals(position.getLevel1TagName(), origPosition.getLevel1TagName());
            assertEquals(position.getLevel2TagName(), origPosition.getLevel2TagName());
            assertEquals(position.getLevel3TagName(), origPosition.getLevel3TagName());
            assertEquals(position.getLevel4TagName(), origPosition.getLevel4TagName());
            assertEquals(position.getPositionType(), origPosition.getPositionType());
            assertEquals(position.getPrimeBroker(), origPosition.getPrimeBroker());

            ISecurity origSecurity = origPosition.getSecurity();
            ISecurity security = position.getSecurity();

            assertEquals(security.getProductCode(), origSecurity.getProductCode());
            assertEquals(security.getSecurityIdFlag(), origSecurity.getSecurityIdFlag());
            assertEquals(security.getSecurityType2(), origSecurity.getSecurityType2());
            
            assertEquals(security.getName(), origSecurity.getName());
            

            if (hasRoot(security)) {
                assertNotEquals(security.getSecurityId(), origSecurity.getSecurityId());
                assertNull(security.getTicker());
            } else {
                assertEquals(security.getSecurityId(), origSecurity.getSecurityId());
                assertEquals(security.getTicker(), origSecurity.getTicker());
            }
        }
    }
    @Test(groups = { "unittest" })
    public void testBatchFileJanuary() {
        List<BatchPosition> batchPositions = BatchPositionTest.loadFromFile("/ActiveMQAf200-2009-01-13.txt");
        SecurityRootPositionAggregationStrategy srpas = new SecurityRootPositionAggregationStrategy();

        for (BatchPosition batchPosition : batchPositions) {
            Position origPosition = batchPosition.getPosition();
            Position position = srpas.convertToAggregate(origPosition);

            assertEquals(position.getAccount(), origPosition.getAccount());
            assertEquals(position.getCurrentPosition(), origPosition.getCurrentPosition());
            assertEquals(position.getIntradayPosition(), origPosition.getIntradayPosition());
            assertEquals(position.getLevel1TagName(), origPosition.getLevel1TagName());
            assertEquals(position.getLevel2TagName(), origPosition.getLevel2TagName());
            assertEquals(position.getLevel3TagName(), origPosition.getLevel3TagName());
            assertEquals(position.getLevel4TagName(), origPosition.getLevel4TagName());
            assertEquals(position.getPositionType(), origPosition.getPositionType());
            assertEquals(position.getPrimeBroker(), origPosition.getPrimeBroker());

            ISecurity origSecurity = origPosition.getSecurity();
            ISecurity security = position.getSecurity();

            assertEquals(security.getProductCode(), origSecurity.getProductCode());
            assertEquals(security.getSecurityIdFlag(), origSecurity.getSecurityIdFlag());
            assertEquals(security.getSecurityType2(), origSecurity.getSecurityType2());
            
            assertEquals(security.getName(), origSecurity.getName());
            
            if (origSecurity.getSecurityId().equals("TYH9C")) {
                System.err.println(origSecurity);
            }
            if (hasRoot(security)) {
                assertNotEquals(security.getSecurityId(), origSecurity.getSecurityId());
                assertNull(security.getTicker());
            } else {
                assertEquals(security.getSecurityId(), origSecurity.getSecurityId());
                assertEquals(security.getTicker(), origSecurity.getTicker());
            }
        }
    }

    @Test(groups = { "unittest" })
    public void testOnlineFile() {
        List<RtfOnlinePosition> onlinePositions = RtfOnlinePositionTest.loadFromFile("/OnlineMessages-2009-03-11.txt");

        SecurityRootPositionAggregationStrategy srpas = new SecurityRootPositionAggregationStrategy();

        for (RtfOnlinePosition onlinePosition : onlinePositions) {
            BatchPosition batchPosition = BatchPosition.valueOf(onlinePosition);
            Position origPosition = batchPosition.getPosition();
            Position position = srpas.convertToAggregate(origPosition);

            assertEquals(position.getAccount(), origPosition.getAccount());
            assertEquals(position.getCurrentPosition(), origPosition.getCurrentPosition());
            assertEquals(position.getIntradayPosition(), origPosition.getIntradayPosition());
            assertEquals(position.getLevel1TagName(), origPosition.getLevel1TagName());
            assertEquals(position.getLevel2TagName(), origPosition.getLevel2TagName());
            assertEquals(position.getLevel3TagName(), origPosition.getLevel3TagName());
            assertEquals(position.getLevel4TagName(), origPosition.getLevel4TagName());
            assertEquals(position.getPositionType(), origPosition.getPositionType());
            assertEquals(position.getPrimeBroker(), origPosition.getPrimeBroker());

            ISecurity origSecurity = origPosition.getSecurity();
            ISecurity security = position.getSecurity();

            assertEquals(security.getProductCode(), origSecurity.getProductCode());
            assertEquals(security.getSecurityIdFlag(), origSecurity.getSecurityIdFlag());
            assertEquals(security.getSecurityType2(), origSecurity.getSecurityType2());
            assertEquals(security.getTicker(), origSecurity.getTicker());
            assertEquals(security.getName(), origSecurity.getName());
            
            if (origSecurity.getSecurityId().equals("UXH9")) {
                System.err.println(security);
            }

            if (hasRoot(security)) {
                assertNotEquals(security.getSecurityId(), origSecurity.getSecurityId());
            } else {
                assertEquals(security.getSecurityId(), origSecurity.getSecurityId());
            }
        }
    }
    private boolean hasRoot(ISecurity security) {

        // do the positive cases first
        if (security instanceof CommodityFutures) {
            if (security.getSecurityType2() == BBSecurityType.Unknown && security.getSecurityIdFlag() == BBSecurityIDFlag.Unknown) {
                return false;
            }
//            if (security.getSecurityType2() == BBSecurityType.Futures || security.getSecurityType2() == BBSecurityType.Option) {
                return true;
//            }
        }
        
        if (security instanceof CurrencyFutures) {
            return true;
        }
        
        if (security instanceof IndexFutures) {
            return true;
        }

        if (security instanceof DefaultFuturesSecurity) {
            return true;
        }
        
        // default to no root
        return false;
    }
}
