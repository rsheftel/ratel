package shredder.merrill;

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

public class MerrillTba
{
    private DecimalFormat df = new DecimalFormat("##0.0");
    private DateTimeFormatter dtf = null;
    
    private Map productCouponFilter = new HashMap();
    
    public MerrillTba()
    {
        // Only process these products with coupons within the specified range
        // inclusive -- NOTE also only accept X.0 and X.5
        productCouponFilter.put("fncl", [ 4.5, 11.0]);
        productCouponFilter.put("fglmc", [ 4.5, 11.0]);
        productCouponFilter.put("gnsf", [ 4.5, 11.0]);
        productCouponFilter.put("g2sf", [ 4.5, 11.0]);
        
        productCouponFilter.put("fnci", [ 4.0, 10.5]);
        productCouponFilter.put("fgci", [ 4.0, 10.5]);
    }
    
    public Map processFile (File srcFile, LocalDate subjectDate) throws IOException
    {
        Map timeSeries = new HashMap();
        dtf = DateTimeFormat.forPattern("yyyyMMdd"); // ensure the correct formatter
        int blockLineCount = 1;
        def DecimalFormat df = new DecimalFormat("##0.0")
        srcFile.splitEachLine(",") {
            line ->
            // Only process lines that have a current_coupon value of '0'
            if (line[5] =~ /0/) {
				def product = line[0].toLowerCase().trim();
				double coupon = df.parse(line[1]);
				// Restrict the products to the following list
				if (shouldProcess(product, coupon))
				{
				    def couponStr = df.format(coupon);
					def settleDateStr = line[3];
					def priceStr = line[7];
					LocalDate date = dtf.parseDateTime(line[2]).toLocalDate();
					if (!date.equals(subjectDate)) {
				    	println "Subject date and line dates do not match!";
				    	System.exit(-1);
					}
					generateTimeSeries(timeSeries, product, couponStr, blockLineCount, priceStr,settleDateStr);
					blockLineCount ++;
					if (blockLineCount >= 5) 
					{
				    	blockLineCount = 1;
					}
            	} 
            }
        }
		return timeSeries;
    }

    /**
     * Restrict to certain product types and coupon values.
     */
    private boolean shouldProcess(String product, double coupon){
        Object validProduct = productCouponFilter.get(product);
        if (validProduct != null)
        {
            Double[] minMax = (Double[])validProduct;
            return (coupon >= minMax[0] && coupon <= minMax[1] && correctDecimal(coupon))
        }
		return false;        
    }
    
    /**
     * Only accept coupons that are either X.0 or X.5, all others are ignored
     */
    private boolean correctDecimal(double coupon)
    {
        long wholePart = coupon;
        double decimalPart = coupon - wholePart;
        
        return (decimalPart == 0.0 || decimalPart == 0.5);
    }
    
    private void generateTimeSeries (Map timeSeries, String product, String couponRate, int month,
        String price, String settleDate)
    {
        StringBuilder sbCore = new StringBuilder();
        sbCore.append(product.toLowerCase()).append("_").append(couponRate).append("_");

        // Build the 4 series
        StringBuilder sb = new StringBuilder(sbCore).append(month).append("n_price:merrill");
        timeSeries.put(sb.toString(), price);

        sb = new StringBuilder(sbCore).append(month).append("n_settle_date:merrill");
        timeSeries.put(sb.toString(), settleDate);

        sb = new StringBuilder(sbCore).append(settleDate[0..5]).append("_price:merrill");
        timeSeries.put(sb.toString(), price);
        
        sb = new StringBuilder(sbCore).append(settleDate[0..5]).append("_settle_date:merrill");
        timeSeries.put(sb.toString(), settleDate);
    }
   
    def convertFile(String srcDir, String srcFilename, String dataDate, String outputDir) {
        //Generate a unique file name using the current time
        def srcFile = new File(srcDir + "/" + srcFilename)
        dtf = DateTimeFormat.forPattern("yyyyMMdd-HHmmssSSS");
        def timestamp = dtf.print(new DateTime())
        def outFilename = outputDir + "tba_merrill_" + timestamp + ".csv"
        def outFile = new File(outFilename)
        outFile.setReadable(true, false);

        println("Created output file: " + outFile.getName());
        
        dtf = DateTimeFormat.forPattern("yyyyMMdd");
        LocalDate subjectDate = dtf.parseDateTime(dataDate).toLocalDate();
        
        Map timeSeries = processFile(srcFile, subjectDate);
        
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
