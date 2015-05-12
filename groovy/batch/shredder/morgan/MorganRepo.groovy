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

import org.joda.time.LocalDate;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Parse Morgan Stanley Repo email.
 *
 */
public class MorganRepo {
    private DecimalFormat df = new DecimalFormat("##0.0");
    private DateTimeFormatter subjectDtf = null;
    private DateTimeFormatter outputDtf = null;

    private Map productCouponFilter = new HashMap();

    public MorganRepo() {
    }

    private String[] processFile(File srcFile) throws IOException {
        String headerLine = "";
        String dataLine = "";
        int lineCount = 0;
        String[] timeSeries = new String[2];

        srcFile.splitEachLine(",") {
            line ->
            lineCount++;

            if (lineCount > 2) {

                timeSeries[0] = headerLine;
                timeSeries[1] = dataLine;

                return
            }

            if (isEven(lineCount)) {
                // Data line
                dataLine = listToString(line)
            } else {
                // header line
                //java.util.Arrays $ArrayList
                headerLine = buildTimeSeriesHeader(line);
            }
        }

        return timeSeries;
    }


    private boolean isEven(int number) {
        return number % 2 == 0
    }


    private String listToString(List<?> list) {
        String result = "";

        list.each {
            result += ", " + it.toString()
        }
        return result;

    }
    private String buildTimeSeriesHeader(List<?> headerLine) {
        String result = ""
        headerLine.each {cell ->
            result += ", bond_government_usd_"
            String strCell = cell.toString().toLowerCase()
            strCell = strCell.replaceAll(/ old/, "o");
            if (strCell =~ /otr/) {
                strCell = strCell.replaceAll(/ /, "_")
                result += strCell
            } else {
                String[] parts = strCell.split(/ /)
                result += parts[1] + "_"
                result += parts[0]
            }

            result += "_repo:morganstanley"
        }
        return result;
    }

    /**
     * Only accept coupons that are either X.0 or X.5, all others are ignored
     */
    private boolean correctDecimal(double coupon) {
        long wholePart = coupon;
        double decimalPart = coupon - wholePart;

        return (decimalPart == 0.0 || decimalPart == 0.5);
    }

    def convertFile(String srcDir, String srcFilename, String dataDate, String outputDir) {
        //Generate a unique file name using the current time
        def srcFile = new File(srcDir + "/" + srcFilename)
        outputDtf = DateTimeFormat.forPattern("yyyyMMdd-HHmmssSSS");
        def timestamp = outputDtf.print(new DateTime())
        def outFilename = outputDir + "repo_morgan_" + timestamp + ".csv"
        def outFile = new File(outFilename)
        outFile.setReadable(true, false);

        println("Created output file: " + outFile.getName());

        subjectDtf = DateTimeFormat.forPattern("yy-MM-dd");
        LocalDate subjectDate = subjectDtf.parseDateTime(dataDate).toLocalDate();

        String[] timeSeries = processFile(srcFile);

        // save the data to the file
        outFile.append("Date")
        outFile.append(timeSeries[0])
        outFile.append("\n")
        outFile.append(convertDate(subjectDate))
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
