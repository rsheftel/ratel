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

import com.fftw.ivydb.domain.StandardOptionPrice;
import com.fftw.ivydb.domain.StandardOptionPricePK;
import com.fftw.tsdb.domain.Attribute;
import com.fftw.tsdb.domain.DataSource;
import com.fftw.tsdb.domain.GeneralAttributeValue;
import com.fftw.tsdb.sdo.TimeSeriesSdo;
import com.fftw.tsdb.service.batch.TimeSeriesBatchServiceImpl;
import com.fftw.util.Emailer;

public class StandardOptionTimeSeriesServiceImpl extends TimeSeriesBatchServiceImpl implements
    StandardOptionTimeSeriesService
{
    private static final Log logger = LogFactory.getLog(StandardOptionPriceServiceImpl.class);

    private static final String DATASOURCENAME = "ivydb";

    private Attribute securityID;

    private Attribute expiry;

    private Attribute optionType;

    private Attribute quoteType;

    private Attribute quoteConvention;

    private Attribute quoteSide;

    private Attribute strike;

    private Attribute instrument;

    private GeneralAttributeValue close;

    private GeneralAttributeValue call;

    private GeneralAttributeValue put;

    private GeneralAttributeValue volLn;

    private GeneralAttributeValue delta;

    private GeneralAttributeValue mid;

    private GeneralAttributeValue atm;

    private GeneralAttributeValue stdEquityOption;

    private DataSource dataSource;

    private Emailer emailer;

    public StandardOptionTimeSeriesServiceImpl ()
    {
        super();
        emailer = new Emailer();
    }

    public void setUp ()
    {
        this.securityID = this.attributeService.findByName("security_id");
        this.expiry = this.attributeService.findByName("expiry");
        this.optionType = this.attributeService.findByName("option_type");
        this.quoteType = this.attributeService.findByName("quote_type");
        this.quoteConvention = this.attributeService.findByName("quote_convention");
        this.quoteSide = this.attributeService.findByName("quote_side");
        this.strike = this.attributeService.findByName("strike");
        this.instrument = this.attributeService.findByName("instrument");
        this.close = this.attributeValueService.createOrGetGeneralAttributeValue("close");
        this.call = this.attributeValueService.createOrGetGeneralAttributeValue("call");
        this.put = this.attributeValueService.createOrGetGeneralAttributeValue("put");
        this.volLn = this.attributeValueService.createOrGetGeneralAttributeValue("vol_ln");
        this.delta = this.attributeValueService.createOrGetGeneralAttributeValue("delta");
        this.mid = this.attributeValueService.createOrGetGeneralAttributeValue("mid");
        this.atm = this.attributeValueService.createOrGetGeneralAttributeValue("atm");
        this.stdEquityOption = this.attributeValueService
            .createOrGetGeneralAttributeValue("std_equity_option");
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

    public void createTimeSeriesDatas (StandardOptionPrice standardOptionPrice)
    {
        logger.info("createTimeSeriesDatas called...");
        String timeSeriesName;
        TimeSeriesSdo timeSeriesSdo;
        List<TimeSeriesSdo> timeSeriesSdos = new ArrayList<TimeSeriesSdo>();
        Map<Attribute, Long> attributes;
        Map<Attribute, Long> baseAttributes;
        StandardOptionPricePK standardOptionPricePK = standardOptionPrice.getId();
        Calendar observationDate = adjustObservationDate(standardOptionPricePK.getDate());
        GeneralAttributeValue callPut;
        if (standardOptionPricePK.getCallPutFlag().equalsIgnoreCase("C"))
        {
            callPut = this.call;
        }
        else
        {
            callPut = this.put;
        }
        try
        {
            baseAttributes = createAttributesMap(standardOptionPricePK.getSecurityId(),
                standardOptionPricePK.getDays().intValue() + "d", callPut);
        }
        catch (EmptyResultDataAccessException ex)
        {
            //TODO configure log4j to write this to specific error log file
            String message = "This row with data: securityID("
                + standardOptionPricePK.getSecurityId() + "), date("
                + standardOptionPricePK.getDate() + "), days("
                + standardOptionPricePK.getDays().toString() + "), callPutFlag("
                + standardOptionPricePK.getCallPutFlag() + ") needs to be reprocessed!";
            logger.error(message);
            emailer.sendEmail("Expiry with value " + standardOptionPricePK.getDays().intValue()
                + "d is missing in general_attribute_value table", message, null);
            return;
        }
        // TimeSeries with Implied Volatility
        Double impliedVolatility = standardOptionPrice.getImpliedVolatility();
        if (impliedVolatility != null)
        {
            attributes = new HashMap<Attribute, Long>(baseAttributes);
            timeSeriesName = createTimeSeriesName(standardOptionPricePK, "vol_ln", callPut
                .getName());
            attributes = addAttributesToAttributesMap(attributes, this.quoteConvention, this.volLn);
            timeSeriesSdo = new TimeSeriesSdo(timeSeriesName, DATASOURCENAME, observationDate,
                impliedVolatility, attributes);
            timeSeriesSdos.add(timeSeriesSdo);
        }
        // TimeSeries with Delta
        Double delta = standardOptionPrice.getDelta();
        if (delta != null)
        {
            attributes = new HashMap<Attribute, Long>(baseAttributes);
            timeSeriesName = createTimeSeriesName(standardOptionPricePK, "delta", callPut.getName());
            attributes = addAttributesToAttributesMap(attributes, this.quoteConvention, this.delta);
            timeSeriesSdo = new TimeSeriesSdo(timeSeriesName, DATASOURCENAME, observationDate,
                delta, attributes);
            timeSeriesSdos.add(timeSeriesSdo);
        }

        createOrUpdateTimeSeriesDatasBatch(timeSeriesSdos, dataSource);

        logger.info("Exiting createTimeSeriesDatas.");
    }

    /** ********************private methods*********************************** */
    private String createTimeSeriesName (StandardOptionPricePK standardOptionPricePK,
        String quoteConventionString, String callPut)
    {
        String timeSeriesName;
        timeSeriesName = "ivydb_" + standardOptionPricePK.getSecurityId() + "_"
            + standardOptionPricePK.getDays().intValue() + "d_atm_" + callPut + "_"
            + quoteConventionString + "_mid";

        logger.info("TimeSeriesName generated is: " + timeSeriesName);

        return timeSeriesName;
    }

    private Map<Attribute, Long> addAttributesToAttributesMap (Map<Attribute, Long> attributes,
        Attribute attribute, GeneralAttributeValue generalAttributeValue)
    {
        attributes.put(attribute, generalAttributeValue.getId());
        return attributes;
    }

    private Map<Attribute, Long> createAttributesMap (Long securityID, String daysString,
        GeneralAttributeValue callPut) throws EmptyResultDataAccessException
    {
        Map<Attribute, Long> attributes = new HashMap<Attribute, Long>();

        attributes.put(this.quoteType, this.close.getId());
        attributes.put(this.quoteSide, this.mid.getId());
        attributes.put(this.strike, this.atm.getId());
        attributes.put(this.instrument, this.stdEquityOption.getId());
        attributes.put(this.securityID, securityID);
        attributes.put(this.optionType, callPut.getId());

        // If days/expiry attribute value is not setup in the
        // general_attribute_value table, it will throw
        // an EmptyResultDataAccessException, valid values are 30, 60d, 91d,
        // 182d, 365d, 547d, 730d, 912d, 1095d
        GeneralAttributeValue days = this.attributeValueService
            .getGeneralAttributeValue(daysString);
        attributes.put(this.expiry, days.getId());
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
