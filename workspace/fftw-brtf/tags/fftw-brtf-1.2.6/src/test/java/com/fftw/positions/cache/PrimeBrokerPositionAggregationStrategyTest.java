package com.fftw.positions.cache;

import static org.testng.Assert.*;

import java.util.List;

import org.testng.annotations.Test;

import com.fftw.bloomberg.PositionKey;
import com.fftw.bloomberg.batch.messages.BatchPosition;
import com.fftw.bloomberg.batch.messages.BatchPositionTest;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePositionTest;
import com.fftw.positions.ISecurity;
import com.fftw.positions.Position;
import com.fftw.util.AbstractBaseTest;

public class PrimeBrokerPositionAggregationStrategyTest extends AbstractBaseTest {

    @Test(groups = { "unittest" })
    public void testBatchFileDecember() {
        List<BatchPosition> batchPositions = BatchPositionTest.loadFromFile("/ActiveMQAf200-2008-12-09.txt");
        // List<BatchPosition> batchPositions = BatchPositionTest.loadFromFile("/BB200-2009-04-07.txt");

        PrimeBrokerPositionAggregationStrategy pbpas = new PrimeBrokerPositionAggregationStrategy();

        for (BatchPosition batchPosition : batchPositions) {
            Position origPosition = batchPosition.getPosition();
            Position position = pbpas.convertToAggregate(origPosition);

            assertEquals(position.getAccount(), origPosition.getAccount());
            assertEquals(position.getCurrentPosition(), origPosition.getCurrentPosition());
            assertEquals(position.getIntradayPosition(), origPosition.getIntradayPosition());
            assertNull(position.getLevel1TagName());
            assertNull(position.getLevel2TagName());
            assertNull(position.getLevel3TagName());
            assertNull(position.getLevel4TagName());
            assertEquals(position.getPositionType(), origPosition.getPositionType());
            assertEquals(position.getPrimeBroker(), origPosition.getPrimeBroker());

            ISecurity origSecurity = origPosition.getSecurity();
            ISecurity security = position.getSecurity();

            assertEquals(security.getProductCode(), origSecurity.getProductCode());
            assertEquals(security.getSecurityIdFlag(), origSecurity.getSecurityIdFlag());
            assertEquals(security.getSecurityType2(), origSecurity.getSecurityType2());

            assertEquals(security.getName(), origSecurity.getName());

            assertEquals(security.getSecurityId(), origSecurity.getSecurityId());
            assertEquals(security.getTicker(), origSecurity.getTicker());
            
            PositionKey positionKey = pbpas.getAggregateKeyForItem(position);
            assertNotNull(positionKey);
            assertEquals(positionKey.getAccount(), position.getAccount());
            assertNull(positionKey.getLevel1TagName());
            assertNull(positionKey.getLevel2TagName());
            assertNull(positionKey.getLevel3TagName());
            assertNull(positionKey.getLevel4TagName());
            assertNull(positionKey.getOnlineOpenPosition());
            assertEquals(positionKey.getPrimeBroker(), position.getPrimeBroker());
 
            assertEquals(positionKey.getProductCode(), security.getProductCode());
            assertEquals(positionKey.getSecurityId(), security.getSecurityId());
        }
    }

    @Test(groups = { "unittest" })
    public void testBatchFileJanuary() {
        List<BatchPosition> batchPositions = BatchPositionTest.loadFromFile("/ActiveMQAf200-2009-01-13.txt");
        PrimeBrokerPositionAggregationStrategy pbpas = new PrimeBrokerPositionAggregationStrategy();

        for (BatchPosition batchPosition : batchPositions) {
            Position origPosition = batchPosition.getPosition();
            Position position = pbpas.convertToAggregate(origPosition);

            assertEquals(position.getAccount(), origPosition.getAccount());
            assertEquals(position.getCurrentPosition(), origPosition.getCurrentPosition());
            assertEquals(position.getIntradayPosition(), origPosition.getIntradayPosition());
            assertNull(position.getLevel1TagName());
            assertNull(position.getLevel2TagName());
            assertNull(position.getLevel3TagName());
            assertNull(position.getLevel4TagName());
            assertEquals(position.getPositionType(), origPosition.getPositionType());
            assertEquals(position.getPrimeBroker(), origPosition.getPrimeBroker());

            ISecurity origSecurity = origPosition.getSecurity();
            ISecurity security = position.getSecurity();

            assertEquals(security.getProductCode(), origSecurity.getProductCode());
            assertEquals(security.getSecurityIdFlag(), origSecurity.getSecurityIdFlag());
            assertEquals(security.getSecurityType2(), origSecurity.getSecurityType2());

            assertEquals(security.getName(), origSecurity.getName());

            if (origSecurity.getSecurityId().equals("OP4F03E5")) {
                System.err.println(origSecurity);
            }
            assertEquals(security.getSecurityId(), origSecurity.getSecurityId());
            assertEquals(security.getTicker(), origSecurity.getTicker());
        }
    }

    @Test(groups = { "unittest" })
    public void testOnlineFile() {
        List<RtfOnlinePosition> onlinePositions = RtfOnlinePositionTest
            .loadFromFile("/OnlineMessages-2009-03-11.txt");

        PrimeBrokerPositionAggregationStrategy pbpas = new PrimeBrokerPositionAggregationStrategy();

        for (RtfOnlinePosition onlinePosition : onlinePositions) {
            BatchPosition batchPosition = BatchPosition.valueOf(onlinePosition);
            Position origPosition = batchPosition.getPosition();
            Position position = pbpas.convertToAggregate(origPosition);

            assertEquals(position.getAccount(), origPosition.getAccount());
            assertEquals(position.getCurrentPosition(), origPosition.getCurrentPosition());
            assertEquals(position.getIntradayPosition(), origPosition.getIntradayPosition());
            assertNull(position.getLevel1TagName());
            assertNull(position.getLevel2TagName());
            assertNull(position.getLevel3TagName());
            assertNull(position.getLevel4TagName());
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

            assertEquals(security.getSecurityId(), origSecurity.getSecurityId());
        }
    }
}
