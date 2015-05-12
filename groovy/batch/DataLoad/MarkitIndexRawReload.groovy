#!/usr/bin/env groovy

import groovy.sql.Sql
import java.text.SimpleDateFormat
import java.sql.Timestamp

SimpleDateFormat fileSdf = new SimpleDateFormat("yyyyMMdd_HHmm")
SimpleDateFormat dbTime = new SimpleDateFormat("yyyy/MM/dd")
Calendar date = Calendar.getInstance()

db = Sql.newInstance('jdbc:jtds:sqlserver://SQLPRODTS:2433/TSDB',
    'sim_load',
    'Simload5878',
    'net.sourceforge.jtds.jdbc.Driver')
    
//Number of days to run
int numDays = 88;
date.set(2007, Calendar.AUGUST, 15)
date.set(Calendar.HOUR_OF_DAY, 0)
date.set(Calendar.MINUTE, 0)
date.set(Calendar.SECOND, 0)
date.set(Calendar.MILLISECOND, 0)
for (i in 0 ..<numDays){
    date.add(Calendar.DATE, 1);
    String dateName = fileSdf.format(date.getTime())
	String timeInSql = dbTime.format(date.getTime())
	println "Processing date " + dateName
	values = []

    
	def outFile = new File("\\\\nyux51\\data\\MarketIndexRaw\\markit_index_raw_contracts_" + dateName + ".csv") 
	if (outFile.exists()){
    	outFile.delete()
	}

	List results = db.rows(
		"select * from dbo.T_Markit_Index_Composite_Hist " +
		"where name in " + 
		"( " +
		"'CDXNAIG' " +
		",'CDXNAXO' " +
		",'CDXNAHY' " +
		",'CDXNAIGHVOL' " +
    	") " +  
		"and date = ' " + timeInSql + "' "
    )

	count = 0
	totalRows = results.size()
	if (totalRows > 0){
		outFile.append('Date,')
	}
	results.collect{
    	row -> count++
           term = row.term.toLowerCase()
           series = row.series
           version = row.version
           if ("CDXNAHY".equalsIgnoreCase(row.name)){
               outFile.append("cdxnahy_market_price_" + term + "_" + series + "_" + version + ":markit,") 
               values += row.compositePrice
               outFile.append("cdxnahy_market_spread_" + term + "_" + series + "_" + version + ":markit,")
               values += row.compositeSpread
               outFile.append("cdxnahy_model_price_" + term + "_" + series + "_" + version + ":markit,")
               values += row.modelPrice
               outFile.append("cdxnahy_model_spread_" + term + "_" + series + "_" + version + ":markit")
               if (count < totalRows){
                   outFile.append(",")
               }
               values += row.modelSpread
           }
    	   if ("CDXNAIG".equalsIgnoreCase(row.name)){
        	   outFile.append("cdxnaig_market_price_" + term + "_" + series + "_" + version + ":markit,") 
               values += row.compositePrice
               outFile.append("cdxnaig_market_spread_" + term + "_" + series + "_" + version + ":markit,")
               values += row.compositeSpread
               outFile.append("cdxnaig_model_price_" + term + "_" + series + "_" + version + ":markit,")
               values += row.modelPrice
               outFile.append("cdxnaig_model_spread_" + term + "_" + series + "_" + version + ":markit")
               if (count < totalRows){
                   outFile.append(",")
               }
               values += row.modelSpread
           }
    	   if ("CDXNAIGHVOL".equalsIgnoreCase(row.name)){
               outFile.append("cdxnaighvol_market_price_" + term + "_" + series + "_" + version + ":markit,") 
               values += row.compositePrice
               outFile.append("cdxnaighvol_market_spread_" + term + "_" + series + "_" + version + ":markit,")
               values += row.compositeSpread
               outFile.append("cdxnaighvol_model_price_" + term + "_" + series + "_" + version + ":markit,")
               values += row.modelPrice
               outFile.append("cdxnaighvol_model_spread_" + term + "_" + series + "_" + version + ":markit")
               if (count < totalRows){
                   outFile.append(",")
               }
               values += row.modelSpread
           }
    	   if ("CDXNAXO".equalsIgnoreCase(row.name)){
               outFile.append("cdxnaxo_market_price_" + term + "_" + series + "_" + version + ":markit,") 
               values += row.compositePrice
               outFile.append("cdxnaxo_market_spread_" + term + "_" + series + "_" + version + ":markit,")
               values += row.compositeSpread
               outFile.append("cdxnaxo_model_price_" + term + "_" + series + "_" + version + ":markit,")
               values += row.modelPrice
               outFile.append("cdxnaxo_model_spread_" + term + "_" + series + "_" + version + ":markit")
               if (count < totalRows){
                   outFile.append(",")
               }
               values += row.modelSpread
           }
	}

	if (totalRows > 0){
		outFile.append("\n" + timeInSql + ",")
	}

	count = 0
	totalRows = values.size()
	values.each{
    	item -> count++
     		outFile.append(item)
     		if (count < totalRows){
                outFile.append(",")
            }
	}
}	
