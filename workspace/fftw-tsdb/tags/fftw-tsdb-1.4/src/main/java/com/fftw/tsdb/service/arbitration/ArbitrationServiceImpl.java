package com.fftw.tsdb.service.arbitration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;

import com.fftw.tsdb.dao.JdbcTimeSeriesDao;
import com.fftw.tsdb.domain.TimeSeries;
import com.fftw.tsdb.domain.TimeSeriesData;
import com.fftw.tsdb.factory.AbstractDaoFactory;
import com.fftw.tsdb.service.TimeSeriesServiceImpl;
import com.fftw.util.Emailer;
import com.fftw.util.Util;

public class ArbitrationServiceImpl implements ArbitrationService
{
    private static final Log logger = LogFactory.getLog(ArbitrationServiceImpl.class);

    protected JdbcTimeSeriesDao jdbcTimeSeriesDao;

    protected TimeSeriesServiceImpl tsServiceImpl;

    public ArbitrationServiceImpl ()
    {
        AbstractDaoFactory jdbcDaoFactory = AbstractDaoFactory.instance(AbstractDaoFactory.JDBC);
        jdbcTimeSeriesDao = jdbcDaoFactory.getJdbcTimeSeriesDao();
        tsServiceImpl = new TimeSeriesServiceImpl();
    }

    public void cdsArbitration (Calendar calArbitration) throws IOException
    {
        Calendar observationTime1 = Util.addWeekDay(calArbitration, -1), observationTime2 = Util
            .addWeekDay(calArbitration, -2), observationTime3 = Util.addWeekDay(calArbitration, 1);
        SimpleDateFormat dfYYYYMMDD = new SimpleDateFormat("yyyy-MM-dd");
        StringBuffer sb = new StringBuffer("time series name,"
            + dfYYYYMMDD.format(calArbitration.getTime()) + ","
            + dfYYYYMMDD.format(observationTime1.getTime()) + ","
            + dfYYYYMMDD.format(observationTime2.getTime()) + "\n");
        List<Long> listTimeSeriesIDs = jdbcTimeSeriesDao
            .getCDSArbitrationTimeSeriesIDs(calArbitration);
        StringBuffer sbMissingData = new StringBuffer();
        String strInternal = "internal";
        for (Iterator<Long> i = listTimeSeriesIDs.iterator(); i.hasNext();)
        {
            Long timeSeriesID = i.next();
            TimeSeries timeSeries = tsServiceImpl.findByID(timeSeriesID);
            boolean bFoundInMarkit = false;
            try
            {
                List<TimeSeriesData> listTSD = tsServiceImpl.findDataByDateRange(timeSeries
                    .getName(), "markit", calArbitration.getTime(), observationTime3.getTime());
                bFoundInMarkit = listTSD != null && !listTSD.isEmpty();
                if (bFoundInMarkit)
                {
                    TimeSeriesData timeSeriesData = listTSD.get(0);
                    tsServiceImpl.createOrUpdateTimeSeriesData(timeSeries, strInternal,
                        timeSeriesData.getObservationValue(), calArbitration);
                    sb.append(timeSeries.getName() + "," + timeSeriesData.getObservationValue());
                }
            }
            catch (EmptyResultDataAccessException e)
            {
            }
            if (bFoundInMarkit)
            {
                boolean bFoundHistory = false;
                try
                {
                    List<TimeSeriesData> listTSD = tsServiceImpl.findDataByDateRange(timeSeries
                        .getName(), strInternal, observationTime1.getTime(), calArbitration
                        .getTime());
                    bFoundHistory = listTSD != null && !listTSD.isEmpty();
                    if (bFoundHistory)
                    {
                        TimeSeriesData timeSeriesData = listTSD.get(0);
                        sb.append("," + timeSeriesData.getObservationValue());
                    }
                }
                catch (EmptyResultDataAccessException e)
                {
                }
                if (!bFoundHistory)
                {
                    sb.append(",");
                }
                bFoundHistory = false;
                try
                {
                    List<TimeSeriesData> listTSD = tsServiceImpl.findDataByDateRange(timeSeries
                        .getName(), strInternal, observationTime2.getTime(), observationTime1
                        .getTime());
                    bFoundHistory = listTSD != null && !listTSD.isEmpty();
                    if (bFoundHistory)
                    {
                        TimeSeriesData timeSeriesData = listTSD.get(0);
                        sb.append("," + timeSeriesData.getObservationValue() + "\n");
                    }
                }
                catch (EmptyResultDataAccessException e)
                {
                }
                if (!bFoundHistory)
                {
                    sb.append(",\n");
                }                
            }
            else
            {
                sbMissingData.append(
                    sbMissingData.length() == 0 ? "\n*** Missing data from markit: ***\n" : "")
                    .append("\n").append(timeSeries.getName());
            }
        }
        StringBuffer sbDiffTickers = new StringBuffer();
        Map<String, List<String>> mapTickerDiff = jdbcTimeSeriesDao.getCDSArbitrationTickerDiff(calArbitration); 
        for (Iterator<String> iDiffType = mapTickerDiff.keySet().iterator(); iDiffType.hasNext();)
        {
            String strDiffType = iDiffType.next();
            List<String> listDiffTickers = mapTickerDiff.get(strDiffType);
            sbDiffTickers.append("\n\n*** Ticker").append(listDiffTickers.size() > 1 ? "s" : "")
                .append(" got " + strDiffType + ": ***\n");            
            for (Iterator<String> iTicker = listDiffTickers.iterator(); iTicker.hasNext();)
            {
                sbDiffTickers.append("\n").append(iTicker.next());
            }
        }
        StringBuffer sbMessage = new StringBuffer();
        sbMessage.append(sbDiffTickers).append("\n\n").append(sbMissingData).append("\n\n\n");
        String strLinuxFileDir = File.separator + "data" + File.separator + "ArbitrationReports"
            + File.separator + "Today" + File.separator;
        String strRemoteDir = "\\\\nyux51" + strLinuxFileDir;
        String strFullFileName = (Util.isWindowsPlatform() ? strRemoteDir : strLinuxFileDir)
            + "markit." + dfYYYYMMDD.format(calArbitration.getTime()) + ".csv";
        File fCDSArbitration = new File(strFullFileName);
        FileWriter fileWriter = new FileWriter(fCDSArbitration);
        fileWriter.write(sb.toString());
        fileWriter.close();
        Emailer emailer = new Emailer();
        emailer.sendEmailWithFileAttachments("CDS Arbitration for "
            + dfYYYYMMDD.format(calArbitration.getTime()), sbMessage.toString(), new String[]
            {
                strFullFileName
            }, "mail.cds.to");
        logger.info("CDS Arbitration email sent.");
    }
}