package com.fftw.tsdb.service.batch;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fftw.tsdb.domain.DataSource;
import com.fftw.tsdb.sdo.TimeSeriesSdo;
import com.fftw.tsdb.service.TimeSeriesService;

public interface TimeSeriesBatchService extends TimeSeriesService
{
    /**
     * Create a bcp file with time series data in it so can it can be bulk
     * inserted
     * 
     * @param timeSeriesSdos
     * @param dataSource
     */
    void createOrUpdateTimeSeriesDatasBatch (List<TimeSeriesSdo> timeSeriesSdos,
        DataSource dataSource);

    /**
     * Bulk insert time series datas contained in a file
     * 
     * @throws IOException
     *             if it cannot move local bcp file to remote server for bulk
     *             insert processing
     */
    void bulkInsert () throws IOException;

    /**
     * Set the diretory of where the bcp file should be created, include slashes
     * at the end
     * 
     * @param fileDir
     */
    void setBcpFileDir (String fileDir);

    void setBcpFile (File bcpFile);
}
