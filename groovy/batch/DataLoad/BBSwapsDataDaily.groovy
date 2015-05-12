#!/usr/bin/env groovy

import groovy.sql.Sql
import java.text.SimpleDateFormat
import java.sql.Timestamp	

Calendar calendar = Calendar.getInstance();
SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm")
def dateName = sdf.format(calendar.getTime())
def myList = []
              
dataDB = Sql.newInstance('jdbc:jtds:sqlserver://SQLPROD:2433/ScratchDB',
    'gfdmpop',
    'gfdmpop',
    'net.sourceforge.jtds.jdbc.Driver')
    
bloombergDB = Sql.newInstance('jdbc:jtds:sqlserver://SQLPRODTS:2433/BloombergFeedDB',
    'sim_load',
    'Simload5878',
    'net.sourceforge.jtds.jdbc.Driver')    
def jobName = bloombergDB.rows("select * from Job where nameJob = \'bloomberg_composite_ny_irs_usd_rates_mid\'")[0].nameFile

def outFile = new File("\\\\nyux51\\data\\TSDB_upload\\Today\\" + jobName + "_" + dateName + ".csv")
    

outFile.append('Date,')

List results = bloombergDB.rows('select * from BloombergData where fieldBB = \'PX_LAST\'')
count = 0
totalRows = results.size()
results.collect{
    row -> count++
           value = row.tickerBB
	       index = value.indexOf("Index")
           tickerBB = value.substring(0, index).trim()
	       valueRow = dataDB.rows("select * from T_BB_Index where bbIndexCode = \'" + tickerBB + "  Index\'")[0] 	
           outFile.append(row.nameTimeSeries)
           if (count < totalRows){
               outFile.append(",")
           }
           myList += valueRow.rateValue
           rateDate = valueRow.rateDate
}



outFile.append("\n" + getModifiedTimeString(rateDate) + ",")

count = 0
totalRows = myList.size()
myList.each{
    item -> count++
            outFile.append(item)
            if (count < totalRows){
                outFile.append(",")
            }
}

println "Finished generating file " + outFile

def String getModifiedTimeString(Timestamp date){
    Calendar observationDate = Calendar.getInstance();
    observationDate.setTimeInMillis(date.getTime());
    observationDate.set(Calendar.HOUR_OF_DAY, 15);
    observationDate.set(Calendar.MINUTE, 0);
    observationDate.set(Calendar.SECOND, 0);
    observationDate.set(Calendar.MILLISECOND, 0);
    Timestamp modifiedTime = new Timestamp(observationDate.getTimeInMillis())
    return modifiedTime.toString();
}
