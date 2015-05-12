#!/usr/bin/env groovy

import java.sql.Date;
import com.fftw.tsdb.domain.cds.MarkitCompositeHist;
import com.fftw.tsdb.service.cds.CdsTimeSeriesService;
import com.fftw.tsdb.service.cds.CdsTimeSeriesServiceImpl;
import com.fftw.tsdb.service.cds.MarkitCompositeHistService;
import com.fftw.tsdb.service.cds.MarkitCompositeHistServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

static final Log logger = LogFactory.getLog(MarkitCdsToTsdb.class);

//This batch job will take previous date of data from T_Markit_Cds_Composite_Hist and insert into TSDB
MarkitCompositeHistService markitCompositeHistService = new MarkitCompositeHistServiceImpl();
CdsTimeSeriesService cdsTimeSeriesService = new CdsTimeSeriesServiceImpl();
cdsTimeSeriesService.setUp();

int numDays = 4;
Calendar processDate = Calendar.getInstance();
processDate.set(2007, Calendar.SEPTEMBER, 14);
for (i in 0 ..<numDays){
	Calendar startTime = Calendar.getInstance();
	logger.info("---------Start time: " + startTime.getTime());
	processDate.add(Calendar.DATE, 1);
	logger.info("---------Processing for date: " + processDate.getTime());
	List cdsDatas = markitCompositeHistService.findByDate(new Date(processDate.getTimeInMillis()));
	for (MarkitCompositeHist markitCompositeHist : cdsDatas)
	{
    	//Insert 14 different time series data points
    	cdsTimeSeriesService.createTimeSeriesDatas(markitCompositeHist);
	}
	cdsTimeSeriesService.bulkInsert();
	Calendar endTime = Calendar.getInstance();
	logger.info("---------End time: " + endTime.getTime());
	logger.info("---------Total cds processed from Markit: " + cdsDatas.size());
	System.out.println("Finished at " + endTime.getTime());
}

