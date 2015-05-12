#!/usr/bin/env groovy

import java.sql.Date;
import com.fftw.ivydb.domain.SecurityPrice;
import com.fftw.ivydb.service.SecurityPriceService;
import com.fftw.ivydb.service.SecurityPriceServiceImpl;
import com.fftw.ivydb.service.SecurityTimeSeriesService;
import com.fftw.ivydb.service.SecurityTimeSeriesServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

static final Log logger = LogFactory.getLog(SecurityPrice.class);

SecurityPriceService securityPriceService = new SecurityPriceServiceImpl();
SecurityTimeSeriesService securityTimeSeriesService = new SecurityTimeSeriesServiceImpl();
securityTimeSeriesService.setUp();

//Number of days to run
int numDays = 20;
Calendar processDate = Calendar.getInstance();
//Set the calendar day to the day before the start date
processDate.set(2007, Calendar.AUGUST, 27);
for (i in 0 ..<numDays){
	Calendar startTime = Calendar.getInstance();
	logger.info("---------Start time: " + startTime.getTime());
	processDate.add(Calendar.DATE, 1);
	logger.info("---------Processing for date: " + processDate.getTime());
	List datas = securityPriceService.findByDate(new Date(processDate.getTimeInMillis()));
	for (SecurityPrice securityPrice : datas)
	{
    	securityTimeSeriesService.createTimeSeriesDatas(securityPrice);
	}
	securityTimeSeriesService.bulkInsert();
	Calendar endTime = Calendar.getInstance();
	logger.info("---------End time: " + endTime.getTime());
}