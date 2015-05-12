#!/usr/bin/env groovy

import java.sql.Date;
import com.fftw.tsdb.domain.cds.MarkitCompositeHist;
import com.fftw.tsdb.service.cds.CdsTimeSeriesService;
import com.fftw.tsdb.service.cds.CdsTimeSeriesServiceImpl;
import com.fftw.tsdb.service.cds.MarkitCompositeHistService;
import com.fftw.tsdb.service.cds.MarkitCompositeHistServiceImpl;
import com.fftw.tsdb.service.arbitration.ArbitrationService;
import com.fftw.tsdb.service.arbitration.ArbitrationServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

static final Log logger = LogFactory.getLog(MarkitCdsToTsdbDaily.class);

//This batch job will take previous date of data from T_Markit_Cds_Composite_Hist and insert into TSDB
MarkitCompositeHistService markitCompositeHistService = new MarkitCompositeHistServiceImpl();
CdsTimeSeriesService cdsTimeSeriesService = new CdsTimeSeriesServiceImpl();
cdsTimeSeriesService.setUp();
ArbitrationService arbitrationService = new ArbitrationServiceImpl();


Calendar startTime = Calendar.getInstance();
logger.info("---------Start time for inserting data: " + startTime.getTime());
Calendar processDate = Calendar.getInstance();
//go to previous day
processDate.add(Calendar.DATE, -1);
logger.info("---------Processing for date: " + processDate.getTime());
List cdsDatas = markitCompositeHistService.findByDate(new Date(
    processDate.getTimeInMillis()));
for (MarkitCompositeHist markitCompositeHist : cdsDatas)
{
    //Insert 14 different time series data points
    cdsTimeSeriesService.createTimeSeriesDatas(markitCompositeHist);
}
cdsTimeSeriesService.bulkInsert();
Calendar endTime = Calendar.getInstance();
logger.info("---------End time for inserting data: " + endTime.getTime());
logger.info("---------Total cds processed from Markit: " + cdsDatas.size());
if (cdsDatas.size() != 0){
    logger.info("Arbitrating data ...")
    processDate.set(Calendar.HOUR_OF_DAY, 0);
    processDate.set(Calendar.MINUTE, 0);
    processDate.set(Calendar.SECOND, 0);
    processDate.set(Calendar.MILLISECOND, 0);
    arbitrationService.cdsArbitration(processDate);
    logger.info("Finished arbitration.")
}
System.out.println("MarkitCdsToTsdbDaily.groovy with arbitration Finished at " + endTime.getTime());
