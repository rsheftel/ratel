package com.fftw.ivydb.service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;

import com.fftw.ivydb.domain.SecurityPrice;
import com.fftw.ivydb.domain.SecurityPricePK;
import com.fftw.tsdb.domain.Attribute;
import com.fftw.tsdb.domain.DataSource;
import com.fftw.tsdb.domain.GeneralAttributeValue;
import com.fftw.tsdb.sdo.TimeSeriesSdo;
import com.fftw.tsdb.service.batch.TimeSeriesBatchServiceImpl;

public class SecurityTimeSeriesServiceImpl extends TimeSeriesBatchServiceImpl implements
    SecurityTimeSeriesService
{
    private static final Log logger = LogFactory.getLog(SecurityTimeSeriesServiceImpl.class);

    private static final String DATASOURCENAME = "ivydb";

    private Attribute securityID;

    private Attribute quoteType;

    private Attribute quoteConvention;

    private Attribute quoteSide;

    private Attribute instrument;

    private GeneralAttributeValue close;

    private GeneralAttributeValue price;

    private GeneralAttributeValue triDailyPct;

    private GeneralAttributeValue totalReturnFactor;

    private GeneralAttributeValue sharesOutstanding;

    private GeneralAttributeValue volume;

    private GeneralAttributeValue mid;

    private GeneralAttributeValue equity;

    private DataSource dataSource;

    public SecurityTimeSeriesServiceImpl ()
    {
        super();
    }

    public void setUp ()
    {
        this.securityID = this.attributeService.findByName("security_id");
        this.quoteType = this.attributeService.findByName("quote_type");
        this.quoteConvention = this.attributeService.findByName("quote_convention");
        this.quoteSide = this.attributeService.findByName("quote_side");
        this.instrument = this.attributeService.findByName("instrument");
        this.close = this.attributeValueService.createOrGetGeneralAttributeValue("close");
        this.price = this.attributeValueService.createOrGetGeneralAttributeValue("price");
        this.triDailyPct = this.attributeValueService
            .createOrGetGeneralAttributeValue("tri_daily_pct");
        this.totalReturnFactor = this.attributeValueService
            .createOrGetGeneralAttributeValue("total_return_factor");
        this.sharesOutstanding = this.attributeValueService
            .createOrGetGeneralAttributeValue("shares_outstanding");
        this.volume = this.attributeValueService.createOrGetGeneralAttributeValue("volume");
        this.mid = this.attributeValueService.createOrGetGeneralAttributeValue("mid");
        this.equity = this.attributeValueService.createOrGetGeneralAttributeValue("equity");
        try
        {
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

    public void createTimeSeriesDatas (SecurityPrice securityPrice)
    {
        logger.info("createTimeSeriesDatas called...");
        String timeSeriesName;
        TimeSeriesSdo timeSeriesSdo;
        List<TimeSeriesSdo> timeSeriesSdos = new ArrayList<TimeSeriesSdo>();
        Map<Attribute, Long> attributes;
        SecurityPricePK securityPricePK = securityPrice.getId();
        Calendar observationDate = adjustObservationDate(securityPricePK.getDate());
        Long securityId = securityPricePK.getSecurityId();
        // Time Series with close price
        Double closePrice = securityPrice.getClosePrice();
        if (closePrice != null)
        {
            attributes = createAttributesMap(securityId);
            timeSeriesName = "ivydb_" + securityId + "_close_price_mid";
            logger.info("TimeSeriesName generated is: " + timeSeriesName);
            attributes = addAttributesToAttributesMap(attributes, this.quoteType, this.close);
            attributes = addAttributesToAttributesMap(attributes, this.quoteConvention, this.price);
            attributes = addAttributesToAttributesMap(attributes, this.quoteSide, this.mid);
            timeSeriesSdo = new TimeSeriesSdo(timeSeriesName, DATASOURCENAME, observationDate,
                closePrice, attributes);
            timeSeriesSdos.add(timeSeriesSdo);
        }
        //Time Series with total return(tri_daily_pct)
        Double totalReturn = securityPrice.getTotalReturn();
        if (totalReturn != null) {
            attributes = createAttributesMap(securityId);
            timeSeriesName = "ivydb_" + securityId + "_tri_daily_pct_mid";
            logger.info("TimeSeriesName generated is: " + timeSeriesName);
            attributes = addAttributesToAttributesMap(attributes, this.quoteType, this.close);
            attributes = addAttributesToAttributesMap(attributes, this.quoteConvention, this.triDailyPct);
            attributes = addAttributesToAttributesMap(attributes, this.quoteSide, this.mid);
            timeSeriesSdo = new TimeSeriesSdo(timeSeriesName, DATASOURCENAME, observationDate,
                totalReturn, attributes);
            timeSeriesSdos.add(timeSeriesSdo);
        }
        //Time Series with cumulative total return factor(total_return_factor)
        Double cumulativeTotalReturnFactor = securityPrice.getCumulativeTotalReturnFactor();
        if (cumulativeTotalReturnFactor != null) {
            attributes = createAttributesMap(securityId);
            timeSeriesName = "ivydb_" + securityId + "_total_return_factor_mid";
            logger.info("TimeSeriesName generated is: " + timeSeriesName);
            attributes = addAttributesToAttributesMap(attributes, this.quoteType, this.close);
            attributes = addAttributesToAttributesMap(attributes, this.quoteConvention, this.totalReturnFactor);
            attributes = addAttributesToAttributesMap(attributes, this.quoteSide, this.mid);
            timeSeriesSdo = new TimeSeriesSdo(timeSeriesName, DATASOURCENAME, observationDate,
                cumulativeTotalReturnFactor, attributes);
            timeSeriesSdos.add(timeSeriesSdo);
        }
        //Time Series with shares outstanding
        Double sharesOutstanding = new Double(securityPrice.getSharesOutstanding().doubleValue());
        if (sharesOutstanding != null) {
            attributes = createAttributesMap(securityId);
            timeSeriesName = "ivydb_" + securityId + "_shares_outstanding";
            logger.info("TimeSeriesName generated is: " + timeSeriesName);
            attributes = addAttributesToAttributesMap(attributes, this.quoteType, this.sharesOutstanding);
            timeSeriesSdo = new TimeSeriesSdo(timeSeriesName, DATASOURCENAME, observationDate,
                sharesOutstanding, attributes);
            timeSeriesSdos.add(timeSeriesSdo);
        }
        //Time Series with volume
        Double volume = securityPrice.getVolume();
        if (volume != null) {
            attributes = createAttributesMap(securityId);
            timeSeriesName = "ivydb_" + securityId + "_volume";
            logger.info("TimeSeriesName generated is: " + timeSeriesName);
            attributes = addAttributesToAttributesMap(attributes, this.quoteType, this.volume);
            timeSeriesSdo = new TimeSeriesSdo(timeSeriesName, DATASOURCENAME, observationDate,
                volume, attributes);
            timeSeriesSdos.add(timeSeriesSdo);
        }
        createOrUpdateTimeSeriesDatasBatch(timeSeriesSdos, dataSource);

        logger.info("Exiting createTimeSeriesDatas.");
    }

    /** ********************private methods*********************************** */
    private Map<Attribute, Long> addAttributesToAttributesMap (Map<Attribute, Long> attributes,
        Attribute attribute, GeneralAttributeValue generalAttributeValue)
    {
        attributes.put(attribute, generalAttributeValue.getId());
        return attributes;
    }

    private Map<Attribute, Long> createAttributesMap (Long securityID)
    {
        Map<Attribute, Long> attributes = new HashMap<Attribute, Long>();
        attributes.put(this.securityID, securityID);
        attributes.put(this.instrument, this.equity.getId());

        return attributes;
    }
    
    private Calendar adjustObservationDate(Date date) {
        Calendar observationDate = Calendar.getInstance();
        observationDate.setTime(date);
        observationDate.set(Calendar.HOUR_OF_DAY, 16);
        observationDate.set(Calendar.MINUTE, 0);
        observationDate.set(Calendar.SECOND, 0);
        observationDate.set(Calendar.MILLISECOND, 0);
        return observationDate;
    }

}
