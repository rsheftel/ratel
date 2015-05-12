#!/usr/bin/env groovy

import groovy.sql.Sql
import java.text.SimpleDateFormat
import java.sql.Timestamp

SimpleDateFormat fileSdf = new SimpleDateFormat("yyyyMMdd_HHmm")
SimpleDateFormat dbTime = new SimpleDateFormat("yyyy/MM/dd")
Calendar date = Calendar.getInstance()
String dateName = fileSdf.format(date.getTime())

date.set(Calendar.HOUR_OF_DAY, 0)
date.set(Calendar.MINUTE, 0)
date.set(Calendar.SECOND, 0)
date.set(Calendar.MILLISECOND, 0)
//set the desire date to process
date.add(Calendar.DATE, -1)

def timeInSql = dbTime.format(date.getTime())
values = []
db = Sql.newInstance('jdbc:jtds:sqlserver://SQLPRODTS:2433/TSDB',
    'sim_load',
    'Simload5878',
    'net.sourceforge.jtds.jdbc.Driver')
    
def outFile = new File("\\\\nyux51\\data\\TSDB_upload\\Today\\markit_index" + "_" + dateName + ".csv") 
if (outFile.exists()){
    outFile.delete()
}
outFile.append('Date,')

List results = db.rows(
    "select x.date,x.name, x.series, x.version, x.compositePrice, x.compositeSpread, x.modelSpread, x.modelPrice " + 
	"from dbo.T_Markit_Index_Composite_Hist x " +
	"join "+ 
	"( " +
	"select y.name,max(cast(y.series as varchar) +  cast(y.version as varchar)) as maxseries " +
	"from dbo.T_Markit_Index_Composite_Hist y " +
	"join dbo.T_Markit_Index_Composite_Hist z " +
	"on y.name = z.name " +
	"and y.date = '" + timeInSql + "' " +
	"and y.term = '5Y' " +
	"and y.name in " + 
	"( " +
	"'CDXNAIG' " +
	",'CDXNAXO' " +
	",'CDXNAHY' " +
	",'CDXNAIGHVOL' " +
    ") " +  
	"group by y.name " +
    ") a " +
	"on x.name = a.name " +
	"and cast(x.series as varchar) + cast(x.version as varchar) = a.maxseries " +
	"where  x.date = ' " + timeInSql + "' " +
	"and x.term = '5Y'"
    )

count = 0
totalRows = results.size()
results.collect{
    row -> count++
           if ("CDXNAHY".equalsIgnoreCase(row.name)){
               outFile.append("CDXNAHY_price_composite_5y:markit,") 
               values += row.compositePrice
               outFile.append("CDXNAHY_spread_composite_5y:markit,")
               values += row.compositeSpread
               outFile.append("CDXNAHY_price_theoretical_5y:markit,")
               values += row.modelPrice
               outFile.append("CDXNAHY_spread_theoretical_5y:markit")
               if (count < totalRows){
                   outFile.append(",")
               }
               values += row.modelSpread
           }
    	   if ("CDXNAIG".equalsIgnoreCase(row.name)){
        	   outFile.append("CDXNAIG_price_composite_5y:markit,") 
               values += row.compositePrice
               outFile.append("CDXNAIG_spread_composite_5y:markit,")
               values += row.compositeSpread
               outFile.append("CDXNAIG_price_theoretical_5y:markit,")
               values += row.modelPrice
               outFile.append("CDXNAIG_spread_theoretical_5y:markit")
               if (count < totalRows){
                   outFile.append(",")
               }
               values += row.modelSpread
           }
    	   if ("CDXNAIGHVOL".equalsIgnoreCase(row.name)){
               outFile.append("CDXNAIGHVOL_price_composite_5y:markit,") 
               values += row.compositePrice
               outFile.append("CDXNAIGHVOL_spread_composite_5y:markit,")
               values += row.compositeSpread
               outFile.append("CDXNAIGHVOL_price_theoretical_5y:markit,")
               values += row.modelPrice
               outFile.append("CDXNAIGHVOL_spread_theoretical_5y:markit")
               if (count < totalRows){
                   outFile.append(",")
               }
               values += row.modelSpread
           }
    	   if ("CDXNAXO".equalsIgnoreCase(row.name)){
               outFile.append("CDXNAXO_price_composite_5y:markit,") 
               values += row.compositePrice
               outFile.append("CDXNAXO_spread_composite_5y:markit,")
               values += row.compositeSpread
               outFile.append("CDXNAXO_price_theoretical_5y:markit,")
               values += row.modelPrice
               outFile.append("CDXNAXO_spread_theoretical_5y:markit")
               if (count < totalRows){
                   outFile.append(",")
               }
               values += row.modelSpread
           }
}
outFile.append("\n" + timeInSql + ",")

count = 0
totalRows = values.size()
values.each{
    item -> count++
     		outFile.append(item)
     		if (count < totalRows){
                outFile.append(",")
            }
}
