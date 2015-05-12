package shredder.jpmorgan;

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

import org.joda.time.LocalDate;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Parse JPMorgan TBA email.
 *
 */
public class JPMorganTba {
    private DecimalFormat df = new DecimalFormat("##0.0");
    private DateTimeFormatter subjectDtf = null;
    private DateTimeFormatter outputDtf = null;

    private Map productCouponFilter = new HashMap();

    public JPMorganTba() {
    }

    private String[] processFile(File srcFile) throws IOException {
        String headerLine = "Date";
        String dataLine = "";
        int lineCount = 0;
        String[] timeSeries = new String[2];

        String asOfDate = "";

        srcFile.splitEachLine(",") {
            line ->
            lineCount++;

            if (lineCount == 1) {
                asOfDate = line[0];
                def dtf = DateTimeFormat.forPattern("yyyyMMdd");
                LocalDate fileDate = dtf.parseDateTime(asOfDate).toLocalDate();
                println("File date:" + fileDate);

                dataLine = convertDate(fileDate);
            }

            if (lineCount > 4) {
                if (line != null && line.size() > 1) {
                    def product = line[0].trim();
                    def term = line[1].trim();

                    def convertedProduct = productMapping(product, term);

                    if (convertedProduct != null) {
                        def coupon = line[2].trim();

                        def price1 = line[21].trim();
                        def settleDate1 = line[25].trim();

                        def price2 = line[22].trim();
                        def settleDate2 = line[26].trim();

                        def price3 = line[23].trim();
                        def settleDate3 = line[27].trim();

                        // price
                        headerLine = headerLine + ", " + buildTicker(product, term, coupon, settleDate1, "price");
                        dataLine = dataLine + ", " + price1;

                        //settle date
                        headerLine = headerLine + ", " + buildTicker(product, term, coupon, settleDate1, "settle_date");
                        dataLine = dataLine + ", " + settleDate1;

                        // price
                        headerLine = headerLine + ", " + buildTicker(product, term, coupon, settleDate2, "price");
                        dataLine = dataLine + ", " + price2;

                        //settle date
                        headerLine = headerLine + ", " + buildTicker(product, term, coupon, settleDate2, "settle_date");
                        dataLine = dataLine + ", " + settleDate2;

                        // price
                        headerLine = headerLine + ", " + buildTicker(product, term, coupon, settleDate3, "price");
                        dataLine = dataLine + ", " + price3;

                        //settle date
                        headerLine = headerLine + ", " + buildTicker(product, term, coupon, settleDate3, "settle_date");
                        dataLine = dataLine + ", " + settleDate3;

                    }
                }
            }

            timeSeries[0] = headerLine;
            timeSeries[1] = dataLine;

        }

        return timeSeries;
    }

    private String buildTicker(def product, def term, def coupon, def settleDate, def type) {
        String result = productMapping(product, term);
        result += "_" + formatCoupon(coupon);
        result += "_" + settleDate.substring(0, 6);
        result += "_" + type;

        result += ":jpmorgan"

        return result;
    }


    private String formatCoupon(def coupon) {
        def bd = new BigDecimal(coupon);

        return df.format(bd);
    }

    def convertFile(String srcDir, String srcFilename, String dataDate, String outputDir) {
        //Generate a unique file name using the current time
        def srcFile = new File(srcDir + "/" + srcFilename)
        outputDtf = DateTimeFormat.forPattern("yyyyMMdd-HHmmssSSS");
        def timestamp = outputDtf.print(new DateTime())
        def outFilename = outputDir + "tba_jpmorgan_" + timestamp + ".csv"
        def outFile = new File(outFilename)
        outFile.setReadable(true, false);

        println("Created output file: " + outFile.getAbsolutePath());

        subjectDtf = DateTimeFormat.forPattern("yyyyMMdd");
        LocalDate subjectDate = subjectDtf.parseDateTime(dataDate).toLocalDate();

        println("Subject date: " + subjectDate);

        String[] timeSeries = processFile(srcFile);

        // save the data to the file
        outFile.append(timeSeries[0])
        outFile.append("\n")
        //outFile.append(convertDate(subjectDate))
        outFile.append(timeSeries[1])
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

    def String productMapping(String product, String term) {
        if ("FN".equalsIgnoreCase(product)) {
            if ("30".equals(term)) {
                return "fncl";
            }
            if ("15".equals(term)) {
                return "fnci";
            }
        }

        if ("FG".equalsIgnoreCase(product)) {
            if ("30".equals(term)) {
                return "fglmc";
            }
            if ("15".equals(term)) {
                return "fgci";
            }
        }

        if ("GN".equalsIgnoreCase(product)) {
            if ("30".equals(term)) {
                return "gnsf";
            }
            if ("15".equals(term)) {
                return "gnjo"
            }
        }

        if ("G2".equalsIgnoreCase(product)) {
            if ("30".equals(term)) {
                return "g2sf";
            }
        }

        // we are not processing these
        return null;
    }
}
