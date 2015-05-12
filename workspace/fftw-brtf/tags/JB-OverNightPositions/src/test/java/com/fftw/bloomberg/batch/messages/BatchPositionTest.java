package com.fftw.bloomberg.batch.messages;

import static org.testng.Assert.*;

import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.bloomberg.types.BBSecurityIDFlag;
import com.fftw.util.Filter;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * BatchPosition Tester.
 * 
 * @created March 5, 2008
 * @since 1.0
 */
public class BatchPositionTest {
    // prod, filename
    private String[] BATCHFILES = {
    // // "/GissingAf200-2008-03-05.txt",
    // // "/GissingAf200PROD-2008-03-04.txt",
    // "/GissingAf200DEV-2008-03-06.txt",
    // "/GissingAf200PROD-2008-03-05.txt",
    // "/GissingAf200PROD-2008-03-06.txt",
    // "/GissingAf200-2008-03-07.txt",
    // "/GissingAf200PROD-2008-03-12.txt",
    // "/GissingAf200DEV-2008-03-11.txt",
    // "/GissingAf200DEV-2008-03-11-v1.txt",
    // "/GissingAf200DEV-2008-03-17.txt",
    // "/GissingAf200-2008-03-31.txt",
    // "/GissingAf200-2008-04-02.txt"
    // "/GissingAf200-2008-04-04.txt",
    // "/GissingAf200-2008-04-07.txt"
    // "/GissingAf200-2008-04-09.txt",
//     "/ActiveMQAf200-2008-06-03.txt",

    // "/ActiveMQAf200-2008-06-08.txt"
    // "/ActiveMQAf200-2008-11-10.txt"
    //"/ActiveMQAf200-2008-12-08.txt",
    //"/ActiveMQAf200-2008-12-09.txt"
    // "/Transaction200-2008-06-05.txt"
//            "/ActiveMQAf200-2008-12-12.txt",
            "/ActiveMQAf200-2008-12-17.txt"
    };

    private static LocalDate extractDateFromFilename(String filename) {

        int startPos = filename.indexOf("-") + 1;
        String date = filename.substring(startPos, startPos + 10);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime dt = fmt.parseDateTime(date);
        return dt.toLocalDate();
    }

    private boolean matches(String fileValue) {
        return true;
        
//        return (fileValue.startsWith("TU") || fileValue.startsWith("TY") || fileValue.startsWith("FV") || fileValue
//                .startsWith("US"));
        // return (fileValue.startsWith("JY") || fileValue.startsWith("NG"));
        // return (fileValue.startsWith("CD") || fileValue.startsWith("CD"));
        // return (fileValue.startsWith("AD") || fileValue.startsWith("CD") || fileValue.startsWith("FV") ||
        // fileValue.startsWith("TU")
        // || fileValue.startsWith("TY"));
        // return (fileValue.startsWith("134429109"));
        // return true;
    }

    @Test(groups = { "unittest", "batchfiledump" })
    public void testValueOf() {
        // for now, read in a sample file and parse it.
        int lineCount = 0;
        String line = "";
        String globlalFilenameStr = "";

        try {
            for (String filename : BATCHFILES) {
                globlalFilenameStr = filename;
                LocalDate fileDate = extractDateFromFilename(filename);
                URL location = BatchPositionTest.class.getResource(globlalFilenameStr);
                System.out.println(location);
                InputStream batchPositionStream = BatchPositionTest.class
                        .getResourceAsStream(globlalFilenameStr);
                BufferedReader batchPositionReader = new BufferedReader(new InputStreamReader(batchPositionStream));

                while ((line = batchPositionReader.readLine()) != null) {
                    lineCount++;
                    BatchPosition bp = null;
                    bp = BatchPosition.valueOf(fileDate, line);

                    // if (bp.getStrikePrice() != null && bp.getStrikePrice().doubleValue() == 1.65d) {
                    if ("QMF".equals(bp.getAccount()) && matches(bp.getSecurityId())) {
                        // !bp.getSecurityId().equals(bp.getBloombergId()))
                        // ) {
                        // System.out.println(bp.getSecurityIdFlag() + ", " + bp.getSecurityId() + ", "
                        // + ", " + bp.buildOnlineId() + ", " + bp.getIdentifier() + ", " +
                        // bp.getLevel1TagName());
                         System.out.println(bp);
                     //   System.out.print("-");
                    }
                }
            }
        } catch (IOException e) {
            assert false : "Unable to read test file: " + e.getMessage();
        } catch (NumberFormatException e) {
            System.out.println("Parsing Line #" + lineCount + " of file " + globlalFilenameStr);
            System.out.println(line);
            e.printStackTrace();
            assert false : "Unable to parse numeric field: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            System.out.println("Parsing Line #" + lineCount + " of file " + globlalFilenameStr);
            System.out.println(line);
            e.printStackTrace();
            assert false : "Unable to parse field: " + e.getMessage();
        }
    }

    @Test(groups = { "unittest", "delete-topics" })
    public void testCreate() {
        BatchPosition bp = new BatchPosition(new LocalDate(), "AAPL", "UnitTest", "Level1", "Level2",
                "Level3", "Level4");

        bp.setFullCurrentNetPosition(BigDecimal.TEN);
        bp.setFullCurrentNetPositionWithoutComma(BigDecimal.TEN);

        // additonal fields
        bp.setSecurityIdFlag(BBSecurityIDFlag.Equity);
        bp.setProductCode(BBProductCode.Equity);

        assertEquals(bp.getFullCurrentNetPosition(), BigDecimal.TEN, "Failed to set FullCurrentNetPosition");
        assertEquals(bp.getFullCurrentNetPositionWithoutComma(), BigDecimal.TEN, "Failed to set FullCurrentNetPositionWithoutComma");
    }

    @Test(groups = { "unittest" })
    public void testFilter() {
        BatchPosition bp = new BatchPosition(new LocalDate(), "AAPL", "UnitTest", "Level1", "Level2",
                "Level3", "Level4");
        Map<String, String> filterFields = new HashMap<String, String>();

        filterFields.put("Account", "UnitTest");
        Filter<BatchPosition> batchFilter = BatchPosition.createFilter(filterFields);

        assert batchFilter.accept(bp) == true : "Case insensitive field names - wrong!";

        filterFields.clear();
        filterFields.put("account", "unittest");
        batchFilter = BatchPosition.createFilter(filterFields);

        assert batchFilter.accept(bp) == false : "Case insensitive value - wrong!";

        filterFields.clear();
        filterFields.put("account", "UnitTest");
        batchFilter = BatchPosition.createFilter(filterFields);

        assert batchFilter.accept(bp) == true : "Filter did not select position based on account=QMF";
    }

    @Test(groups = { "unittest" })
    public void testAggregatable() {
        BatchPosition bp = new BatchPosition(new LocalDate(), "AAPL", "UnitTest", "Level1", "Level2",
                "Level3", "Level4");
        BatchPosition other = new BatchPosition(new LocalDate(), "AAPL", "UnitTest", "Level1", "Level2",
                "Level3", "Level4");

        assert bp.aggregatable(other) : "Positions are aggregatble";
        assert other.aggregatable(bp) : "Positions are aggregatble";

        BatchPosition otherFail = new BatchPosition(new LocalDate(), "AAPL", "UnitTest", "Level1", "Level2",
                "Level3", "Level9");
        assert !bp.aggregatable(otherFail) : "Positions are NOT aggregatble";
        assert !otherFail.aggregatable(bp) : "Positions are NOT aggregatble";
    }

    @Test(groups = { "unittest" })
    public void testAggregate() {
        // Use strings here for the BigDecimal constructors or you will have double rounding issues!
        BatchPosition bp = new BatchPosition(new LocalDate(), "AAPL", "UnitTest", "Level1", "Level2",
                "Level3", "Level4");
        BatchPosition other = new BatchPosition(new LocalDate(), "AAPL", "UnitTest", "Level1", "Level2",
                "Level3", "Level4");

        bp.setFullCurrentNetPosition(new BigDecimal("100.23"));
        bp.setFullCurrentNetPositionWithoutComma(new BigDecimal("100.23"));
        bp.setCurrentLongPosition(new BigDecimal("90"));
        bp.setCurrentShortPosition(new BigDecimal("89"));

        BatchPosition result = bp.aggregate(other);

        assert (result.getFullCurrentNetPosition().compareTo(bp.getFullCurrentNetPosition()) == 0
                && result.getFullCurrentNetPositionWithoutComma().compareTo(
                        bp.getFullCurrentNetPositionWithoutComma()) == 0
                && result.getCurrentLongPosition().compareTo(bp.getCurrentLongPosition()) == 0 && result
                .getCurrentShortPosition().compareTo(bp.getCurrentShortPosition()) == 0) : "Failed to aggregate";

        other.setFullCurrentNetPosition(new BigDecimal("100.23"));
        other.setFullCurrentNetPositionWithoutComma(new BigDecimal("100.23"));
        other.setCurrentLongPosition(new BigDecimal("90"));
        other.setCurrentShortPosition(new BigDecimal("89"));

        result = bp.aggregate(other);

        assert (result.getFullCurrentNetPosition().compareTo(new BigDecimal("100.23")) == 0
                && result.getFullCurrentNetPositionWithoutComma().compareTo(new BigDecimal("100.23")) == 0
                && result.getCurrentLongPosition().compareTo(new BigDecimal("180")) == 0 && result
                .getCurrentShortPosition().compareTo(new BigDecimal("178")) == 0) : "Failed to aggregate";

        bp.setFullCurrentNetPosition(new BigDecimal("7200000.0000"));
        bp.setFullCurrentNetPositionWithoutComma(new BigDecimal("7200000.00"));
        bp.setCurrentLongPosition(new BigDecimal("-19200"));
        bp.setCurrentShortPosition(new BigDecimal("0"));

        other.setFullCurrentNetPosition(new BigDecimal("7200000.0000"));
        other.setFullCurrentNetPositionWithoutComma(new BigDecimal("7200000.00"));
        other.setCurrentLongPosition(new BigDecimal("19200"));
        other.setCurrentShortPosition(new BigDecimal("0"));

        result = bp.aggregate(other);

        assert (result.getFullCurrentNetPosition().compareTo(new BigDecimal("7200000.0000")) == 0
                && result.getFullCurrentNetPositionWithoutComma().compareTo(new BigDecimal("7200000.00")) == 0
                && result.getCurrentLongPosition().compareTo(new BigDecimal("0")) == 0 && result
                .getCurrentShortPosition().compareTo(new BigDecimal("0")) == 0) : "Failed to aggregate";

    }

}
