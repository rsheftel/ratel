package com.fftw.tsdb.service.cds;

import com.fftw.tsdb.domain.TimeSeries;
import com.fftw.tsdb.domain.cds.MarkitCompositeHist;
import com.fftw.tsdb.domain.cds.MarkitCompositeHistPK;
import com.fftw.tsdb.service.TimeSeriesService;

public interface CdsTimeSeriesService extends TimeSeriesService
{
    /**
     * Sets up all the attribute and attribute value objects so that they can be
     * reused
     * 
     */
    void setUp ();

    /**
     * 
     * Retrieve the cds time series by name if it exists, otherwise, create a
     * new cds time series without data point and insert into database
     * 
     * @param quoteTypeString
     *            quote type
     * @param ccyString
     *            ccy
     * @param cdsTickerString
     *            ticker
     * @param cdsTickerShortName
     *            ticker short name
     * @param docClauseString
     *            docClause
     * @param tierString
     *            tier
     * @param tenorString
     *            tenor
     * @return
     */
    TimeSeries createOrGetCdsTimeSeries (String quoteTypeString, String ccyString,
        String cdsTickerString, String cdsTickerShortName, String docClauseString,
        String tierString, String tenorString);

    /**
     * Retrieve the cds time series by name if it exists, otherwise, create a
     * new cds time series without data point and insert into database but
     * without the tenor attribute
     * 
     * @param quoteTypeString
     *            quote type
     * @param ccyString
     *            ccy
     * @param cdsTickerString
     *            ticker
     * @param cdsTickerShortName
     *            ticker short name
     * @param docClauseString
     *            docClause
     * @param tierString
     *            tier
     * @return
     */
    TimeSeries createOrGetCdsTimeSeries (String quoteTypeString, String ccyString,
        String cdsTickerString, String cdsTickerShortName, String docClauseString, String tierString);

    /**
     * Retrieve the cds time series by name if it exists, otherwise, create a
     * new cds time series without data point and insert into database given a
     * MarkitCompositeHistPK object and other attributes
     * 
     * @param markitCompositeHistPK
     *            MarkitCompositeHistPK object that holds attributes ccy,
     *            ticker, docClause, and tier
     * @param quoteTypeString
     *            quote type
     * @param cdsTickerShortName
     *            ticker short name
     * @param tenorString
     *            tenor
     * @return
     */
    TimeSeries createOrGetCdsTimeSeries (MarkitCompositeHistPK markitCompositeHistPK,
        String quoteTypeString, String cdsTickerShortName, String tenorString);

    /**
     * Retrieve the cds time series by name if it exists, otherwise, create a
     * new cds time series without data point and insert into database but
     * without the tenor attribute given a MarkitCompositeHistPK object and
     * other attributes
     * 
     * @param markitCompositeHistPK
     *            MarkitCompositeHistPK object that holds attributes ccy,
     *            ticker, docClause, and tier
     * @param quoteTypeString
     *            quote type
     * @param cdsTickerShortName
     *            ticker short name
     * @return
     */
    TimeSeries createOrGetCdsTimeSeries (MarkitCompositeHistPK markitCompositeHistPK,
        String quoteTypeString, String cdsTickerShortName);

    /**
     * Retrieve the cds time series by name if it exists and insert data point,
     * otherwise, create a new cds time series with a data point and insert into
     * database given a MarkitCompositeHistPK object and other attributes
     * 
     * @param markitCompositeHistPK
     * @param quoteTypeString
     * @param cdsTickerShortName
     * @param tenorString
     * @param dataSourceName
     * @param observationValue
     * @return
     */
    TimeSeries createOrGetCdsTimeSeriesWithData (MarkitCompositeHistPK markitCompositeHistPK,
        String quoteTypeString, String cdsTickerShortName, String tenorString,
        String dataSourceName, Double observationValue);

    /**
     * Retrieve the cds time series by name if it exists and insert data point,
     * otherwise, create a new cds time series with a data point and insert into
     * database but without the tenor attribute given a MarkitCompositeHistPK
     * object and other attributes
     * 
     * @param markitCompositeHistPK
     * @param quoteTypeString
     * @param cdsTickerShortName
     * @param dataSourceName
     * @param observationValue
     * @return
     */
    TimeSeries createOrGetCdsTimeSeriesWithData (MarkitCompositeHistPK markitCompositeHistPK,
        String quoteTypeString, String cdsTickerShortName, String dataSourceName,
        Double observationValue);

    /**
     * Given a MarkitCompositeHist object, create or update 14 time series with
     * data points
     * 
     * @param markitCompositeHist
     *            MarkitCompositeHist object
     * @param dataSourceName
     *            data source name
     */
    void createTimeSeriesDatas (MarkitCompositeHist markitCompositeHist);
}
