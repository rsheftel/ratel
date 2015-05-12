#!/usr/bin/env groovy

import java.sql.Date;
import com.fftw.ivydb.domain.StandardOptionPrice;
import com.fftw.ivydb.service.StandardOptionPriceService;
import com.fftw.ivydb.service.StandardOptionPriceServiceImpl;
import com.fftw.ivydb.service.StandardOptionTimeSeriesService;
import com.fftw.ivydb.service.StandardOptionTimeSeriesServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

static final Log logger = LogFactory.getLog(StdOptionPrice.class);

StandardOptionPriceService standardOptionPriceService = new StandardOptionPriceServiceImpl();
StandardOptionTimeSeriesService standardOptionTimeSeriesService = new StandardOptionTimeSeriesServiceImpl();
standardOptionTimeSeriesService.setUp();

//Number of days to run
int numDays = 6;
Calendar processDate = Calendar.getInstance();
//Set the calendar day to the day before the start date
processDate.set(2007, Calendar.SEPTEMBER, 9);
for (i in 0 ..<numDays){
	Calendar startTime = Calendar.getInstance();
	logger.info("---------Start time: " + startTime.getTime());
	processDate.add(Calendar.DATE, 1);
	logger.info("---------Processing for date: " + processDate.getTime());
	List datas = standardOptionPriceService.findByDate(new Date(processDate.getTimeInMillis()));
	for (StandardOptionPrice standardOptionPrice : datas)
	{
    	standardOptionTimeSeriesService.createTimeSeriesDatas(standardOptionPrice);
	}
	standardOptionTimeSeriesService.bulkInsert();
	Calendar endTime = Calendar.getInstance();
	logger.info("---------End time: " + endTime.getTime());
}

