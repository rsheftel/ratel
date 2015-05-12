#!/usr/bin/env groovy

package Risk;

import groovy.sql.Sql
import java.text.SimpleDateFormat
import java.sql.Timestamp;


/**
 *
 */
class ReadRiskMetricsVarByAssetType {

    private static int HEADER_ROW = 7;

    private static String TABLE_NAME = "RiskMetrixVaRByAssetType";

    private static List<String[]> fileToDbColumnNames = new ArrayList<String[]>();

    static void main(args) {
        def cli = new CliBuilder(usage: 'groovy RiskMetrixVaRByAssetType -d dir -f filename [-o output dir]')
        cli.h(longOpt: 'help', 'usage information')
        cli.d(longOpt: 'dir', args: 1, required: true, 'directory attachment was saved to')
        cli.f(longOpt: 'filename', args: 1, required: true, 'attachment filename')
        cli.o(longOpt: 'output', args: 1, required: false, 'output dir')

        def opt = cli.parse(args)

        if (!opt) return
        if (opt.h) cli.usage();

        def srcDir = new File(opt.d);

        def srcFile = new File(srcDir, opt.f);
        println("File=" + srcFile.absoluteFile);

        // do a query to find the available columns
        def stageDB = Sql.newInstance('jdbc:jtds:sqlserver://SQLPRODTS:2433/Stage',
                'sim',
                'Sim5878',
                'net.sourceforge.jtds.jdbc.Driver')

        //stageDB.execute("SET ROWCOUNT 1");
        groovy.sql.GroovyRowResult dbRow = stageDB.firstRow("select * from " + TABLE_NAME );

        // Read the entire file creating a table of data.  The date column is at the end of
        // the file and needs to be used once we read all data and start to insert

        Map<Integer, String> fileColumnIndexMapping = new HashMap<Integer, String>();
        Map<Integer, String> dbColumnIndexMapping = new HashMap<Integer, String>();
        int lineCount = 0;
        boolean endOfDataRows = false;
        Timestamp analysisDate = null;
        List<List> rows = new ArrayList<List>();
        List<String> columnsToCreate = new ArrayList<String>();
        String portfolio = null;
        String assettype = null;
        String instrument = null;

        srcFile.splitEachLine("\t") {
            lineParts ->
            lineCount = lineCount + 1;

            if (lineCount == HEADER_ROW) {
                // This contains the headers
                int fileColumnIndex = 0;
                int dbColumnIndex = 0;

                //Add columns to represent Portfolio/AssetType hierarchy levels
                dbColumnIndexMapping.put(dbColumnIndex, "Portfolio");
                dbColumnIndex++;
                dbColumnIndexMapping.put(dbColumnIndex, "AssetType");
                dbColumnIndex++;
                
                for (columnName in lineParts) {
                    if (fileColumnIndex == 0) {
                        columnName = "Instrument";
                    }
                    fileColumnIndexMapping.put(fileColumnIndex++, columnName);
                    String generatedColumnName = buildDbColumnFromFileColumn(columnName);
                    dbColumnIndexMapping.put(dbColumnIndex, generatedColumnName);
                    dbColumnIndex++;
                }
             
               // Find all of the columns that we need to create before we insert
                for (genDbColIndex in dbColumnIndexMapping.keySet()) {
                    String genDbColName = dbColumnIndexMapping.get(genDbColIndex);
                    if (!dbRow.containsKey(genDbColName)) {
                        columnsToCreate.add(genDbColName);
                        println("Need to add column " + genDbColName);
                    }
                }
                println("New columns: " + columnsToCreate);
            } else if (lineCount > HEADER_ROW && !endOfDataRows) {
                // These are the rows that have data in them
                List<String> dbColumnData = new ArrayList<String>();
                int columnIndex = 0;
                for (String columnData in lineParts) {
                    // Warnings or errors are in the first column
                    if (columnData.contains("Problems Encountered During Processing")) {
                        // This is the end of the good data
                        endOfDataRows = true;
                    } else {
                        // This should be a valid data row
                        String columnName = fileColumnIndexMapping.get(columnIndex);
                        if (columnName != null && columnName.trim().length() > 0) {
                            if (columnName == "Instrument") {
                                if (isPorfolioLine(columnData)) {
                                    portfolio = columnData.trim();
                                    assettype = portfolio;
                                }
                                if (isAssetType(columnData)){
                                    assettype = columnData.trim();

                                }

                                dbColumnData.add(portfolio);
                                dbColumnData.add(assettype);
                         // Instrument
                                dbColumnData.add(columnData.trim());
                          } else  {
                            // data is a measure
                                dbColumnData.add(columnData);
                            }
                        } else {
                            println("skipping column " + columnIndex);
                        }

                        columnIndex++;
                    }
                }
                // We the row ends in empty tabs, the split routine drops them.  Ensure we don't
                if (dbColumnData.size() < dbColumnIndexMapping.size() && dbColumnData.size() > 1) {
                    int dataCount = dbColumnData.size();
                    int columnCount = dbColumnIndexMapping.size();

                    for (int i = 0; i < (columnCount - dataCount); i++) {
                        dbColumnData.add("");
                    }
                }
                // Add the row
                if (dbColumnData.size() > 1) {
                    rows.add(dbColumnData);

                }
            } else {
                // Skipping all lines looking for the 'Analysis Date'
                String columnData = lineParts[0];
                if (columnData.contains("Analysis Date")) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd")
                    analysisDate = new Timestamp(sdf.parse(lineParts[1]).getTime());
                }
            }
        }

// Add the date to the end
        dbColumnIndexMapping.put(dbColumnIndexMapping.size(), "AnalysisDate");

        // Check if we have already loaded this date
        groovy.sql.GroovyRowResult dbADRow = stageDB.firstRow("select * from " + TABLE_NAME + " where AnalysisDate=?", [analysisDate]);
        if (dbADRow != null && dbADRow.containsKey("AnalysisDate")) {
            println("Date already loaded " + analysisDate);
            System.exit(1);
        }

        //value pair
        def riskData = stageDB.dataSet(TABLE_NAME);

// This is the end of the file - spit out the data
        for (List row in rows) {
            List insertValues = new ArrayList();
            insertValues.addAll(row);
            insertValues.add(analysisDate);

            Map<String, String> dataMap = new HashMap<String, String>();
            int columnIndex = 0;
            for (String columnData in insertValues) {
                String columnName = dbColumnIndexMapping.get(columnIndex);
                //println("Adding: "+ columnName + ":" + columnData);
                if (columnData.toString() == "") {
                  // If we don't have a value, don't add the column
                } else {
                    dataMap.put(columnName, columnData);
                    if (columnsToCreate.contains(columnName)) {
                        String alterTableSql = createColumn(columnName, columnData)
                        println(alterTableSql)
                        stageDB.execute(alterTableSql)
                        columnsToCreate.remove(columnName)
                    }
                }
                columnIndex++;
            }
            riskData.add(dataMap);
        }

        println("Inserted " + rows.size() + " records for the " + analysisDate);
    }

    // A portfolio line has no leading space
    private static boolean isPorfolioLine(String ticker) {
        if (ticker.length() > 1) {
            return (ticker.charAt(0) != ' ');
        } else return false;
    }

    // An assettype line has 1 leading space
        private static boolean isAssetType(String ticker) {
            if (ticker.length() > 2) {
                return (ticker.charAt(0) == ' ' && ticker.charAt(1) != ' ');
            } else return false;
        }

    private static boolean isUnsettledCloseLine(String ticker) {
       return ticker.startsWith("  Unsettled Closed FX"); // there are 2 spaces
    }

    private static boolean noLongerUnsettledClose(String ticker) {
        if (ticker.length() > 3) {
            boolean validUsc = (ticker.charAt(0) == ' ' && ticker.charAt(1) == ' ' && ticker.charAt(2) == ' ');
            return !validUsc;
        } else return true;
    }

    private static String createColumn(String columnName, String columnValue) {
        // determine column type
        boolean isNumeric = false;

        try {
            Double.valueOf(columnValue)
            isNumeric = true;
            return "ALTER TABLE " + TABLE_NAME + " ADD " + columnName + " numeric(18,5) null";
        } catch (Exception e) {

        }

        return "ALTER TABLE " + TABLE_NAME + " ADD " + columnName + " varchar(50) null";
    }

    private static int addCompositColumn(int columnIndex, String columnName, HashMap<Integer, String> columnIndexMapping) {

        if (columnName == "ACCOUNT") {
            for (String dbName in buildSqlAccountNames()) {
                columnIndexMapping.put(columnIndex, dbName);
                columnIndex++;
            }
        } else if (columnName == "Country") {
            columnIndexMapping.put(columnIndex, "Country");
            columnIndex++;
        }

        return columnIndex;
    }

    private static String[] buildSqlAccountNames() {
        return ["Fund", "StrategyLevel1", "StrategyLevel2", "StrategyLevel3", "StrategyLevel4"];
    }


    private static void splitAccount(ArrayList<String> columnValues, String columnData) {
        // This is the key, break it into parts
        String[] dbKey = columnData.split("\\\\");
        // We are expecting upto 5 keys
        if (dbKey.size() > 5) {
            println("Error parsing key field -- " + dbKey.size());
        } else {
            for (String keyData in dbKey) {
                columnValues.add(keyData);
            }
            for (int i = 0; i < 5 - dbKey.size(); i++) {
                columnValues.add(""); // We really need 5 keys, add the 'null's
            }
        }
    }

    /**
     *
     */
    private static void splitCountry(ArrayList<String> columnValues, String columnData) {
        // if (columnName == "Country") {
        // if we have parts, take the last one, otherwise the original string
        String[] dbValue = columnData.split("\\\\");
        String country = "";
        if (dbValue.size() > 1) {
            country = dbValue[dbValue.size() - 1];
        } else {
            country = columnData;
        }

        if (country.endsWith("\"")) {
            country = country.substring(0, country.length() - 1);
        }
        columnValues.add(country);
    }

    private static String buildDbColumnFromFileColumn(String columnName) {
        // remove any spaces, special characters and trailing \Total
        int totalPos = columnName.indexOf("\\Total");

        if (totalPos != -1) {
            columnName = columnName.substring(0, totalPos);
        }

        columnName = columnName.replaceAll("\\.", "");

        columnName = columnName.replaceAll("-", "_");
        columnName = columnName.replaceAll("\\+", "_");
        columnName = columnName.replaceAll("%", "");

        columnName = columnName.replaceAll("\\(", "");
        columnName = columnName.replaceAll("\\)", "");
        columnName = columnName.replaceAll("/", "");
        columnName = columnName.replaceAll("&", "and");

        // this must be last
        columnName = columnName.replaceAll(" ", "");

        return columnName;
    }

}