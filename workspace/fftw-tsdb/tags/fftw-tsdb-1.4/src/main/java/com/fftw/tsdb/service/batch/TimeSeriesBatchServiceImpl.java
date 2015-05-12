package com.fftw.tsdb.service.batch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fftw.tsdb.dao.JdbcTimeSeriesDao;
import com.fftw.tsdb.domain.DataSource;
import com.fftw.tsdb.domain.TimeSeries;
import com.fftw.tsdb.factory.AbstractDaoFactory;
import com.fftw.tsdb.sdo.TimeSeriesSdo;
import com.fftw.tsdb.service.TimeSeriesServiceImpl;
import com.fftw.util.Util;

public class TimeSeriesBatchServiceImpl extends TimeSeriesServiceImpl implements
    TimeSeriesBatchService
{
    // get the right file separator so it can run on both wins
    private static final String FILE_SEPARATOR = File.separator;

    private static final Log logger = LogFactory.getLog(TimeSeriesBatchServiceImpl.class);

    public static final String remoteDir = "\\\\nyux51" + FILE_SEPARATOR + "data" + FILE_SEPARATOR
        + "temp_TSDB" + FILE_SEPARATOR;

    public static final String linuxFileDir = FILE_SEPARATOR + "data" + FILE_SEPARATOR
        + "temp_TSDB" + FILE_SEPARATOR;

    protected JdbcTimeSeriesDao jdbcTimeSeriesDao;

    protected PrintStream printStream;

    protected File bcpFile;

    protected boolean isWindows;

    public TimeSeriesBatchServiceImpl ()
    {
        // TODO same file will be used for the duration of the existance of this
        // class
        super();
        AbstractDaoFactory jdbcDaoFactory = AbstractDaoFactory.instance(AbstractDaoFactory.JDBC);
        jdbcTimeSeriesDao = jdbcDaoFactory.getJdbcTimeSeriesDao();
        isWindows = Util.isWindowsPlatform();
        logger.info("isWindows flag is: " + isWindows);
        Calendar calendar = Calendar.getInstance();
        if (isWindows)
        {
            bcpFile = new File("bcp." + calendar.getTimeInMillis());
            logger.info("Bcp file: " + bcpFile);
        }
        else
        {
            bcpFile = new File(linuxFileDir + "bcp." + calendar.getTimeInMillis());
            logger.info("Bcp file: " + bcpFile);
        }
    }

    // TODO maybe use properties file instead
    // this needs to be set before running createOrUpdateTimeSeriesDatasBatch
    public void setBcpFileDir (String fileDir)
    {
        Calendar calendar = Calendar.getInstance();
        bcpFile = new File(fileDir + "bcp." + calendar.getTimeInMillis());
    }

    public void setBcpFile (File bcpFile)
    {
        this.bcpFile = bcpFile;
    }

    public void createOrUpdateTimeSeriesDatasBatch (List<TimeSeriesSdo> timeSeriesSdos,
        DataSource dataSource)
    {
        FileOutputStream out;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try
        {
            out = new FileOutputStream(bcpFile, true);
            PrintStream printStream = new PrintStream(out);

            for (TimeSeriesSdo timeSeriesSdo : timeSeriesSdos)
            {
                if (timeSeriesSdo.getObservationValue() != null)
                {
                    TimeSeries timeSeries = createOrGetTimeSeries(timeSeriesSdo.getAttributes(),
                        timeSeriesSdo.getTimeSeriesName());
                    String observationDate = simpleDateFormat.format(timeSeriesSdo
                        .getObservationDate().getTime());

                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Writing to bcpFile at " + bcpFile + "..............");
                    }
                    printStream.print(timeSeries.getId() + "\t" + dataSource.getId() + "\t"
                        + observationDate + "\t" + timeSeriesSdo.getObservationValue() + "\r\n");
                }
            }
            printStream.close();
        }
        catch (FileNotFoundException e)
        {
            logger.error("Fail to find or create bcp file: " + bcpFile.getAbsolutePath());
            logger.error(e.getMessage());
        }
    }

    public void bulkInsert () throws IOException
    {
        // check if bcp file exists first because it can run on a weekend and
        // there is no data
        if (bcpFile.exists())
        {
            if (isWindows)
            {
                logger.info("Moving bcp file to \\\\nyux51\\data.");
                // TODO test create file locally then move to nyux51, delete
                // remote file if exists? There shouldn't be need to delete
                // remote file though
                Calendar calendar = Calendar.getInstance();
                File bcpProcessed = new File(remoteDir + "bcp-processed."
                    + calendar.getTimeInMillis());
                boolean isFileMoved = bcpFile.renameTo(bcpProcessed);
                if (!isFileMoved)
                {
                    throw new IOException("Failed to move file " + bcpFile.getAbsolutePath()
                        + " to " + bcpProcessed.getAbsolutePath());
                }
                logger.debug("Start processing bcp file + " + bcpProcessed);
                jdbcTimeSeriesDao.bulkInsert(bcpProcessed.getAbsolutePath());
                logger.debug("Finished processing bcp file " + bcpProcessed);
            }
            else
            {
                logger.debug("Start processing bcp file + " + bcpFile);
                //TODO need to fix bulkinsert because sqlserver is on windows and doesn't know the file name
                String windowsDirString = remoteDir.replace(FILE_SEPARATOR, "\\");
                jdbcTimeSeriesDao.bulkInsert(windowsDirString + bcpFile.getName());
                Calendar calendar = Calendar.getInstance();
                File bcpProcessed = new File(linuxFileDir + "bcp-processed."
                    + calendar.getTimeInMillis());
                boolean isFileMoved = bcpFile.renameTo(bcpProcessed); 
                if (!isFileMoved)
                {
                    throw new IOException("Failed to move file " + bcpFile.getAbsolutePath()
                        + " to " + bcpProcessed.getAbsolutePath());
                }
                logger.debug("Finished processing bcp file " + bcpProcessed);
            }
            
        }
        else
        {
            logger.info("bcpFile is not created beacause there is no data to be processsed.");
        }
    }
}
