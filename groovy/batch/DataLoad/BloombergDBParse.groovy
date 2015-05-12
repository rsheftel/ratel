#!/usr/bin/env groovy
 
import groovy.sql.Sql
import java.text.SimpleDateFormat
import java.sql.Timestamp	

Calendar today = Calendar.getInstance();
SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm")
SimpleDateFormat dbTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
String dateName = sdf.format(today.getTime())
myList = []
hour = ""
minute = ""
second = ""

bloombergDB = Sql.newInstance('jdbc:jtds:sqlserver://SQLPRODTS:2433/BloombergFeedDB',
    'sim_load',
    'Simload5878',
    'net.sourceforge.jtds.jdbc.Driver')  

    
def cli = new CliBuilder(usage: 'groovy BloombergDBParse.groovy -j[h] "text"' )
cli.h(longOpt:'help', 'usage information')
cli.j(argName:'job number', longOpt:'job', args:1, required:true, 'Job id to execute parsing for')

def options = cli.parse(args)
if (!options) return
if (options.h) cli.usage()

jobId = options.j

def jobInfo = bloombergDB.rows("select * from Job where idJob = " + jobId)[0]
def jobName = jobInfo.nameFile

timeObservation = jobInfo.timeObservation.split(":")
def rateDate = today
rateDate.add(Calendar.DATE, -1)
rateDate.set(Calendar.HOUR_OF_DAY, new Integer(timeObservation[0]))
rateDate.set(Calendar.MINUTE, new Integer(timeObservation[1]))
rateDate.set(Calendar.SECOND, new Integer(timeObservation[2]))
rateDate.set(Calendar.MILLISECOND, 0)

def outFile = new File("/data/TSDB_upload/Today/" + jobName + "_" + dateName + ".csv")
if (outFile.exists()){
    outFile.delete()
}
outFile.append('Date,')

def timeInSql = dbTime.format(rateDate.getTime())
List results = bloombergDB.rows("select b.nameTimeSeries, h.datetimeObservation, h.value from dbo.BloombergData b, " 
                                 + "JobBloombergDataHist h where b.idBBData = h.idBBData and h.idJob = " + jobId 
                                 + " and datetimeObservation = '" + timeInSql + "'")
count = 0
totalRows = results.size()
results.collect{
    row -> count++
           outFile.append(row.nameTimeSeries)
           if (count < totalRows){
               outFile.append(",")
           }
           myList += row.value
}
outFile.append("\n" + timeInSql + ",")

count = 0
totalRows = myList.size()
myList.each{
    item -> count++
     		outFile.append(item)
     		if (count < totalRows){
                outFile.append(",")
            }
}

