package com.fftw.bloomberg;

import static org.testng.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import com.fftw.bloomberg.batch.messages.BatchPosition;
import com.fftw.bloomberg.batch.messages.BatchPositionTest;
import com.fftw.bloomberg.rtf.BatchPositionCache;
import com.fftw.bloomberg.rtf.OnlinePositionAggregateCache;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;
import com.fftw.test.BaseTestNG;

public class MergeBatchOnlineMessagesTest extends BaseTestNG {

    @Test(groups = { "unittest", "postiontests" })
    public void testMatchOnlineAndBatch() throws Exception {
        // use the June 3, 2008 data file fir TUU8 for Ryan's Futures
        // This should test the bug within Bloomberg where they don't
        // handle the changing of prime broker
        BatchPositionCache batchCache = loadTestBatchFile();

        assertTrue(batchCache.size() > 0, "Failed to read batch records");
        
        // check that we have the records we need
        BatchPosition bp1 = batchCache.getCacheItem(new PositionKey("TUU8", "QMF", "QF.NDayBreak",
                "Breakout", "General", "QUANTYS", new BigDecimal(96)));
        assertNotNull(bp1, "Failed to find TUU8 for NDayBreak position 96");
        assertComparable(bp1.getFullCurrentNetPosition(), new BigDecimal(7200000), "Current position is wrong");
        
        BatchPosition bp2 = batchCache.getCacheItem(new PositionKey("TUU8", "QMF", "QF.NDayBreak",
                "Breakout", "General", "QUANTYS", new BigDecimal(-96)));
        assertNotNull(bp2, "Failed to find TUU8 for NDayBreak position -96");
        assertComparable(bp2.getFullCurrentNetPosition(), new BigDecimal(7200000), "Current position is wrong");
        
        // Build the online cache
        OnlinePositionAggregateCache onlineCache = loadTestOnlineFile();    
        
        assertTrue(onlineCache.size() > 0, "Failed to read in online records");
        
        // check that we have the records we need
        RtfOnlinePosition op1 = onlineCache.getOnlinePosition(new PositionKey("TUU8", "QMF", "QF.NDayBreak",
                "Breakout", "General", "QUANTYS", new BigDecimal(96)));
        assertNotNull(op1, "Failed to find TUU8 for NDayBreak position 96");
        assertEquals(op1.getPrimeBroker(), "NO PB", "Wrong record found");
        assertEquals(op1.getCurrentPosition(), BigDecimal.ZERO, "Current position is wrong");
        
        RtfOnlinePosition op2 = onlineCache.getOnlinePosition(new PositionKey("TUU8", "QMF", "QF.NDayBreak",
                "Breakout", "General", "QUANTYS", new BigDecimal(-96)));
        assertNotNull(op2, "Failed to find TUU8 for NDayBreak position -96");
        assertEquals(op2.getPrimeBroker(), "GSFUT", "Wrong record found");
        assertEquals(op2.getCurrentPosition(), BigDecimal.ZERO, "Current position is wrong");
    }

    private OnlinePositionAggregateCache loadTestOnlineFile() throws IOException {
        OnlinePositionAggregateCache onlineCache = new OnlinePositionAggregateCache();
        
        String filename = "/OnlineMessages-2008-06-04.txt";

        InputStream onlinePositionStream = BatchPositionTest.class.getResourceAsStream(filename);
        BufferedReader onlinePositionReader = new BufferedReader(new InputStreamReader(onlinePositionStream));

        String line = null;
        while ((line = onlinePositionReader.readLine()) != null) {
            String[] pairs = line.split(",");
            RtfOnlinePosition op = RtfOnlinePosition.valueOf(pairs);
            onlineCache.addOnlinePosition(op);
        }
        return onlineCache;
    }


    
    private BatchPositionCache loadTestBatchFile() throws IOException {
        BatchPositionCache batchCache = new BatchPositionCache();

        String filename = "/ActiveMQAf200-2008-06-03.txt";
        LocalDate fileDate = new LocalDate(2008, 6, 3);

        InputStream batchPositionStream = BatchPositionTest.class.getResourceAsStream(filename);
        BufferedReader batchPositionReader = new BufferedReader(new InputStreamReader(batchPositionStream));

        String line = null;
        while ((line = batchPositionReader.readLine()) != null) {
            BatchPosition bp = BatchPosition.valueOf(fileDate, line);
            batchCache.addBatchPosition(bp);
            System.out.println(batchCache.getKeyForItem(bp));
        }
        return batchCache;
    }
}
