package com.fftw.tsdb.service.cds;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;

import com.fftw.tsdb.domain.Attribute;
import com.fftw.tsdb.domain.DataSource;
import com.fftw.tsdb.domain.GeneralAttributeValue;
import com.fftw.tsdb.domain.Ticker;
import com.fftw.tsdb.domain.TimeSeries;
import com.fftw.tsdb.domain.cds.CdsTicker;
import com.fftw.tsdb.domain.cds.MarkitCompositeHist;
import com.fftw.tsdb.domain.cds.MarkitCompositeHistPK;
import com.fftw.tsdb.sdo.TimeSeriesSdo;
import com.fftw.tsdb.service.TimeSeriesServiceImpl;

public class CdsTimeSeriesServiceImpl extends TimeSeriesServiceImpl implements CdsTimeSeriesService
{
    private static final Log logger = LogFactory.getLog(CdsTimeSeriesServiceImpl.class);
    private static final String DATASOURCENAME = "markit";
    
    private MarkitCompositeHistService markitCompositeHistService;
    
    private Attribute ccy;
    private Attribute docClause;
    private Attribute tier;
    private Attribute ticker;
    private Attribute cdsTicker;
    private Attribute quoteType;
    private Attribute tenor;
    private GeneralAttributeValue avRating;
    private GeneralAttributeValue recovery;
    private GeneralAttributeValue compositeDepth;
    private GeneralAttributeValue spread;
    private GeneralAttributeValue sixMonth;
    private GeneralAttributeValue oneYear;
    private GeneralAttributeValue twoYear;
    private GeneralAttributeValue threeYear;
    private GeneralAttributeValue fourYear;
    private GeneralAttributeValue fiveYear;
    private GeneralAttributeValue sevenYear;
    private GeneralAttributeValue tenYear;
    private GeneralAttributeValue fifteenYear;
    private GeneralAttributeValue twentyYear;
    private GeneralAttributeValue thirtyYear;
    private DataSource dataSource;

    public CdsTimeSeriesServiceImpl ()
    {
        super();
        this.markitCompositeHistService = new MarkitCompositeHistServiceImpl();
    }

    //TODO eliminate this setup class, so that this init is done in constructor
    //need this now because dbunit doesn't work without it to insert data for testing
    public void setUp ()
    {
        this.ccy = this.attributeService.findByName("ccy");
        this.docClause = this.attributeService.findByName("doc_clause");
        this.tier = this.attributeService.findByName("tier");
        this.ticker = this.attributeService.findByName("ticker");
        this.cdsTicker = this.attributeService.findByName("cds_ticker");
        this.quoteType = this.attributeService.findByName("quote_type");
        this.tenor = this.attributeService.findByName("tenor");
        this.avRating = this.attributeValueService.createOrGetGeneralAttributeValue("av_rating");
        this.recovery = this.attributeValueService.createOrGetGeneralAttributeValue("recovery");
        this.compositeDepth = this.attributeValueService.createOrGetGeneralAttributeValue("composite_depth");
        this.spread = this.attributeValueService.createOrGetGeneralAttributeValue("spread");
        this.sixMonth = this.attributeValueService.createOrGetGeneralAttributeValue("6m");
        this.oneYear = this.attributeValueService.createOrGetGeneralAttributeValue("1y");
        this.twoYear = this.attributeValueService.createOrGetGeneralAttributeValue("2y");
        this.threeYear = this.attributeValueService.createOrGetGeneralAttributeValue("3y");
        this.fourYear = this.attributeValueService.createOrGetGeneralAttributeValue("4y");
        this.fiveYear = this.attributeValueService.createOrGetGeneralAttributeValue("5y");
        this.sevenYear = this.attributeValueService.createOrGetGeneralAttributeValue("7y");
        this.tenYear = this.attributeValueService.createOrGetGeneralAttributeValue("10y");
        this.fifteenYear = this.attributeValueService.createOrGetGeneralAttributeValue("15y");
        this.twentyYear = this.attributeValueService.createOrGetGeneralAttributeValue("20y");
        this.thirtyYear = this.attributeValueService.createOrGetGeneralAttributeValue("30y");
        //TODO defaulting data source to "markit", maybe have a setter to set to different datasource if needed
        try {
            this.dataSource = timeSeriesDao.findDataSourceByName(DATASOURCENAME);
        }
        catch (EmptyResultDataAccessException e)
        {
            logger.equals("createOrUpdateTimeSeriesDatas - datasource " + DATASOURCENAME
                + " does not exist in database");
            throw new EmptyResultDataAccessException("Datasource " + DATASOURCENAME
                + " does not exist in database.", 1);
        }
    }


    public TimeSeries createOrGetCdsTimeSeries (String quoteTypeString, String ccyString,
        String cdsTickerString, String cdsTickerShortName, String docClauseString,
        String tierString, String tenorString)
    {
        Map<Attribute, Long> attributes = createAttributesMap(quoteTypeString, ccyString,
            cdsTickerString, cdsTickerShortName, docClauseString, tierString, tenorString);
        String timeSeriesName = cdsTickerString + "_" + tierString + "_" + ccyString + "_"
            + docClauseString + "_" + quoteTypeString + "" + tenorString;
        return createOrGetTimeSeries(attributes, timeSeriesName);
    }

    public TimeSeries createOrGetCdsTimeSeries (String quoteTypeString, String ccyString,
        String cdsTickerString, String cdsTickerShortName, String docClauseString, String tierString)
    {
        Map<Attribute, Long> attributes = createAttributesMap(quoteTypeString, ccyString,
            cdsTickerString, cdsTickerShortName, docClauseString, tierString, null);
        String timeSeriesName = cdsTickerString + "_" + tierString + "_" + ccyString + "_"
            + docClauseString + "_" + quoteTypeString;
        return createOrGetTimeSeries(attributes, timeSeriesName);
    }

    public TimeSeries createOrGetCdsTimeSeries (MarkitCompositeHistPK markitCompositeHistPK,
        String quoteTypeString, String cdsTickerShortName, String tenorString)
    {
        return createOrGetCdsTimeSeries(quoteTypeString, markitCompositeHistPK.getCcy(),
            markitCompositeHistPK.getTicker(), cdsTickerShortName, markitCompositeHistPK
                .getDocClause(), markitCompositeHistPK.getTier(), tenorString);
    }

    public TimeSeries createOrGetCdsTimeSeries (MarkitCompositeHistPK markitCompositeHistPK,
        String quoteTypeString, String cdsTickerShortName)
    {
        return createOrGetCdsTimeSeries(quoteTypeString, markitCompositeHistPK.getCcy(),
            markitCompositeHistPK.getTicker(), cdsTickerShortName, markitCompositeHistPK
                .getDocClause(), markitCompositeHistPK.getTier());
    }

    public TimeSeries createOrGetCdsTimeSeriesWithData (
        MarkitCompositeHistPK markitCompositeHistPK, String quoteTypeString,
        String cdsTickerShortName, String tenorString, String dataSourceName,
        Double observationValue)
    {
        TimeSeries timeSeries = createOrGetCdsTimeSeries(markitCompositeHistPK, quoteTypeString,
            cdsTickerShortName, tenorString);
        createOrUpdateTimeSeriesData(timeSeries, dataSourceName, observationValue,
            markitCompositeHistPK.getDate());
        return timeSeries;
    }

    public TimeSeries createOrGetCdsTimeSeriesWithData (
        MarkitCompositeHistPK markitCompositeHistPK, String quoteTypeString,
        String cdsTickerShortName, String dataSourceName, Double observationValue)
    {
        TimeSeries timeSeries = createOrGetCdsTimeSeries(markitCompositeHistPK, quoteTypeString,
            cdsTickerShortName);
        createOrUpdateTimeSeriesData(timeSeries, dataSourceName, observationValue,
            markitCompositeHistPK.getDate());
        return timeSeries;
    }

    /**
     * Insert 14 different time series data points from a MarkitCompositeHist
     * object
     */
    public void createTimeSeriesDatas (MarkitCompositeHist markitCompositeHist)
    {
        logger.info("createTimeSeriesDatas called...");
        String timeSeriesName;
        TimeSeriesSdo timeSeriesSdo;
        List<TimeSeriesSdo> timeSeriesSdos = new ArrayList<TimeSeriesSdo>();
        Map<Attribute, Long> attributes;
        MarkitCompositeHistPK markitCompositeHistPK = markitCompositeHist.getId();
        Date observationDate = markitCompositeHistPK.getDate();
        Map<Attribute, Long> baseAttributes = createAttributesMap(markitCompositeHistPK.getCcy(),
            markitCompositeHistPK.getTicker(), markitCompositeHist.getTickerShortName(),
            markitCompositeHistPK.getDocClause(), markitCompositeHistPK.getTier());

        // The reason for the baseAttributes map is that we don't have to lookup
        // all the attribues again
        attributes = baseAttributes;
        // Time series with av_rating
        String avRating = markitCompositeHist.getAvRating();
        if (avRating != null)
        {
            timeSeriesName = createTimeSeriesName(markitCompositeHistPK, "av_rating", null);
            attributes = addAttributesToAttributesMap(attributes, this.quoteType, this.avRating);
            timeSeriesSdo = new TimeSeriesSdo(timeSeriesName, DATASOURCENAME, observationDate, this.markitCompositeHistService.getCreditRating(avRating)
                .getRatingValue(), attributes);
            timeSeriesSdos.add(timeSeriesSdo);
        }
        // Time series with recovery
        Double recovery = markitCompositeHist.getRecovery();
        if (recovery != null)
        {
            timeSeriesName = createTimeSeriesName(markitCompositeHistPK, "recovery", null);
            attributes = baseAttributes;
            attributes = addAttributesToAttributesMap(attributes, this.quoteType, this.recovery);
            timeSeriesSdo = new TimeSeriesSdo(timeSeriesName, DATASOURCENAME, observationDate, recovery, attributes);
            timeSeriesSdos.add(timeSeriesSdo);
        }
        // Time series with composite_depth and tenor 5y
        String compositeDepth5y = markitCompositeHist.getCompositeDepth5y();
        if (compositeDepth5y != null)
        {
            timeSeriesName = createTimeSeriesName(markitCompositeHistPK, "composite_depth", "5y");
            attributes = baseAttributes;
            attributes = addAttributesToAttributesMap(attributes, this.quoteType, this.compositeDepth);
            attributes = addAttributesToAttributesMap(attributes, this.tenor, this.fiveYear);
            timeSeriesSdo = new TimeSeriesSdo(timeSeriesName, DATASOURCENAME, observationDate, new Double(compositeDepth5y), attributes);
            timeSeriesSdos.add(timeSeriesSdo);
        }
        // Add spread quote type attribute to baseAttributes map
        baseAttributes = addAttributesToAttributesMap(attributes, this.quoteType, this.spread);
        // Time series with spread and tenor 6m
        Double spread6m = markitCompositeHist.getSpread6m();
        if (spread6m != null)
        {
            timeSeriesName = createTimeSeriesName(markitCompositeHistPK, "spread", "6m");
            attributes = baseAttributes;
            attributes = addAttributesToAttributesMap(attributes, this.tenor, this.sixMonth);
            timeSeriesSdo = new TimeSeriesSdo(timeSeriesName, DATASOURCENAME, observationDate, spread6m, attributes);
            timeSeriesSdos.add(timeSeriesSdo);
        }
        // Time series with spread and tenor 1y
        Double spread1y = markitCompositeHist.getSpread1y();
        if (spread1y != null)
        {
            timeSeriesName = createTimeSeriesName(markitCompositeHistPK, "spread", "1y");
            attributes = baseAttributes;
            attributes = addAttributesToAttributesMap(attributes, this.tenor, this.oneYear);
            timeSeriesSdo = new TimeSeriesSdo(timeSeriesName, DATASOURCENAME, observationDate, spread1y, attributes);
            timeSeriesSdos.add(timeSeriesSdo);
        }
        // Time series with spread and tenor 2y
        Double spread2y = markitCompositeHist.getSpread2y();
        if (spread2y != null)
        {
            timeSeriesName = createTimeSeriesName(markitCompositeHistPK, "spread", "2y");
            attributes = baseAttributes;
            attributes = addAttributesToAttributesMap(attributes, this.tenor, this.twoYear);
            timeSeriesSdo = new TimeSeriesSdo(timeSeriesName, DATASOURCENAME, observationDate, spread2y, attributes);
            timeSeriesSdos.add(timeSeriesSdo);
        }
        // Time series with spread and tenor 3y
        Double spread3y = markitCompositeHist.getSpread3y();
        if (spread3y != null)
        {
            timeSeriesName = createTimeSeriesName(markitCompositeHistPK, "spread", "3y");
            attributes = baseAttributes;
            attributes = addAttributesToAttributesMap(attributes, this.tenor, this.threeYear);
            timeSeriesSdo = new TimeSeriesSdo(timeSeriesName, DATASOURCENAME, observationDate, spread3y, attributes);
            timeSeriesSdos.add(timeSeriesSdo);
        }
        // Time series with spread and tenor 4y
        Double spread4y = markitCompositeHist.getSpread4y();
        if (spread4y != null)
        {
            timeSeriesName = createTimeSeriesName(markitCompositeHistPK, "spread", "4y");
            attributes = baseAttributes;
            attributes = addAttributesToAttributesMap(attributes, this.tenor, this.fourYear);
            timeSeriesSdo = new TimeSeriesSdo(timeSeriesName, DATASOURCENAME, observationDate, spread4y, attributes);
            timeSeriesSdos.add(timeSeriesSdo);
        }
        // Time series with spread and tenor 5y
        Double spread5y = markitCompositeHist.getSpread5y();
        if (spread5y != null)
        {
            timeSeriesName = createTimeSeriesName(markitCompositeHistPK, "spread", "5y");
            attributes = baseAttributes;
            attributes = addAttributesToAttributesMap(attributes, this.tenor, this.fiveYear);
            timeSeriesSdo = new TimeSeriesSdo(timeSeriesName, DATASOURCENAME, observationDate, spread5y, attributes);
            timeSeriesSdos.add(timeSeriesSdo);
        }
        // Time series with spread and tenor 7y
        Double spread7y = markitCompositeHist.getSpread7y();
        if (spread7y != null)
        {
            timeSeriesName = createTimeSeriesName(markitCompositeHistPK, "spread", "7y");
            attributes = baseAttributes;
            attributes = addAttributesToAttributesMap(attributes, this.tenor, this.sevenYear);
            timeSeriesSdo = new TimeSeriesSdo(timeSeriesName, DATASOURCENAME, observationDate, spread7y, attributes);
            timeSeriesSdos.add(timeSeriesSdo);
        }
        // Time series with spread and tenor 10y
        Double spread10y = markitCompositeHist.getSpread10y();
        if (spread10y != null)
        {
            timeSeriesName = createTimeSeriesName(markitCompositeHistPK, "spread", "10y");
            attributes = baseAttributes;
            attributes = addAttributesToAttributesMap(attributes, this.tenor, this.tenYear);
            timeSeriesSdo = new TimeSeriesSdo(timeSeriesName, DATASOURCENAME, observationDate, spread10y, attributes);
            timeSeriesSdos.add(timeSeriesSdo);
        }
        // Time series with spread and tenor 15y
        Double spread15y = markitCompositeHist.getSpread15y();
        if (spread15y != null)
        {
            timeSeriesName = createTimeSeriesName(markitCompositeHistPK, "spread", "15y");
            attributes = baseAttributes;
            attributes = addAttributesToAttributesMap(attributes, this.tenor, this.fifteenYear);
            timeSeriesSdo = new TimeSeriesSdo(timeSeriesName, DATASOURCENAME, observationDate, spread15y, attributes);
            timeSeriesSdos.add(timeSeriesSdo);
        }
        // Time series with spread and tenor 20y
        Double spread20y = markitCompositeHist.getSpread20y();
        if (spread20y != null)
        {
            timeSeriesName = createTimeSeriesName(markitCompositeHistPK, "spread", "20y");
            attributes = baseAttributes;
            attributes = addAttributesToAttributesMap(attributes, this.tenor, this.twentyYear);
            timeSeriesSdo = new TimeSeriesSdo(timeSeriesName, DATASOURCENAME, observationDate, spread20y, attributes);
            timeSeriesSdos.add(timeSeriesSdo);
        }
        // Time series with spread and tenor 30y
        Double spread30y = markitCompositeHist.getSpread30y();
        if (spread30y != null)
        {
            timeSeriesName = createTimeSeriesName(markitCompositeHistPK, "spread", "30y");
            attributes = baseAttributes;
            attributes = addAttributesToAttributesMap(attributes, this.tenor, this.thirtyYear);
            timeSeriesSdo = new TimeSeriesSdo(timeSeriesName, DATASOURCENAME, observationDate, spread30y, attributes);
            timeSeriesSdos.add(timeSeriesSdo);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("timeSeriesSdos size is: " + timeSeriesSdos.size());
        }
        createOrUpdateTimeSeriesDatasBatch(timeSeriesSdos, dataSource);
        
        logger.info("Exiting createTimeSeriesDatas.");
    }

    /** ********************private methods*********************************** */
    private String createTimeSeriesName (MarkitCompositeHistPK markitCompositeHistPK,
        String quoteTypeString, String tenorString)
    {
        String timeSeriesName;
        if (tenorString != null)
        {
            timeSeriesName = markitCompositeHistPK.getTicker() + "_"
                + markitCompositeHistPK.getTier() + "_" + markitCompositeHistPK.getCcy() + "_"
                + markitCompositeHistPK.getDocClause() + "_" + quoteTypeString + "_" + tenorString;
        }
        else
        {
            timeSeriesName = markitCompositeHistPK.getTicker() + "_"
                + markitCompositeHistPK.getTier() + "_" + markitCompositeHistPK.getCcy() + "_"
                + markitCompositeHistPK.getDocClause() + "_" + quoteTypeString;
        }
        return timeSeriesName;
    }

    private Map<Attribute, Long> createAttributesMap (String quoteTypeString, String ccyString,
        String cdsTickerString, String cdsTickerShortName, String docClauseString,
        String tierString, String tenorString)
    {
        Map<Attribute, Long> attributes = createAttributesMap(ccyString, cdsTickerString,
            cdsTickerShortName, docClauseString, tierString);

        attributes = addQuoteTypeToAttributesMap(attributes, quoteTypeString);
        if (tenorString != null)
        {
            attributes = addTenorToAttributesMap(attributes, tenorString);
        }
        return attributes;
    }

    private Map<Attribute, Long> addQuoteTypeToAttributesMap (Map<Attribute, Long> attributes,
        String quoteTypeString)
    {
        GeneralAttributeValue quoteType = this.attributeValueService
            .createOrGetGeneralAttributeValue(quoteTypeString);
        attributes.put(this.attributeService.findByName("quote_type"), quoteType.getId());

        return attributes;
    }

    private Map<Attribute, Long> addTenorToAttributesMap (Map<Attribute, Long> attributes,
        String tenorString)
    {
        GeneralAttributeValue tenor = this.attributeValueService
            .createOrGetGeneralAttributeValue(tenorString);
        attributes.put(this.attributeService.findByName("tenor"), tenor.getId());
        return attributes;
    }

    private Map<Attribute, Long> addAttributesToAttributesMap(Map<Attribute, Long> attributes, 
        Attribute attribute, GeneralAttributeValue generalAttributeValue){
        attributes.put(attribute, generalAttributeValue.getId());
        return attributes;
    }
    
    
    private Map<Attribute, Long> createAttributesMap (String ccyString, String cdsTickerString,
        String cdsTickerShortName, String docClauseString, String tierString)
    {
        Map<Attribute, Long> attributes = new HashMap<Attribute, Long>();

        GeneralAttributeValue ccy = this.attributeValueService
            .createOrGetGeneralAttributeValue(ccyString);
        attributes.put(this.ccy, ccy.getId());

        GeneralAttributeValue docClause = this.attributeValueService
            .createOrGetGeneralAttributeValue(docClauseString);
        attributes.put(this.docClause, docClause.getId());

        GeneralAttributeValue tier = this.attributeValueService
            .createOrGetGeneralAttributeValue(tierString);
        attributes.put(this.tier, tier.getId());

        String cdsTickerWholeString = cdsTickerString + "_" + tierString + "_" + ccyString + "_"
            + docClauseString;
        Ticker ticker = this.attributeValueService.createOrGetTicker(cdsTickerString,
            cdsTickerShortName);
        attributes.put(this.ticker, ticker.getId());

        CdsTicker cdsTicker = this.attributeValueService.createOrGetCdsTicker(cdsTickerWholeString,
            ccy, docClause, ticker, tier);
        attributes.put(this.cdsTicker, cdsTicker.getId());

        return attributes;
    }
}
