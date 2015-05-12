package com.fftw.bloomberg.batch.messages;

import org.testng.annotations.*;
import org.joda.time.LocalDate;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;

import java.io.File;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

import com.fftw.bloomberg.types.BBSecurityIDFlag;
import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.util.Filter;

/**
 * BatchPosition Tester.
 *
 * @author mfranz
 * @version $Revision$, $Date$
 * @created March 5, 2008
 * @since 1.0
 */
public class BatchPositionTest {
    // prod, filename
    private String[] BATCHFILES = {
////            "/GissingAf200-2008-03-05.txt",
////            "/GissingAf200PROD-2008-03-04.txt",
//            "/GissingAf200DEV-2008-03-06.txt",
//            "/GissingAf200PROD-2008-03-05.txt",
//            "/GissingAf200PROD-2008-03-06.txt",
//            "/GissingAf200-2008-03-07.txt",
//            "/GissingAf200PROD-2008-03-12.txt",
//            "/GissingAf200DEV-2008-03-11.txt",
//            "/GissingAf200DEV-2008-03-11-v1.txt",
//            "/GissingAf200DEV-2008-03-17.txt",
//            "/GissingAf200-2008-03-31.txt",
//            "/GissingAf200-2008-04-02.txt",
//            "/GissingAf200-2008-04-04.txt",
//            "/GissingAf200-2008-04-07.txt"
            "/GissingAf200-2008-04-09.txt"
    };


    private static LocalDate extractDateFromFilename(String filename) {

        int startPos = filename.indexOf("-")+1;
        String date = filename.substring(startPos, startPos+ 10);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime dt =fmt.parseDateTime(date);
        return dt.toLocalDate();
    }

    @Test(groups =
            {
                    "unittest"
                    })
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
                InputStream batchPositionStream = BatchPositionTest.class.getResourceAsStream(globlalFilenameStr);
                BufferedReader batchPositionReader = new BufferedReader(new InputStreamReader(batchPositionStream));

                while ((line = batchPositionReader.readLine()) != null) {
                    lineCount++;
                    BatchPosition bp = null;
                    bp = BatchPosition.valueOf(fileDate, line);

//                    if (bp.getStrikePrice() != null && bp.getStrikePrice().doubleValue() == 1.65d) {
//                   if ("QMF".equals(bp.getAccount())
//                           (bp.getSecurityId().startsWith("FVM8") || bp.getSecurityId().startsWith("USM8"))) {
//            !bp.getSecurityId().equals(bp.getBloombergId()))
//                   ) {
//                        System.out.println(bp.getSecurityIdFlag() +", "+ bp.getSecurityId() + ", " + bp.getBloombergId()
//                        +", " + bp.buildOnlineId() +", " + bp.getIdentifier()+", " + bp.getLevel1TagName());
//                       System.out.println(bp);
                    }
//                }
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

    @Test(groups =
            {
                    "unittest"
                    })
    public void testCreate() {
        BatchPosition bp = new BatchPosition(new LocalDate(), "AAPL", "UnitTest", "Level1", "Level2", "Level3", "Level4");

        bp.setFullCurrentNetPosition(BigDecimal.TEN);
        bp.setFullCurrentNetPositionWithoutComma(BigDecimal.TEN);

// additonal fields
        bp.setSecurityIdFlag(BBSecurityIDFlag.Equity);
        bp.setProductCode(BBProductCode.Equity);

        assert BigDecimal.TEN.equals(bp.getFullCurrentNetPosition()) : "Failed to set FullCurrentNetPosition";
        assert BigDecimal.TEN.equals(bp.getFullCurrentNetPositionWithoutComma()) : "Failed to set FullCurrentNetPositionWithoutComma";
    }

    @Test(groups =
            {
                    "unittest"
                    })
    public void testFilter() {
        BatchPosition bp = new BatchPosition(new LocalDate(), "AAPL", "UnitTest", "Level1", "Level2", "Level3", "Level4");
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

}
