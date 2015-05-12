package shredder.jpmorgan;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;


import org.joda.time.LocalDate;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Parse JPMorgan TBA supplemental info email.
 *
 */
public class JPMorganTbaSupplemental {
    private DecimalFormat df = new DecimalFormat("##0.0");
    private DateTimeFormatter subjectDtf = null;
    private DateTimeFormatter outputDtf = null;

    public JPMorganTbaSupplemental() {
    }

    private String[] processFile(File srcFile) throws IOException {
        String headerLine = "";
        String dataLine = "";
        int lineCount = 0;
        String[] timeSeries = new String[2];

        String asOfDate = "";

        srcFile.splitEachLine(",") {
            line ->
            lineCount++;

            if (lineCount > 1) {
                if (line != null && line.size() > 1) {
                    def product = line[0].trim();
                    def term = line[1].trim();
                    def contract = line[3].trim();

                    if ("front".equalsIgnoreCase(contract)) {
                        def convertedProduct = productMapping(product, term);

                        if (convertedProduct != null) {
                            def coupon = line[2].trim();

                            def liborOas = line[12].trim();
                            def liborOad = line[14].trim();
                            def spreadDur = line[18].trim();
                            def krd2y = line[25].trim();
                            def krd5y = line[26].trim();
                            def krd10y = line[27].trim();
                            def krd30y = line[28].trim();

                            headerLine = headerLine + ", " + buildTicker(product, term, coupon, "1n_libor_oas");
                            dataLine = dataLine + ", " + liborOas;

                            headerLine = headerLine + ", " + buildTicker(product, term, coupon, "1n_dv01");
                            dataLine = dataLine + ", " + liborOad;

                            headerLine = headerLine + ", " + buildTicker(product, term, coupon, "1n_spread_duration");
                            dataLine = dataLine + ", " + spreadDur;

                            headerLine = headerLine + ", " + buildTicker(product, term, coupon, "1n_partial_duration_2y");
                            dataLine = dataLine + ", " + krd2y;

                            headerLine = headerLine + ", " + buildTicker(product, term, coupon, "1n_partial_duration_5y");
                            dataLine = dataLine + ", " + krd5y;

                            headerLine = headerLine + ", " + buildTicker(product, term, coupon, "1n_partial_duration_10y");
                            dataLine = dataLine + ", " + krd10y;

                            headerLine = headerLine + ", " + buildTicker(product, term, coupon, "1n_partial_duration_30y");
                            dataLine = dataLine + ", " + krd30y;

                        }
                    }
                }
            }

            timeSeries[0] = headerLine;
            timeSeries[1] = dataLine;

        }

        return timeSeries;
    }

    private String buildTicker(def product, def term, def coupon, def type) {
        String result = productMapping(product, term);
        result += "_" + formatCoupon(coupon);
        result += "_" + type;

        result += ":model_jpmorgan_bondStudio2008"

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
        def outFilename = outputDir + "tba_sup_jpmorgan_" + timestamp + ".csv"
        def outFile = new File(outFilename)
        outFile.setReadable(true, false);

        println("Created output file: " + outFile.getAbsolutePath());

        subjectDtf = DateTimeFormat.forPattern("yyyyMMdd");
        LocalDate subjectDate = subjectDtf.parseDateTime(dataDate).toLocalDate();

        println("Passed in date: " + subjectDate);

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

    def String productMapping(String product, String term) {
        if ("FN".equalsIgnoreCase(product)) {
            if ("30".equals(term)) {
                return "fncl";
            }
            if ("15".equals(term)) {
                return "fnci";
            }
        }
/*
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
  */
        // we are not processing these
        return null;
    }
}
