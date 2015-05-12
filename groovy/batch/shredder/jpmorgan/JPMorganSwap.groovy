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
public class JPMorganSwap {
    private DecimalFormat df = new DecimalFormat("##0.0");
    private DateTimeFormatter subjectDtf = null;
    private DateTimeFormatter outputDtf = null;

    List<String> invalidProducts = new ArrayList<String>();

    public JPMorganSwap() {
        invalidProducts.add("1D");
        invalidProducts.add("1M");
        invalidProducts.add("2M");
        invalidProducts.add("3M");
        invalidProducts.add("4M");
        invalidProducts.add("5M");
        invalidProducts.add("6M");
        invalidProducts.add("1Y");
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

            if (lineCount > 1) {
                if (line != null && line.size() > 1) {
                    def product = line[0].trim();
                    if (validProduct(product)) {
                        def price = line[1].trim();

                        // price
                        headerLine = headerLine + ", " + buildTicker(product);
                        dataLine = dataLine + ", " + price;
                    }
                }
            }

            timeSeries[0] = headerLine;
            timeSeries[1] = dataLine;

        }

        return timeSeries;
    }

    private boolean validProduct(String product) {
        return !invalidProducts.contains(product.toUpperCase());
    }

    private String buildTicker(String product) {
        String result = "irs_usd_rate_" + product.toLowerCase() + "_mid";
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
        def outFilename = outputDir + "swap_jpmorgan_" + timestamp + ".csv"
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
}
