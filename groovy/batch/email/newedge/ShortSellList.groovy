#!/usr/bin/env groovy
package email.newedge

import groovy.sql.Sql
import java.sql.Date
import org.joda.time.LocalDate
import org.joda.time.DateMidnight
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 *
 */
public class ShortSellList {
    
    //private static final String DB_SERVER_NAME = "SQLDEVTS"
    private static final String DB_SERVER_NAME = "SQLPRODTS"
    
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
        
        // Load up the descriptions
        def descriptionList = baDB.rows("select distinct Symbol, Description from dbo.ShortSellList where Source = 'MAN'")
        Map<String, String> descriptionMap = new HashMap<String, String>();
        for (description in descriptionList) {
             descriptionMap.put(description.Symbol, description.Description)
        }
        // Read in Excel file here
        def convertedSheet = processExcelFile(srcFile);
        
        for (List<String> row : convertedSheet.iterator()){
            def symbol = row[1].trim()
                def cusip = row[0].trim()
                def quantityStr = row[2].trim()
                long quantity = Long.parseLong(quantityStr)
                
                Map<String, String> recordMap = new HashMap<String, String>()
                recordMap.put("Symbol", symbol)
                recordMap.put("Cusip", cusip)
                recordMap.put("Quantity", quantity)
                recordMap.put("Date", dbDate)
                recordMap.put("Source", "NEWEDGE")
                def description = descriptionMap.get(symbol)
                if (description == null) {
                    recordMap.put("Description", symbol +"-" + cusip)
                } else {
                    recordMap.put("Description", description)
                }
                
                // Save data to database
                dbTable.add(recordMap);
                recordCount ++;
            
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
    
    public static List<List<String>> processExcelFile (File inputExcelFile) throws IOException
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
       
        return processWorkSheetBody(sheet, 1);
    }
    
    /**
     * Read the data from the sheet 
     * 
     * 
     * @param sheet
     * @param bodyStartRow
     */
    private static List<List<String>> processWorkSheetBody (HSSFSheet sheet, int bodyStartRow)
    {
        int rows = sheet.getPhysicalNumberOfRows();
        List<List<String>> sheetData = new ArrayList<List<String>>(rows);
        
// columns are: SecurityId, Symbol, Quantity
        for (int r = bodyStartRow;  r < rows; r++)
        {
            HSSFRow row = sheet.getRow(r);
            
            // Do the symbol first, as we may be skipping the row
            HSSFCell cell = row.getCell((short)1);
            def symbol = cell.getStringCellValue(); 
            if (symbol == null || symbol.trim().length() == 0) {
                continue;
            }

            
            cell = row.getCell((short)0);
            def securityId = cell.getStringCellValue();
            
             
                
            cell = row.getCell((short)2);
            def quantity = cell.getStringCellValue();
            quantity = quantity.replaceAll(",","")
            def rowData = new ArrayList<String>(3);
            rowData.add(securityId);
            rowData.add(symbol);
            rowData.add(quantity);
            
            sheetData.add(rowData);
        }
        return sheetData;
    }
}
