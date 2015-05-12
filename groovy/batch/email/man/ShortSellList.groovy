#!/usr/bin/env groovy
package email.man

import groovy.sql.Sql
import java.sql.Date
import org.joda.time.LocalDate
import org.joda.time.DateMidnight
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 */
public class ShortSellList{
    
    private static final String DB_SERVER_NAME = "SQLDEVTS"
    //    private static final String DB_SERVER_NAME = "SQLPRODTS"
    
    private static final String JDBC_URL = 'jdbc:jtds:sqlserver://' + DB_SERVER_NAME + ':2433/BADB'
    private static String TABLE_NAME = "ShortSellList"
    
    static void main(args) {
        def cli = new CliBuilder(usage: 'groovy ShortSellList -d dir -f filename [-t yyyymmdd]')
        cli.h(longOpt: 'help', 'usage information')
        cli.d(longOpt: 'dir', args: 1, required: true, 'directory attachment was saved to')
        cli.f(longOpt: 'filename', args: 1, required: true, 'attachment filename')
        cli.t(longOpt: 'date', args: 1, required: false, 'date records are for')
        
        def opt = cli.parse(args)
        
        if (!opt) {
            return
        }
        if (opt.h) cli.usage();
        
        
        LocalDate today = new LocalDate();
        if (opt.t) {
            DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd");
            today = dtf.parseDateTime(opt.t).toLocalDate();
        }
        
        DateMidnight dmd = today.toDateMidnight()
        Date dbDate = new Date(dmd.getMillis())
        
        def tmpFilename = opt.f
        if (opt.f.endsWith("\"")) {
            tmpFilename = opt.f.substring(0, opt.f.length() - 1)
        }
        
        def srcFile = new File(opt.d + "/" + tmpFilename)
        println("Processing file: " + srcFile.getAbsoluteFile())
        println("Loading for " + today)
        
        def baDB = Sql.newInstance(JDBC_URL,
                'sim',
                'Sim5878',
                'net.sourceforge.jtds.jdbc.Driver')
        
        def dbTable = baDB.dataSet(TABLE_NAME);
        
        List<Map<String,String>> rows = new ArrayList<Map<String,String>>()
        int recordCount = 0
        srcFile.splitEachLine(";") { line ->
            def symbol = line[0].toUpperCase().trim()
            if (symbol != "") {
                def cusip = line[1].trim()
                def description = line[2].trim()
                def quantityStr = line[3].trim()
                long quantity = Long.parseLong(quantityStr)
                Map<String, String> recordMap = new HashMap<String, String>()
                recordMap.put("Symbol", symbol)
                recordMap.put("Cusip", cusip)
                recordMap.put("Description", description)
                recordMap.put("Quantity", quantity)
                recordMap.put("Date", dbDate)
                recordMap.put("Source", "MAN")
                
                // Save data to database
                dbTable.add(recordMap);
                recordCount ++;
            }
        }
        println("Loaded " + recordCount + " records")
        // Move the processed file so we have a record
        String filename = opt.f;
        String dstFilename = addDate(filename, today)
        File dstDir = new File(opt.d + "/processed/");
        
        if (!dstDir.exists()) {
            dstDir.mkdirs();
        }
        
        def processedFile = new File(dstDir, dstFilename)
        srcFile.renameTo(processedFile)
        
        println("Moved processed file to " + processedFile.getName())
    }
    
    private static String addDate(String filename, LocalDate ld) {
        int dotPos = filename.lastIndexOf(".");
        String extension = "";
        
        if (dotPos > -1 ){
            extension = filename.substring(dotPos+1)
            filename = filename.substring(0, dotPos)
        }
        
        String tmpStr = filename + ld.toString("yyyy-MM-dd")
        
        if (extension != "") {
            tmpStr = tmpStr + "." + extension
        }
        
        return tmpStr;
    }
}
