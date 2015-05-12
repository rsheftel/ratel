#!/usr/bin/env groovy

import java.sql.Date;
import com.fftw.tsdb.domain.cds.MarkitCompositeHist;
import com.fftw.tsdb.service.cds.CdsTimeSeriesService;
import com.fftw.tsdb.service.cds.CdsTimeSeriesServiceImpl;
import com.fftw.tsdb.service.cds.MarkitCompositeHistService;
import com.fftw.tsdb.service.cds.MarkitCompositeHistServiceImpl;


MarkitCompositeHistService markitCompositeHistService = new MarkitCompositeHistServiceImpl();
CdsTimeSeriesService cdsTimeSeriesService = new CdsTimeSeriesServiceImpl();
cdsTimeSeriesService.setUp();


Calendar startTime = Calendar.getInstance();
System.out.println("Start time: " + startTime.getTime());
Calendar processDate = Calendar.getInstance();
//go to previous day
processDate.add(Calendar.DATE, -1);
System.out.println("Processing for date: " + processDate.getTime());
List cdsDatas = markitCompositeHistService.findByDate(new Date(
    processDate.getTimeInMillis()));
for (MarkitCompositeHist markitCompositeHist : cdsDatas)
{
    //Insert 14 different time series data points
    cdsTimeSeriesService.createTimeSeriesDatas(markitCompositeHist);
}
Calendar endTime = Calendar.getInstance();
System.out.println("End time: " + endTime.getTime());
System.out.println("Total cds processed from Markit: " + cdsDatas.size());


