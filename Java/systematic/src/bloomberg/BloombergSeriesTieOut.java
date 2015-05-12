package bloomberg;

import static futures.BloombergJobTable.*;
import static tsdb.DataSource.*;
import static util.Dates.*;
import static util.Log.*;
import static util.Objects.*;

import java.util.*;

import tsdb.*;
import util.*;
import futures.*;

public class BloombergSeriesTieOut {

    public static void main(String[] args) {
        doNotDebugSqlForever();
        List<String> jobs = BLOOMBERG_JOBS.C_NAMEJOB.values();
        List<Date> dates = list(date("2009/05/20"));
        for (String jobName : jobs) {
            BloombergJob job = new BloombergJob(jobName);
            for(BloombergJobEntry entry : job.entries()) {
                SeriesSource spreadsheetSs = entry.seriesSource();
                SeriesSource downloaderSs = spreadsheetSs.series().with(BLOOMBERG_TEST);
                for(Date date : dates) {
                    Date time = job.observationTime(date);
                    Range range = new Range(time, time);
                    Observations spreadsheet = spreadsheetSs.observations(range);
                    Observations downloader = downloaderSs.observations(range);
                    String prefix = spreadsheetSs + "," + ymdHuman(time);
                    if(spreadsheet.isEmpty() && downloader.isEmpty()) continue;
                    if(spreadsheet.isEmpty() && downloader.hasContent())
                        info(prefix + " spreadsheet is missing");
                    else if(spreadsheet.hasContent() && downloader.isEmpty())
                        info(prefix + " downloader is missing");
                    else if(spreadsheet.value() != downloader.value())
                        info(prefix + " (old != new): " + spreadsheet.value() + " != " + downloader.value());
                }
            }
        }
    }
}
