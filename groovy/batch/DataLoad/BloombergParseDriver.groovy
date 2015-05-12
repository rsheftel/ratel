#!/usr/bin/env groovy

import groovy.sql.Sql
import java.text.SimpleDateFormat
import java.sql.Timestamp
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import com.fftw.bloomberg.tsdbfeeds.TSDBFeed;


def jdbcUrl = 'jdbc:jtds:sqlserver://SQLPRODTS:2433/BloombergFeedDB';
//def jdbcUrl = 'jdbc:jtds:sqlserver://SQLDEVTS:2433/BloombergFeedDB';

// We accept the 'time period' to run for, or no period, if we have no specified period
// round down to the nearest 15 minute interval.
def cli = new CliBuilder(usage: 'groovy BloombergParseDriver.groovy [-t <time>] [-o <minutes>] [-d <directory>]')
cli.h(longOpt: 'help', 'usage information')
cli.t(argName: 'time', longOpt: 'time', args: 1, required: false, 'Time for jobs to run')
cli.j(argName: 'jobid', longOpt: 'jobid', args: 1, required: false, 'jobid to run')
cli.d(argName: 'output directory', longOpt: 'dir', args: 1, required: false, 'Base directory for output')


def options = cli.parse(args)

if (!options) return
if (options.h) cli.usage()

String baseDir = "/data/TSDB_upload/Today/";
if (options.d) {
    baseDir = options.d;
}

if (options.t && options.j) {
    println("Cannot select both -t and -j");
    System.exit(-1);
}

String sqlStr;

// Put in the default date/time
DateTime startTime = new DateTime(1900, 01, 01, 0, 0, 0, 0);
DateTime endTime = new DateTime(1900, 01, 01, 0, 0, 0, 0);
DateTime now = new DateTime();


if (options.t) {
    time = options.t;
    String[] parts = time.split(":");
    startTime = setTime(startTime, parts[0], parts[1]);
    endTime = setTime(endTime, parts[0], parts[1]);
    // we are not checking previous time periods, just those that match
    sqlStr = "select * from Job where CAST ( timeRun AS datetime ) >= '" +
            new Timestamp(startTime.millis) + "' and CAST ( timeRun AS datetime ) <= '" + new Timestamp(endTime.millis) + "'"
} else if (options.j) {
    sqlStr = "select * from Job where idJob=" + options.j;
} else {

    DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
    String select = "select * from Job";
    String scheduledJobs = "CAST ( '" + fmt.print(now) + " ' + timeRun AS datetime ) > lastRunOn " +
            "and CAST ( '" + fmt.print(now) + " ' + timeRun AS datetime ) < getDate()";

    String neverRunJobs = " lastRunOn is null and CAST ( '" + fmt.print(now) + " ' + timeRun AS datetime ) < getDate()";

    String withinRange = " and CAST ( '" + fmt.print(now) + " ' + timeRun AS datetime ) > '" + fmt.print(now) + " 00:00:00' " +
            " and CAST ( '" + fmt.print(now) + " ' + timeRun AS datetime ) < '" + fmt.print(now) + " 12:00:00' ";

    sqlStr = select + " where (" + scheduledJobs + withinRange + ") or (" + neverRunJobs + withinRange + ")";

}


bloombergDB = Sql.newInstance(jdbcUrl,
        'sim_load',
        'Simload5878',
        'net.sourceforge.jtds.jdbc.Driver')

println(sqlStr);

List jobList = bloombergDB.rows(sqlStr);

// Loop here
println("Selected " + jobList.size() + " jobs to run");
for (jobInfo in jobList) {
    // Process each job in turn
    println("Processing job ID: " + jobInfo.idJob + " - " + jobInfo.nameJob);
    TSDBFeed tsdbFeed = new TSDBFeed(jobInfo.idJob, bloombergDB.connection);
    if (tsdbFeed.performTradeFeedRequest()) {
        createExcel(jobInfo, baseDir, now);

    }
    println("Completed scheduled run");
}

/**
 *
 */
private createExcel(def jobInfo, String baseDir, DateTime runTime) {
    Calendar today = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm")
    SimpleDateFormat dbTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
    String dateName = sdf.format(today.getTime())
    myList = []
    hour = ""
    minute = ""
    second = ""

    def jobName = jobInfo.nameFile

    timeObservation = jobInfo.timeObservation.split(":")
    def rateDate = today
    rateDate.add(Calendar.DATE, -1)
    rateDate.set(Calendar.HOUR_OF_DAY, new Integer(timeObservation[0]))
    rateDate.set(Calendar.MINUTE, new Integer(timeObservation[1]))
    rateDate.set(Calendar.SECOND, new Integer(timeObservation[2]))
    rateDate.set(Calendar.MILLISECOND, 0)


    def timeInSql = dbTime.format(rateDate.getTime())
    List results = bloombergDB.rows("select b.nameTimeSeries, h.datetimeObservation, h.value from dbo.BloombergData b, "
            + "JobBloombergDataHist h where b.idBBData = h.idBBData and h.idJob = " + jobInfo.idJob
            + " and datetimeObservation = '" + timeInSql + "'")
    count = 0
    totalRows = results.size()

    if (totalRows > 0) {
        def outFile = new File(baseDir + jobName + "_" + dateName + ".csv")
        if (outFile.exists()) {
            outFile.delete()
        }
        outFile.append('Date,')

        results.collect {
            row -> count++
            outFile.append(row.nameTimeSeries)
            if (count < totalRows) {
                outFile.append(",")
            }
            myList += row.value
        }
        outFile.append("\n" + timeInSql + ",")

        count = 0
        totalRows = myList.size()
        myList.each {
            item -> count++
            outFile.append(item)
            if (count < totalRows) {
                outFile.append(",")
            }
        }
    } else {
        println("Did not create output file - no data found");
    }

    List params = [new Timestamp(runTime.millis), jobInfo.idJob];
    bloombergDB.executeUpdate("update Job set lastRunOn=? where idJob=?", params);
}

private DateTime setTime(DateTime origTime, String hour, String minute) {
    return setTime(origTime, Integer.parseInt(hour), Integer.parseInt(minute));
}

private DateTime setTime(DateTime origTime, int hour, int minute) {
    return new DateTime(origTime.year, origTime.monthOfYear, origTime.dayOfMonth, hour, minute, 0, 0);
}


