#!/usr/bin/env groovy

import java.sql.Date;
import com.fftw.ivydb.domain.SecurityPrice;
import com.fftw.ivydb.service.SecurityPriceService;
import com.fftw.ivydb.service.SecurityPriceServiceImpl;
import com.fftw.ivydb.service.SecurityTimeSeriesService;
import com.fftw.ivydb.service.SecurityTimeSeriesServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

static final Log logger = LogFactory.getLog(SecurityPriceDaily.class);

SecurityPriceService securityPriceService = new SecurityPriceServiceImpl();
SecurityTimeSeriesService securityTimeSeriesService = new SecurityTimeSeriesServiceImpl();
securityTimeSeriesService.setUp();

Calendar startTime = Calendar.getInstance();
logger.info("---------Start time: " + startTime.getTime());
Calendar processDate = Calendar.getInstance()
int currentHour = processDate.get(Calendar.HOUR_OF_DAY)
if (currentHour < 22){
	//go to previous day
	processDate.add(Calendar.DATE, -1)
}
logger.info("---------Processing for date: " + processDate.getTime());
List datas = securityPriceService.findByDate(new Date(processDate.getTimeInMillis()));
for (SecurityPrice securityPrice : datas)
{
    securityTimeSeriesService.createTimeSeriesDatas(securityPrice);
}
securityTimeSeriesService.bulkInsert();
Calendar endTime = Calendar.getInstance();
logger.info("---------End time: " + endTime.getTime());
System.out.println("SecurityPriceDaily.groovy Finished at " + endTime.getTime());
