#!/usr/bin/env groovy

import java.text.SimpleDateFormat
import java.util.Date

/**
 * Parse the Goldman FX file and create the standard time series CSV file.
 */
class GoldmanFXParse {
    def timeSeriesString = ""
    def values = ""
    def timeSeries = [:]

    /**
     * Convert the source file into the standard load file format
     */
    def convertFile(String srcDir, String srcFilename, String dataDate, String outputDir) {
        //Generate a unique file name using the current time
        def srcFile = new File(srcDir + "/" + srcFilename)
        def df = new SimpleDateFormat("yyyyMMdd-HHmmssSSS");
        def timestamp = df.format(new Date())
        def outFilename = outputDir + "fx_goldman_" + timestamp + ".csv"
        def outFile = new File(outFilename)
        outFile.setReadable(true, false);


        srcFile.splitEachLine(",") {
            line ->
            if (line[0].startsWith("Date")) {
                // skip the header line
            }
            else {
                //parse actual data, check flag to see which map to store data
                processData(line)
            }
        }

        // turn the Map into two strings
        def count = 0
        def total = timeSeries.size()
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
        outFile.append(convertDate(dataDate))
        outFile.append(",")
        outFile.append(values)
        // Make the load script happy - it produces "incomplete final line found by readTableHeader on" log entry
        outFile.append("\n")

        // Move the processed file so we have a record
        def processedFile = new File(srcDir + "/processed/" + srcFilename)
        srcFile.renameTo(processedFile)
    }


    def processData(List inputList) {

        def settlementPattern = /\d{8}/
        //only process if it is a valid settlement date
        def timeSeriesName = ''
        // Ensure we have some data, usually 13 pieces
        if (inputList.size() > 1) {
            // We skip JPY -> KRW was it is a low volume pair
            def currencyKey = inputList[3] + inputList[2]
            currencyKey = currencyKey.toLowerCase()
            if (currencyKey.equals("jpykrw")) return;

            for (j in 1..5) {
                switch (j) {
                    case 1: timeSeriesName = currencyKey + "_spot_rate_mid:goldman"
                        timeSeries[timeSeriesName.toLowerCase()] = inputList[4]
                        break;
                    case 2: timeSeriesName = currencyKey + "_" + inputList[6] + "_rate_mid:goldman"
                        timeSeries[timeSeriesName.toLowerCase()] = inputList[9]
                        break;
                    case 3: timeSeriesName = currencyKey + "_" + inputList[6] + "_vol_ln_mid:goldman"
                        timeSeries[timeSeriesName.toLowerCase()] = inputList[10]
                        break;
                    case 4: timeSeriesName = "fx_" + inputList[2] + "_" + inputList[6] + "_disc_rate_mid:goldman"
                        timeSeries[timeSeriesName.toLowerCase()] = inputList[11]
                        break;
                    case 5: timeSeriesName = "fx_" + inputList[3] + "_" + inputList[6] + "_disc_rate_mid:goldman"
                        timeSeries[timeSeriesName.toLowerCase()] = inputList[12]
                        break;
                }

            }
        }
    }


    /**
    * Convert a date from ddMMMyy -> yyyy/MM/dd HH:mm:ss
    * The time is always set to 15:00:00
    */
    def String convertDate(String srcDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMMyy");
        SimpleDateFormat ddf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        Date date = sdf.parse(srcDate);

        Calendar calDate = Calendar.getInstance();

        calDate.setTime(date);
        calDate.set(Calendar.HOUR_OF_DAY, 15);
        calDate.set(Calendar.MINUTE, 0);
        calDate.set(Calendar.SECOND, 0);
        calDate.set(Calendar.MILLISECOND, 0);

        return ddf.format(calDate.getTime())
    }
}