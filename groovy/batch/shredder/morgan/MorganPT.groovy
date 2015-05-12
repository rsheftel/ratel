package shredder.morgan;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.joda.time.LocalDate;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class MorganPT
{

    private static final String FIRST_ROW_TEXT = "Morgan Stanley Pass Thru Marks & Spreads as of";

    private static final BigDecimal THIRTY_TWO = new BigDecimal(32);

    private static final BigDecimal TWO_FIFTY_SIX = new BigDecimal(256);

    private DecimalFormat df = new DecimalFormat("##0.0");

    private DateTimeFormatter settleMonthFormat = DateTimeFormat.forPattern("yyyyMM");

    /**
     * Map the product name to column number
     */
    private static final String[][] PRODUCTS =
    [
        [
            "g2sf", "1"
        ],
        [
            "gnsf", "3"
        ],
        [
            "fncl", "6"
        ],
        [
            "fglmc", "9"
        ],
        [
            "fnci", "16"
        ],
        [
            "fgci", "19"
        ]
    ];

    private LocalDate reportDate;

    private LocalDate firstReportMonth;
    
    public Map processExcelFile (File inputExcelFile) throws IOException
    {
        POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(inputExcelFile));

        HSSFWorkbook wb = new HSSFWorkbook(fs);

        // We only work on one sheet. Ensure it is there.

        if (wb.getNumberOfSheets() < 1)
        {
            // There is nothing to process; skip
            System.our.println("No sheet to process.");
            return -1;
        }

        HSSFSheet sheet = wb.getSheetAt(0); // the first worksheet
        int bodyStartRow = processWorkSheetHeader(sheet);
        if (bodyStartRow == -1)
        {
            // We were not able to process the sheet
            System.out.println("Unable to process sheet");
            return -1;
        }

        return processWorkSheetBody(sheet, bodyStartRow);
    }

    /**
     * This processes blocks of cells that contain the Marks and Spreads.
     * 
     * The <code>bodyStartRow</code> should be 4.
     * 
     * @param sheet
     * @param bodyStartRow
     */
    private Map processWorkSheetBody (HSSFSheet sheet, int bodyStartRow)
    {
        int rows = sheet.getPhysicalNumberOfRows();
        // We have 7 blocks, 4 rows/block and 6 products per block
        Map timeSeries = new HashMap(rows * 2);

        // We process blocks at a time - extract coupon rate for the block
        // and then the prices for the different products
        BigDecimal groupOneCoupon = null;
        BigDecimal groupTwoCoupon = null;
        int month = 1;
        int blockCount = 0;
        int r = bodyStartRow;
        while ( r < rows && blockCount < 7)
        {
            HSSFRow row = sheet.getRow(r);
            if (month == 1)
            {
                // Assume starting a new block
                HSSFCell cell = row.getCell((short)0);
                groupOneCoupon = new BigDecimal(cell.getNumericCellValue());
                cell = row.getCell((short)12);
                groupTwoCoupon = new BigDecimal(cell.getNumericCellValue());
            }

            // Process a line of the block
            // We create 3 time series per line
            for (String[] productColumn : PRODUCTS)
            {
                short column = Short.parseShort(productColumn[1]);

                // When a product is no-longer traded, the cell is either empty or does not exist.
                // In either case, we need to skip the product
                HSSFCell cell = row.getCell(column);
                if (cell != null) {
                BigDecimal bondPrice = parseBondPricing(cell.getStringCellValue());
                if (bondPrice == null || BigDecimal.ZERO.compareTo(bondPrice) > 0) 
                {
                    // No pricing or negative price, skip
                    continue;
                }
                String settleMonth = settleMonthFormat.print(firstReportMonth.plusMonths(month - 1));

                if (column < 12)
                {
                    generateTimeSeries(timeSeries, productColumn[0], groupOneCoupon, month, bondPrice,
                        settleMonth);
                }
                else
                {
                    generateTimeSeries(timeSeries, productColumn[0], groupTwoCoupon, month, bondPrice,
                        settleMonth);

                }
                }
            }
            // Finished a row, move to the next valid row
            if (month % 4 == 0) {
                // finished a block
                month = 1;
                blockCount ++;
                r++;
                continue;
            }
            // Increment our counters
            month++;
            r = r +2;
        }
        return timeSeries;
    }

    private void generateTimeSeries (Map timeSeries, String product, BigDecimal couponRate, int month,
        BigDecimal price, String settleMonth)
    {
        StringBuilder sbCore = new StringBuilder();
        sbCore.append(product).append("_").append(df.format(couponRate)).append("_");

        // Build the 3 series
        StringBuilder sb = new StringBuilder(sbCore).append(month).append("n_price:morganstanley");
        timeSeries.put(sb.toString(), price);

        sb = new StringBuilder(sbCore).append(month).append("n_settle_date:morganstanley");
        timeSeries.put(sb.toString(), settleMonth);

        sb = new StringBuilder(sbCore).append(settleMonth).append("_price:morganstanley");
        timeSeries.put(sb.toString(), price);
    }

    /**
     * This parses a string that is the bond pricing format.
     * 
     * Bond pricing format is defined as:
     * 
     * <pre>
     * YYY-XXQ
     * 
     * where:
     *  YYY - is the whole number portion
     *  XX - is the 32ndth part of the decimal portion (XX/32)
     *  Q - is the 256th part of the decimal portion (Q/8/32).
     *      when a '+' is in this position, replace it with a '4'
     * </pre>
     * 
     * @param stringCellValue
     * @return
     */
    private BigDecimal parseBondPricing (String bondPricing)
    {
         // When the products roll to the next month, some products
         // stop trading
         if (bondPricing == null || bondPricing.trim().length() == 0) 
         {
             return null;
         }
        // A '+' sign in the decimal portion (last place) is a half -- 4
        String bondPricingPlus = bondPricing.replaceAll("\\+", "4");
        boolean negativePrice = false;
        if (bondPricingPlus.startsWith("-")) {
            negativePrice = true;
            bondPricingPlus = bondPricingPlus.substring(1, bondPricingPlus.length());
        }
        String[] bondPricingParts = bondPricingPlus.split("-");

        BigDecimal wholePart = BigDecimal.valueOf(Long.parseLong(bondPricingParts[0]));

        // ensure we have at least 3 characters
        String decimalPart = bondPricingParts[1] + "000";
        long xxPart = Long.parseLong(decimalPart.substring(0, 2));
        long qPart = Long.parseLong(decimalPart.substring(2, 3));
        BigDecimal xx = BigDecimal.valueOf(xxPart).divide(THIRTY_TWO);
        BigDecimal q = BigDecimal.valueOf(qPart).divide(TWO_FIFTY_SIX);

        BigDecimal decimalPrice = wholePart.add(xx).add(q);

        if (negativePrice) {
            return decimalPrice.negate();
        }

        return decimalPrice;
    }

    /**
     * Extract all the information we need from the header and return the first
     * row after the header
     * 
     * This extracts the report date and the first reporting month
     * 
     * @param sheet
     * @return
     */
    private int processWorkSheetHeader (HSSFSheet sheet)
    {
        int rows = sheet.getPhysicalNumberOfRows();

        boolean foundStartRow = false;
        int r = sheet.getFirstRowNum();
        
        while ( r < rows)
        {
            HSSFRow row = sheet.getRow(r);
            // we are just being sure that if they remove the first row we will
            // still be able to process the workbook. The first row is not part
            // of the print area.
            if (!foundStartRow)
            {
                foundStartRow = isStartRow(row);
                if (foundStartRow)
                {
                    reportDate = extractReportDate(row);
                    // get the starting month for this report
                    row = sheet.getRow(r + 1);
                    firstReportMonth = extractFirstReportMonth(row);
                    return r + 3; // there is a blank row and headers to skip
                }
            }
            r++;
        }
        // If we make it here, something is wrong!
        return -1;
    }

    private LocalDate extractFirstReportMonth (HSSFRow row)
    {
        HSSFCell cell = row.getCell(row.getFirstCellNum());
        if (HSSFCell.CELL_TYPE_STRING == cell.getCellType())
        {
            String value = cell.getStringCellValue();
            Pattern myPattern = Pattern.compile("[a-zA-Z]{3}");

            Matcher myMatcher = myPattern.matcher(value);
            if (myMatcher.find())
            {
                DateTimeFormatter dtf = DateTimeFormat.forPattern("MMM/dd/yyyy");
                
                LocalDate today1 = new LocalDate();
                LocalDate firstOfMonth = today1.dayOfMonth().setCopy(1);
                
                StringBuilder sb = new StringBuilder();
                sb.append(myMatcher.group());
                // The text must be in Uppercase first letter, lowercase rest of
                // the word
                sb.replace(1, 3, sb.substring(1).toLowerCase());
                sb.append("/01/").append(firstOfMonth.getYear());

                LocalDate tmpDate = dtf.parseDateTime(sb.toString()).toLocalDate();
                
                if (firstOfMonth.isAfter(tmpDate))
                {
                    // Assume we guessed the year wrong, add 1 year
                    tmpDate = tmpDate.plusYears(1);
                }
                return tmpDate;
            }
        }

        return null;
    }

    private LocalDate extractReportDate (HSSFRow row)
    {
        HSSFCell cell = row.getCell(row.getFirstCellNum());
        if (HSSFCell.CELL_TYPE_STRING == cell.getCellType())
        {
            String value = cell.getStringCellValue();
            Pattern myPattern = Pattern.compile("(\\d\\d|\\d)/(\\d\\d|\\d)");

            Matcher myMatcher = myPattern.matcher(value);
            if (myMatcher.find())
            {
                DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy");
                LocalDate today = new LocalDate();
                String myMatchedValue = myMatcher.group() + "/" + today.getYear();
                LocalDate tmpDate = dtf.parseDateTime(myMatchedValue).toLocalDate();
                if (today.isBefore(tmpDate))
                {
                    // Assume we guessed the year wrong, subtract 1 year
                    tmpDate = tmpDate.minusYears(1);
                }
                return tmpDate;
            }
        }

        return null;
    }

    /**
     * The first column of the start row has the magic string.
     * 
     * @param row
     * @return
     */
    private boolean isStartRow (HSSFRow row)
    {
                // At the begining of May 2009 this started to return null for some rows
        HSSFCell cell = row.getCell(row.getFirstCellNum());
        if (cell == null)
        {
            return false;
        }
        if (HSSFCell.CELL_TYPE_STRING == cell.getCellType())
        {
            String value = cell.getStringCellValue();
            return value.contains(FIRST_ROW_TEXT);
        }

        return false;
    }
    
    def convertFile(String srcDir, String srcFilename, String dataDate, String outputDir) {
        //Generate a unique file name using the current time
        def srcFile = new File(srcDir + "/" + srcFilename)
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd-HHmmssSSS");
        def timestamp = dtf.print(new DateTime())
        def outFilename = outputDir + "tba_morgan_" + timestamp + ".csv"
        def outFile = new File(outFilename)
        outFile.setReadable(true, false);

        println("Created output file: " + outFile.getName());
        Map timeSeries = processExcelFile(srcFile);
        dtf = DateTimeFormat.forPattern("MM-dd-yy");
        LocalDate subjectDate = dtf.parseDateTime(dataDate).toLocalDate();
        if (!subjectDate.equals(reportDate)) {
            println ("Report date and subject date do not match!");
            println ("Subject date:" + subjectDate);
            println ("Report date: "+ reportDate);
        }
                
        // turn the Map into two strings
        def count = 0
        def total = timeSeries.size()
        def timeSeriesString = ""
        def values = ""

        timeSeries.each {
            entry -> count++
            timeSeriesString += entry.key
            values += entry.value
            if (count < total) {
                timeSeriesString += ","
                values += ","
            }
        }

        // save the data to the file
        outFile.append("Date,")
        outFile.append(timeSeriesString)
        outFile.append("\n")
        outFile.append(convertDate(subjectDate))
        outFile.append(",")
        outFile.append(values)
        // Make the load script happy - it produces "incomplete final line found by readTableHeader on" log entry
        outFile.append("\n")

        // Move the processed file so we have a record
        def processedFile = new File(srcDir + "/processed/" + srcFilename)
        srcFile.renameTo(processedFile)
    }
    
    /**
     * Convert a date to yyyy/MM/dd 15:00:00
     */
     def String convertDate(LocalDate srcDate) {
         DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy/MM/dd 15:00:00");

         return dtf.print(srcDate);
     }    
}
