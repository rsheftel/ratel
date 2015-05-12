package shredder.bofa;

import java.io.File;
import java.net.URL;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Map;


import org.joda.time.LocalDate;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.DateTimeFieldType;

import org.ccil.cowan.tagsoup.Parser;

public class BofATba {

    private static final String FIRST_ROW_TEXT = "Morgan Stanley Pass Thru Marks & Spreads as of";

    private static final BigDecimal THIRTY_TWO = new BigDecimal(32);

    private static final BigDecimal TWO_FIFTY_SIX = new BigDecimal(256);

    private DecimalFormat df = new DecimalFormat("##0.0");

    private DateTimeFormatter settleMonthFormat = DateTimeFormat.forPattern("yyyyMM");

    /**
     * Map the product name to column number     [file, product, startrow, number of rows]
     */
    private static final String[][] PRODUCTS =
    [
            [
                    "15", "gnjo", "17", "11"
            ],
            [
                    "15", "fnci", "31", "11"
            ],
            [
                    "15", "fgci", "44", "11"
            ],
            [
                    "30", "gnsf", "18", "11"
            ],
            [
                    "30", "fncl", "32", "11"
            ],
            [
                    "30", "fglmc", "46", "11"
            ],
            [
                    "30", "g2sf", "60", "9"
            ]
    ];

    private LocalDate reportDate;

    private LocalDate firstReportMonth;

    public Map processHtmlFile(File inputHtmlFile) throws IOException {
        XmlSlurper slurper = new XmlSlurper(new Parser());
        URL fileUrl = new URL("file:///" + inputHtmlFile.getAbsolutePath());

        println "Opening file: " + fileUrl

        // Read in the entire document
        def html
        fileUrl.withReader {reader ->
            html = slurper.parse(reader);
        }

        def table = html.body.table;
        def fileType = determineFileType(table);
        if ("Unknown" == fileType) {
            println "Unable to determine file type";
            return null;
        }

        boolean found = extractFileDate(table);
        if (!found) {
            // We were not able to process the sheet
            System.out.println("Unable to process file");
            return null;
        }

        return processTableBody(fileType, table);
    }


    private LocalDate extractReportDate(def tableCell) {
        println "Parsing date field of " + tableCell.toString();

        DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy");
        LocalDate tmpDate = dtf.parseDateTime(tableCell.toString()).toLocalDate();

        return tmpDate;
    }

    /**
     * This processes blocks of cells that contain the Marks and Spreads.
     *
     * The <code>bodyStartRow</code> should be 4.
     *
     * @param sheet
     * @param bodyStartRow
     */
    private Map processTableBody(def fileType, def table) {
        Map timeSeries = new LinkedHashMap();

        int rowCount = 0;

        def settlementMonths;
        def productName;

        def settlmentMonthRow = getSettlementMonthRow(fileType);

        table.tr.list().each {row ->

            if (rowCount == settlmentMonthRow) {
                settlementMonths = determineSettlementMonths(row, reportDate);
            }

            if (isProductNameRow(fileType, rowCount)) {
                productName = determineProductName(row);
            }

            if (isValidDataRow(rowCount, productName)) {
                processTbaBlockLine(timeSeries, settlementMonths, productName, row);
            }

//            println "RowCount=" + rowCount++;
            rowCount++;
        }

        return timeSeries;
    }

    private boolean isProductNameRow(def fileType, int rowCount) {
        boolean returnValue = false;

        for (String[] line: PRODUCTS) {
            if (line[0] == fileType) {
                int startLine = Integer.parseInt(line[2]);
                if (!returnValue) {
                    returnValue = startLine == rowCount;
                }
            }
        }

        return returnValue;
    }

    private int getSettlementMonthRow(def fileType) {
        if (fileType == "15") {
            return 17;
        } else if (fileType == "30") {
            return 18
        }

        return -1;
    }

    private def determineSettlementMonths(def row, LocalDate reportDate) {
        LocalDate today = reportDate;
        LocalDate.Property day = today.property(DateTimeFieldType.monthOfYear());

        def settlementDates = [];

        int cellCount = 0;
        row.td.list().each {cell ->
            if (cellCount > 1 && cellCount < 7) {
                LocalDate startDate = day.setCopy(cell.toString());
                def firstOfMonth = startDate.getYear() + "-" + startDate.getMonthOfYear() + "-01";
                LocalDate firstDate = new LocalDate(firstOfMonth);
                settlementDates.add(firstDate);
            }
            cellCount++;
        }

        // ensure that the dates are increasing
        for (int i = settlementDates.size() - 1; i > 0; i--) {
            def laterDate = settlementDates[i];
            def earlierDate = settlementDates[i - 1];

            if (earlierDate.isAfter(laterDate)) {
                settlementDates[i - 1] = earlierDate.minusYears(1);
            }
        }

        return settlementDates;
    }

    private boolean isValidDataRow(int rowCount, def productName) {
        for (String[] line: PRODUCTS) {
            if (line[1] == productName) {
                int startLine = Integer.parseInt(line[2]);
                int rows = Integer.parseInt(line[3]);

                return (rowCount > startLine && rowCount <= startLine + rows);
            }
        }
        return false;
    }

    private String determineProductName(def row) {
        def returnValue = "";

        row.td.list().findIndexOf(1) {cell ->
            def cellValue = cell.toString();

            if (cellValue == "Midgets") {
                returnValue = "gnjo";
            } else if (cellValue == "Dwarfs") {
                returnValue = "fnci";
            } else if (cellValue == "FGCI") {
                returnValue = "fgci";
            } else if (cellValue == "GNMA") {
                returnValue = "gnsf";
            } else if (cellValue == "FNMA") {
                returnValue = "fncl";
            } else if (cellValue == "FGLMC") {
                returnValue = "fglmc";
            } else if (cellValue == "GNMAII") {
                returnValue = "g2sf";
            }
        }

        return returnValue;
    }

    private void processTbaBlockLine(def timeSeries, def settlementMonths, def product, def row) {
        int cellCount = 0;
        BigDecimal coupon;

        row.td.list().each {cell ->
            if (cellCount == 1) {
                //println "Coupon is: " + cell.toString() + " for " + product;
                coupon = new BigDecimal(cell.toString());
            }
            if (cellCount > 1 && cellCount < 7) {
                BigDecimal bondPrice = parseBondPricing(cell.toString());
                LocalDate settlementMonth = settlementMonths[cellCount - 2];
                String settlementStr = settleMonthFormat.print(settlementMonth);
                generateTimeSeries(timeSeries, product, coupon, cellCount - 1, bondPrice, settlementStr);
            }
            cellCount++;
        }
    }

    private void generateTimeSeries(Map timeSeries, String product, BigDecimal couponRate, int month,
                                    BigDecimal price, String settleMonth) {
        StringBuilder sbCore = new StringBuilder();
        sbCore.append(product).append("_").append(df.format(couponRate)).append("_");

        // Build the 3 series -- Actually only one, the other two were not being used
//        StringBuilder sb = new StringBuilder();
//        StringBuilder sb = new StringBuilder(sbCore).append(month).append("n_price:bankofamerica");
//        timeSeries.put(sb.toString(), price);

//        sb = new StringBuilder(sbCore).append(month).append("n_settle_date:bankofamerica");
//        timeSeries.put(sb.toString(), settleMonth);

        StringBuilder sb = new StringBuilder(sbCore).append(settleMonth).append("_price:bankofamerica");
        timeSeries.put(sb.toString(), price);
    }

    /**
     * This parses a string that is the bond pricing format.
     *
     * Bond pricing format is defined as:
     *
     * <pre>
     * YYY-XX.Q
     *
     * where:
     *  YYY - is the whole number portion
     *  XX - is the 32ndth part of the decimal portion (XX/32)
     *  . - is a separator
     *  Q - is the 256th part of the decimal portion (Q/8/32).
     *
     * </pre>
     *
     * @param stringCellValue
     * @return
     */
    private BigDecimal parseBondPricing(String bondPricing) {
        // When the products roll to the next month, some products
        // stop trading
        if (bondPricing == null || bondPricing.trim().length() == 0) {
            return null;
        }
        // Remove any decimals we might have
        String[] bondPricingParts = bondPricing.replaceAll("\\.", "").split("-");
        BigDecimal wholePart = BigDecimal.valueOf(Long.parseLong(bondPricingParts[0]));

        // ensure we have at least 3 characters
        String decimalPart = bondPricingParts[1] + "000";
        long xxPart = Long.parseLong(decimalPart.substring(0, 2));
        long qPart = Long.parseLong(decimalPart.substring(2, 3));
        BigDecimal xx = BigDecimal.valueOf(xxPart).divide(THIRTY_TWO);
        BigDecimal q = BigDecimal.valueOf(qPart).divide(TWO_FIFTY_SIX);

        return wholePart.add(xx).add(q);
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
    private boolean extractFileDate(def table) {
        table.tr.list().findIndexOf(1) {row ->
            reportDate = extractReportDate(row.td[1]);
        }
        return reportDate != null;
    }


    private String determineFileType(def table) {
        def returnValue = "Unknown";

        table.tr.list().findIndexOf(0) {row ->
            def fileType = row.td[0].toString();
            if (fileType.startsWith("30")) {
                returnValue = "30";
            } else if (fileType.startsWith("Intermed")) {
                returnValue = "15";
            }
        }
        return returnValue;
    }

    def convertFile(String srcDir, String srcFilename, String dataDate, String outputDir) {
        //Generate a unique file name using the current time
        def srcFile = new File(srcDir + "/" + srcFilename)
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd-HHmmssSSS");
        def timestamp = dtf.print(new DateTime())
        def outFilename = outputDir + "tba_bofa_" + timestamp + ".csv"
        def outFile = new File(outFilename)
        outFile.setReadable(true, false);

        println("Created output file: " + outFile.getAbsoluteFile());
        Map timeSeries = processHtmlFile(srcFile);
        dtf = DateTimeFormat.forPattern("MM/dd/yyyy");
        LocalDate subjectDate = dtf.parseDateTime(dataDate).toLocalDate();
        if (!subjectDate.equals(reportDate)) {
            println("Report date and subject date do not match!");
            println("Subject date:" + subjectDate);
            println("Report date: " + reportDate);
        }

        // turn the Map into two strings
        def count = 0
        def total = timeSeries.size()
        def timeSeriesString = ""
        def values = ""

        timeSeries.each {
            entry ->
            count++
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
        if (srcFile.renameTo(processedFile)) {
            println "moved file " + srcFile.getAbsoluteFile() + " to " + processedFile.getAbsoluteFile()
        } else {
            println "failed to move file " + srcFile.getAbsoluteFile();
        }
    }

    /**
     * Convert a date to yyyy/MM/dd 15:00:00
     */
    def String convertDate(LocalDate srcDate) {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy/MM/dd 15:00:00");

        return dtf.print(srcDate);
    }
}
