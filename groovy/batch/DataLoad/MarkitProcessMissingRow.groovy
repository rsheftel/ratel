#!/usr/bin/env groovy

import java.sql.Date;
import com.fftw.tsdb.domain.cds.MarkitCompositeHist;
import com.fftw.tsdb.domain.cds.MarkitCompositeHistPK;
import com.fftw.tsdb.service.cds.CdsTimeSeriesService;
import com.fftw.tsdb.service.cds.CdsTimeSeriesServiceImpl;
import com.fftw.tsdb.service.cds.MarkitCompositeHistService;
import com.fftw.tsdb.service.cds.MarkitCompositeHistServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

static final Log logger = LogFactory.getLog(MarkitProcessMissingRow.class);

MarkitCompositeHistService markitCompositeHistService = new MarkitCompositeHistServiceImpl();
CdsTimeSeriesService cdsTimeSeriesService = new CdsTimeSeriesServiceImpl();
cdsTimeSeriesService.setUp();

Calendar startTime = Calendar.getInstance();
logger.info("---------Start time: " + startTime.getTime());
Calendar processDate = Calendar.getInstance();
processDate.set(2007, Calendar.JULY, 18);
logger.info("---------Processing for date: " + processDate.getTime());
String ccy = "NZD";
String docClause = "MR";
String ticker = "A";
String tier = "SNRFOR";
MarkitCompositeHistPK id = new MarkitCompositeHistPK();
id.setDate(new Date(processDate.getTimeInMillis()));
id.setCcy(ccy);
id.setDocClause(docClause);
id.setTicker(ticker);
id.setTier(tier);
MarkitCompositeHist data = this.markitCompositeHistService.findByID(id);
cdsTimeSeriesService.createTimeSeriesDatas(data);
cdsTimeSeriesService.bulkInsert();
Calendar endTime = Calendar.getInstance();
logger.info("---------End time: " + endTime.getTime());
println("Finished at " + endTime.getTime());