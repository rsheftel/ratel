#!/usr/bin/env groovy

import java.text.SimpleDateFormat
import java.util.Date
import java.text.DecimalFormat

/**
 * Parse a Goldman Mortgage file
 *
 * This takes coupon data and produces 'time series' that is then loaded (another process) into the
 * TSDB
 */
class GoldmanParse {
    def timeSeriesString = ""
    def values = ""
    def fnma30 = [:]
    def fnma30Flag = false
    def fhGold30 = [:]
    def fhGold30Flag = false
    def gnma30 = [:]
    def gnma30Flag = false
    def gnmaII = [:]
    def gnmaIIFlag = false
    def fn15 = [:]
    def fn15Flag = false
    def fhGold15 = [:]
    def fhGold15Flag = false
    def gnma15 = [:]
    def gnma15Flag = false
    def couponList = []

    /**
     * Convert the source file into the standard load file format
     */
    def convertFile(String srcDir, String srcFilename, String dataDate, String outputDir) {
    
        def srcFile = new File(srcDir + "/" + srcFilename)
        
        if (!srcFile.exists()) 
        {
            // We are the second email, skip processing
            println("Source file no-longer exists. "+ srcFile.getAbsoluteFile());
            return;
        }

        
        int blockLineCount = 0 // which line with the block we are processing

        srcFile.splitEachLine(",") {
            line ->

            if (line[0].startsWith("FNMA 30Yr")) {
                setCounters('f' as char)
                couponList = line
                blockLineCount = 0
            }
            else if (line[0].startsWith("FH Gold 30Yr")) {
                setCounters('h' as char)
                couponList = line
                blockLineCount = 0
            }
            else if (line[0].startsWith("GNMA 30Yr")) {
                setCounters('g' as char)
                couponList = line
                blockLineCount = 0
            }
            else if (line[0].startsWith("GNMA-II")) {
                setCounters('i' as char)
                couponList = line
                blockLineCount = 0
            }
            else if (line[0].startsWith("FN 15Yr")) {
                setCounters('n' as char)
                couponList = line
                blockLineCount = 0
            }
            else if (line[0].startsWith("FH Gold 15Yr")) {
                setCounters('j' as char)
                couponList = line
                blockLineCount = 0
            }
            else if (line[0].startsWith("GNMA 15Yr")) {
                setCounters('m' as char)
                couponList = line
                blockLineCount = 0
            }
            else {
                //parse actual data, check flag to see which map to store data
                blockLineCount++
                processData(line, couponList, blockLineCount)
            }
        }

        // start building the strings from the maps
        fnma30.each {
            entry -> timeSeriesString += entry.key + ","
            values += entry.value + ","
        }

        fhGold30.each {
            entry -> timeSeriesString += entry.key + ","
            values += entry.value + ","
        }

        gnma30.each {
            entry -> timeSeriesString += entry.key + ","
            values += entry.value + ","
        }

        gnmaII.each {
            entry -> timeSeriesString += entry.key + ","
            values += entry.value + ","
        }

        fn15.each {
            entry -> timeSeriesString += entry.key + ","
            values += entry.value + ","
        }

        fhGold15.each {
            entry -> timeSeriesString += entry.key + ","
            values += entry.value + ","
        }
        def count = 0
        def total = gnma15.size()
        gnma15.each {
            entry -> count++
            timeSeriesString += entry.key
            values += entry.value
            if (count < total) {
                timeSeriesString += ","
                values += ","
            }
        }

        // Open the output file after we have read the input file
        // Generate a unique file name using the current time
        def df = new SimpleDateFormat("yyyyMMdd-HHmmssSSS");
        def timestamp = df.format(new Date())
        def outFilename = outputDir + "tba_close_goldman_" + timestamp + ".csv"
        FileOutputStream fos = new FileOutputStream(outFilename);
        def lock = fos.channel.lock() // acquire exclusive lock
        
        if (lock == null) 
        {
            println("Unable to obtain lock.  Assume duplicate process." + new Date());
        } else {
            println ("Exclusive lock obtained.");
        }
        
        def outFile = new OutputStreamWriter(fos);

        outFile.append("Date,")
        outFile.append(timeSeriesString)
        outFile.append("\n")
        outFile.append(convertDate(dataDate))
        outFile.append(",")
        outFile.append(values)
        // Make the load script happy - it produces "incomplete final line found by readTableHeader on" log entry
        outFile.append("\n")

        outFile.flush();
        lock.release();
        
        outFile.close();
        
        def processedFile = new File(srcDir + "/processed/" + srcFilename)
        srcFile.renameTo(processedFile)
    }

    def processData(List inputList, List couponList, int blockLineCount) {
        def DecimalFormat df = new DecimalFormat("##0.0")
        def timeSeriesName = ''
        // ensure that we have a valid date on the line, any NA values are ignored
        def settlementPattern = /\d{8}/

        //only process if it is a valid settlement date
        if (inputList[1] =~ settlementPattern) {
            // for each coupon we have extract the price, the first two columns are month and date - skip
            for (i in 2..<inputList.size()) {
                def dateStr = inputList[1]
                def coupon = df.format(df.parse(couponList[i]))
                def shortDate = dateStr[0..5]
                def timeSeriesCore = coupon + "_" + shortDate

                if (fnma30Flag) {
                    generateTimeSeries(fnma30, "fncl", dateStr, coupon, inputList[i], blockLineCount)
                }
                else if (fhGold30Flag) {
                    generateTimeSeries(fhGold30, "fglmc", dateStr, coupon, inputList[i], blockLineCount)
                }
                else if (gnma30Flag) {
                    generateTimeSeries(gnma30, "gnsf", dateStr, coupon, inputList[i], blockLineCount)
                }
                else if (gnmaIIFlag) {
                    generateTimeSeries(gnmaII, "g2sf", dateStr, coupon, inputList[i], blockLineCount)
                }
                else if (fn15Flag) {
                    generateTimeSeries(fn15, "fnci", dateStr, coupon, inputList[i], blockLineCount)
                }
                else if (fhGold15Flag) {
                    generateTimeSeries(fhGold15, "fgci", dateStr, coupon, inputList[i], blockLineCount)
                }
                else if (gnma15Flag) {
                    generateTimeSeries(gnma15, "gnjo", dateStr, coupon, inputList[i], blockLineCount)
                }
            }
        }
    }


    def setCounters(char product) {
        fnma30Flag = false
        fhGold30Flag = false
        gnma30Flag = false
        gnmaIIFlag = false
        fn15Flag = false
        fhGold15Flag = false
        gnma15Flag = false
        switch (product) {
        //fnma30
            case 'f':
                fnma30Flag = true
                break
        //fhGold30   
            case 'h':
                fhGold30Flag = true
                break
        //gnma30    
            case 'g':
                gnma30Flag = true
                break
        //gnmaII    
            case 'i':
                gnmaIIFlag = true
                break
        //fn15    
            case 'n':
                fn15Flag = true
                break
        //fhGold15
            case 'j':
                fhGold15Flag = true
                break
        //gnma15
            case 'm':
                gnma15Flag = true
                break
            default:
                break
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

    /**
     * Build the 3 time series for each set of data
     */
    def generateTimeSeries(Map seriesMap, String prefix, String dateStr, String coupon, String price, int lineNumber) {
        String shortDate = dateStr[0..5]
        String timeSeriesCore = "_" + coupon + "_" + shortDate

        // Build the coupon TS
        String timeSeriesName = prefix + timeSeriesCore + "_price:goldman"
        seriesMap[timeSeriesName] = price
        // build the settlement date TS
        timeSeriesName = prefix + timeSeriesCore + "_settle_date:goldman"
        seriesMap[timeSeriesName] = dateStr
        // build the nth month price TS
        timeSeriesName = prefix + "_" + coupon + "_" + lineNumber + "n_price:goldman"
        seriesMap[timeSeriesName] = price
        // build the nth month settlement date TS
        timeSeriesName = prefix + "_" + coupon + "_" + lineNumber + "n_settle_date:goldman"
        seriesMap[timeSeriesName] = dateStr

    }
}