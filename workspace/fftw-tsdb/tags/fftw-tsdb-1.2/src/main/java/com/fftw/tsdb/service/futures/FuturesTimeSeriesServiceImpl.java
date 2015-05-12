package com.fftw.tsdb.service.futures;

import java.util.HashMap;
import java.util.Map;

import com.fftw.tsdb.domain.Attribute;
import com.fftw.tsdb.domain.GeneralAttributeValue;
import com.fftw.tsdb.domain.Ticker;
import com.fftw.tsdb.domain.TimeSeries;
import com.fftw.tsdb.service.TimeSeriesServiceImpl;

public class FuturesTimeSeriesServiceImpl extends TimeSeriesServiceImpl implements
    FuturesTimeSeriesService
{
    public TimeSeries createOrGetFuturesOptionsTimeSeries (String optionTypeString,
        String quoteTypeString, String tickerString, String strikeString)
    {
        Map<Attribute, Long> attributes = new HashMap<Attribute, Long>();
        String timeSeriesName = tickerString + " " + optionTypeString + " " + strikeString + " "
            + quoteTypeString;
        GeneralAttributeValue quoteType = this.attributeValueService
            .createOrGetGeneralAttributeValue(quoteTypeString);
        attributes.put(this.attributeService.findByName("quote_type"), quoteType.getId());
        GeneralAttributeValue optionType = this.attributeValueService
            .createOrGetGeneralAttributeValue(optionTypeString);
        attributes.put(this.attributeService.findByName("option_type"), optionType.getId());
        GeneralAttributeValue strike = this.attributeValueService
            .createOrGetGeneralAttributeValue(strikeString);
        attributes.put(this.attributeService.findByName("strike"), strike.getId());
        Ticker ticker = this.attributeValueService.createOrGetTicker(tickerString, tickerString);
        attributes.put(this.attributeService.findByName("ticker"), ticker.getId());
        TimeSeries timeSeries = findByName(timeSeriesName);
        if (timeSeries != null)
        {
            return timeSeries;
        }
        else
        {
            return createTimeSeries(timeSeriesName, attributes);
        }
    }
}
