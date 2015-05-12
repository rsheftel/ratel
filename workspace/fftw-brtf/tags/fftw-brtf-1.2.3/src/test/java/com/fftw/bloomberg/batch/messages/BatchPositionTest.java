package com.fftw.bloomberg.batch.messages;

import static org.testng.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.testng.annotations.Test;

import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;
import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.bloomberg.types.BBSecurityIDFlag;
import com.fftw.positions.Position;
import com.fftw.util.AbstractBaseTest;
import com.fftw.util.Filter;

/**
 * BatchPosition Tester.
 */
public class BatchPositionTest extends AbstractBaseTest {
    // prod, filename
    private String[] BATCHFILES = {
    // // "/GissingAf200-2008-03-05.txt",
    // // "/GissingAf200PROD-2008-03-04.txt",
     "/GissingAf200DEV-2008-03-06.txt",
     "/GissingAf200PROD-2008-03-05.txt",
     "/GissingAf200PROD-2008-03-06.txt",
     "/GissingAf200-2008-03-07.txt",
     "/GissingAf200PROD-2008-03-12.txt",
     "/GissingAf200DEV-2008-03-11.txt",
//     "/GissingAf200DEV-2008-03-11-v1.txt",
     "/GissingAf200DEV-2008-03-17.txt",
     "/GissingAf200-2008-03-31.txt",
     "/GissingAf200-2008-04-02.txt",
     "/BB200-2009-04-07.txt",
     "/BB200-2009-04-06.txt"
    //"/ShortAndLongXLB-2008-12-09.txt"
    // "/GissingAf200-2008-04-04.txt",
    // "/GissingAf200-2008-04-07.txt"
    // "/GissingAf200-2008-04-09.txt",
    // "/ActiveMQAf200-2008-06-03.txt",
    // "/Transaction200-2008-06-05.txt"
    };

    private static LocalDate extractDateFromFilename(String filename) {

        int startPos = filename.indexOf("-") + 1;
        String date = filename.substring(startPos, startPos + 10);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime dt = fmt.parseDateTime(date);
        return dt.toLocalDate();
    }

    private boolean matches(String fileValue) {
        // return (fileValue.startsWith("TU") || fileValue.startsWith("TY") || fileValue.startsWith("FV") ||
        // fileValue.startsWith("US"));
        // return (fileValue.startsWith("JY") || fileValue.startsWith("NG"));
        // return (fileValue.startsWith("CD") || fileValue.startsWith("CD"));
        // return (fileValue.startsWith("AD") || fileValue.startsWith("CD") || fileValue.startsWith("FV") ||
        // fileValue.startsWith("TU")
        // || fileValue.startsWith("TY"));
        // return (fileValue.startsWith("134429109"));
        return true;
    }

    @Test(groups = { "unittest" })
    public void testValueOf() {
        // for now, read in a sample file and parse it.
        int lineCount = 0;
        String line = "";
        String globlalFilenameStr = "";

        try {
            for (String filename : BATCHFILES) {
                System.out.println("Processing file: " + filename);
                globlalFilenameStr = filename;
                LocalDate fileDate = extractDateFromFilename(filename);
                URL location = BatchPositionTest.class.getResource(globlalFilenameStr);
                System.out.println(location);
                InputStream batchPositionStream = BatchPositionTest.class
                    .getResourceAsStream(globlalFilenameStr);
                BufferedReader batchPositionReader = new BufferedReader(new InputStreamReader(
                    batchPositionStream));
 
                while ((line = batchPositionReader.readLine()) != null) {
                    lineCount++;
                    BatchPosition bp = null;
                    bp = BatchPosition.valueOf(fileDate, line);
                    Position position = bp.getPosition();

                    // if (bp.getStrikePrice() != null && bp.getStrikePrice().doubleValue() == 1.65d) {
                    if ("QMF".equals(bp.getAccount()) /* && matches(bp.getSecurityId()) */) {
//                        System.out.println(bp);
                        // !bp.getSecurityId().equals(bp.getBloombergId()))
                        // ) {
                        // System.out.println(bp.getSecurityIdFlag() + ", " + bp.getSecurityId() + ", "
                        // + ", " + bp.buildOnlineId() + ", " + bp.getIdentifier() + ", " +
                        // bp.getLevel1TagName());
                        // System.out.println(bp);
                        // System.out.print("-");
                    }
                }
                batchPositionReader.close();
            }
        } catch (IOException e) {
            fail("Unable to read test file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Parsing Line #" + lineCount + " of file " + globlalFilenameStr);
            System.out.println(line);
            e.printStackTrace();
            fail("Unable to parse numeric field: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Parsing Line #" + lineCount + " of file " + globlalFilenameStr);
            System.out.println(line);
            e.printStackTrace();
            fail("Unable to parse field: " + e.getMessage());
        }
    }

    @Test(groups = { "unittest" })
    public void testCreate() {
        BatchPosition bp = new BatchPosition(new LocalDate(), "AAPL", "UnitTest", "Level1", "Level2",
            "Level3", "Level4", "PB", BigDecimal.ZERO);
        // additional fields
        bp.setSecurityIdFlag(BBSecurityIDFlag.Equity);
        bp.setProductCode(BBProductCode.Equity);
        bp.setFullCurrentNetPosition(BigDecimal.TEN);
        bp.setFullCurrentNetPositionWithoutComma(BigDecimal.TEN);
        
        assertEquals(BigDecimal.TEN, bp.getFullCurrentNetPosition(), "Failed to set FullCurrentNetPosition");
        assertEquals(BigDecimal.TEN, bp.getFullCurrentNetPositionWithoutComma(),
            "Failed to set FullCurrentNetPositionWithoutComma");
    }

    @Test(groups = { "unittest" })
    public void testFilter() {
        BatchPosition bp = new BatchPosition(new LocalDate(), "AAPL", "UnitTest", "Level1", "Level2",
            "Level3", "Level4", "PB", BigDecimal.ZERO);
        Map<String, String> filterFields = new HashMap<String, String>();

        filterFields.put("Account", "UnitTest");
        Filter<BatchPosition> batchFilter = BatchPosition.createFilter(filterFields);

        assertTrue(batchFilter.accept(bp), "Case insensitive field names - wrong!");

        filterFields.clear();
        filterFields.put("account", "unittest");
        batchFilter = BatchPosition.createFilter(filterFields);

        assertFalse(batchFilter.accept(bp), "Case insensitive value - wrong!");

        filterFields.clear();
        filterFields.put("account", "UnitTest");
        batchFilter = BatchPosition.createFilter(filterFields);

        assertTrue(batchFilter.accept(bp), "Filter did not select position based on account=QMF");
    }

    @Test(groups = { "unittest" })
    public void testAggregate() {
        // Use strings here for the BigDecimal constructors or you will have double rounding issues!
        BatchPosition bp = new BatchPosition(new LocalDate(), "AAPL", "UnitTest", "Level1", "Level2",
            "Level3", "Level4", "PB", BigDecimal.ZERO);
        BatchPosition other = new BatchPosition(new LocalDate(), "AAPL", "UnitTest", "Level1", "Level2",
            "Level3", "Level4", "PB", BigDecimal.ZERO);

        bp.setProductCode(BBProductCode.Equity);
        bp.setFullCurrentNetPosition(new BigDecimal("100"));
        bp.setFullCurrentNetPositionWithoutComma(new BigDecimal("100"));
        bp.setCurrentLongPosition(new BigDecimal("90"));
        bp.setCurrentShortPosition(new BigDecimal("89"));
        
        BatchPosition result = bp.aggregate(other);

        assertEquals(result.getFullCurrentNetPosition().compareTo(bp.getFullCurrentNetPosition()), 0);

        assertEquals(result.getFullCurrentNetPositionWithoutComma().compareTo(
            bp.getFullCurrentNetPositionWithoutComma()), 0);
        assertEquals(result.getCurrentLongPosition().compareTo(bp.getCurrentLongPosition()), 0);
        assertEquals(result.getCurrentShortPosition().compareTo(bp.getCurrentShortPosition()), 0);

        // Check normalized fields
        Position position = bp.getPosition();
//        assertEqualsBD(position.getCurrentPosition(), bp.getCurrentLongPosition());
//        assertEqualsBD(position.getCurrentPosition(), bp.getCurrentShortPosition());
        assertEqualsBD(position.getCurrentPosition(), bp.getFullCurrentNetPosition());
        assertEqualsBD(position.getCurrentPosition(), bp.getFullCurrentNetPositionWithoutComma());
        
        other.setProductCode(BBProductCode.Equity);
        other.setFullCurrentNetPosition(new BigDecimal("100"));
        other.setFullCurrentNetPositionWithoutComma(new BigDecimal("100"));
        other.setCurrentLongPosition(new BigDecimal("90"));
        other.setCurrentShortPosition(new BigDecimal("89"));

        result = bp.aggregate(other);

        assertEquals(result.getFullCurrentNetPosition().compareTo(new BigDecimal("200")), 0);
        assertEquals(result.getFullCurrentNetPositionWithoutComma().compareTo(new BigDecimal("200")), 0);
        assertEquals(result.getCurrentLongPosition().compareTo(new BigDecimal("180")), 0);
        assertEquals(result.getCurrentShortPosition().compareTo(new BigDecimal("178")), 0);

        bp.setFullCurrentNetPosition(new BigDecimal("7200000"));
        bp.setFullCurrentNetPositionWithoutComma(new BigDecimal("7200000"));
        bp.setCurrentLongPosition(new BigDecimal("-19200"));
        bp.setCurrentShortPosition(new BigDecimal("0"));

        other.setFullCurrentNetPosition(new BigDecimal("7200000"));
        other.setFullCurrentNetPositionWithoutComma(new BigDecimal("7200000"));
        other.setCurrentLongPosition(new BigDecimal("19200"));
        other.setCurrentShortPosition(new BigDecimal("0"));

        result = bp.aggregate(other);

        assertEquals(result.getFullCurrentNetPosition().compareTo(new BigDecimal("14400000")), 0);
        assertEquals(result.getFullCurrentNetPositionWithoutComma().compareTo(new BigDecimal("14400000")), 0);

        assertEquals(result.getCurrentLongPosition().compareTo(new BigDecimal("0")), 0);
        assertEquals(result.getCurrentShortPosition().compareTo(new BigDecimal("0")), 0);
    }

    @Test
    public void testAddOnlineToBatch() {
        BatchPosition bp = new BatchPosition(new LocalDate(), "AAPL", "UnitTest", "Level1", "Level2",
            "Level3", "Level4", "PB", BigDecimal.ZERO);
        RtfOnlinePosition other = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2",
            "Level3", "Level4", "PB", BigDecimal.ZERO);

        bp.setProductCode(BBProductCode.Equity);
        bp.setFullCurrentNetPosition(new BigDecimal("100"));
        bp.setFullCurrentNetPositionWithoutComma(new BigDecimal("100"));
        bp.setCurrentLongPosition(new BigDecimal("90"));
        bp.setCurrentShortPosition(new BigDecimal("89"));
        
        BatchPosition result = bp.aggregate(other);
        
        assertNotNull(result);
        assertNotSame(bp, result);
        
    }
    
    public static List<BatchPosition> loadFromFile(String filename) {
        List<BatchPosition> positions = new ArrayList<BatchPosition>();
        String line = null;
        int lineCount = 0;
        
        try {
            LocalDate fileDate = extractDateFromFilename(filename);

            InputStream batchPositionStream = BatchPositionTest.class.getResourceAsStream(filename);
            BufferedReader batchPositionReader = new BufferedReader(
                new InputStreamReader(batchPositionStream));

            while ((line = batchPositionReader.readLine()) != null) {
                lineCount++;
                BatchPosition bp = BatchPosition.valueOf(fileDate, line);
                positions.add(bp);
            }
            batchPositionReader.close();
        } catch (IOException e) {
            fail("Unable to read test file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Parsing Line #" + lineCount + " of file " + filename);
            System.out.println(line);
            e.printStackTrace();
            fail("Unable to parse numeric field: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Parsing Line #" + lineCount + " of file " + filename);
            System.out.println(line);
            e.printStackTrace();
            fail("Unable to parse field: " + e.getMessage());
        }
        return positions;
    }
}
