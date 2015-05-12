#!/usr/bin/env groovy

import java.sql.Date;
import com.fftw.tsdb.service.arbitration.ArbitrationService;
import com.fftw.tsdb.service.arbitration.ArbitrationServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

static final Log logger = LogFactory.getLog(MarkitArbitrationDaily.class);

//This batch job will run arbitration process on previous day's data
ArbitrationService arbitrationService = new ArbitrationServiceImpl();

Calendar startTime = Calendar.getInstance();
logger.info("---------Start time for arbitrating data: " + startTime.getTime());
Calendar processDate = Calendar.getInstance();
//go to previous day
processDate.add(Calendar.DATE, -1);
logger.info("---------Processing for date: " + processDate.getTime());
processDate.set(Calendar.HOUR_OF_DAY, 0);
processDate.set(Calendar.MINUTE, 0);
processDate.set(Calendar.SECOND, 0);
processDate.set(Calendar.MILLISECOND, 0);
arbitrationService.cdsArbitration(processDate);
Calendar endTime = Calendar.getInstance();
logger.info("---------End time for arbitrating data: " + endTime.getTime());

