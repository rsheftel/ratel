package com.fftw.tsdb.service.irs;

import java.util.Map;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fftw.tsdb.domain.Attribute;

import com.fftw.tsdb.domain.TimeSeries;
import com.fftw.tsdb.service.TimeSeriesServiceImpl;

public class IrsTimeSeriesServiceImpl extends TimeSeriesServiceImpl implements IrsTimeSeriesService
{
    private static final Log logger = LogFactory.getLog(IrsTimeSeriesServiceImpl.class);
    
    public TimeSeries createOrGetIrsTimeSeries (String ccy, String quoteSide,
        String quoteConvention, String tenor)
    {
        String timeSeriesName = "irs_" + ccy + "_" + quoteConvention + "_" + tenor + "_"
            + quoteSide;
        TimeSeries timeSeries = findByName(timeSeriesName);

        if (timeSeries != null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Found time series " + timeSeriesName);
            }
            return timeSeries;
        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Did not find time series, creating a new time series name "
                    + timeSeriesName);
            }
            Map<Attribute, Long> attributes = new HashMap<Attribute, Long>();
            setAttribute(attributes, "ccy", ccy);
            setAttribute(attributes, "quote_side", quoteSide);
            setAttribute(attributes, "quote_convention", quoteConvention);
            setAttribute(attributes, "tenor", tenor);
            setAttribute(attributes, "quote_type", "close");
            setAttribute(attributes, "instrument", "irs");
            timeSeries = this.createTimeSeries(timeSeriesName, attributes);

        }
        return timeSeries;
    }

    private void setAttribute (Map<Attribute, Long> attributes, String name, String value)
    {
        attributes.put(attributeService.findByName(name), attributeValueService
            .createOrGetGeneralAttributeValue(value).getId());
    }
}
