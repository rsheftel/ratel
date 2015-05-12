#!/usr/bin/env groovy

import java.sql.Date;
import com.fftw.ivydb.domain.StandardOptionPrice;
import com.fftw.ivydb.service.StandardOptionPriceService;
import com.fftw.ivydb.service.StandardOptionPriceServiceImpl;
import com.fftw.ivydb.service.StandardOptionTimeSeriesService;
import com.fftw.ivydb.service.StandardOptionTimeSeriesServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

static final Log logger = LogFactory.getLog(StdOptionPriceDaily.class);

StandardOptionPriceService standardOptionPriceService = new StandardOptionPriceServiceImpl();
StandardOptionTimeSeriesService standardOptionTimeSeriesService = new StandardOptionTimeSeriesServiceImpl();
standardOptionTimeSeriesService.setUp();

Calendar startTime = Calendar.getInstance();
logger.info("---------Start time: " + startTime.getTime());
Calendar processDate = Calendar.getInstance();
int currentHour = processDate.get(Calendar.HOUR_OF_DAY)
if (currentHour < 22){
	//go to previous day
	processDate.add(Calendar.DATE, -1);
}
logger.info("---------Processing for date: " + processDate.getTime());
List datas = standardOptionPriceService.findByDate(new Date(processDate.getTimeInMillis()));
for (StandardOptionPrice standardOptionPrice : datas)
{
    standardOptionTimeSeriesService.createTimeSeriesDatas(standardOptionPrice);
}
standardOptionTimeSeriesService.bulkInsert();
Calendar endTime = Calendar.getInstance();
logger.info("---------End time: " + endTime.getTime());

System.out.println("StdOptionPriceDaily.groovy Finished at " + endTime.getTime());