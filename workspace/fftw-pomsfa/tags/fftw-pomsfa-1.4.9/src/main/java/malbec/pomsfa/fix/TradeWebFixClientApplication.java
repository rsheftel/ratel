package malbec.pomsfa.fix;

import java.util.Properties;

import malbec.util.EmailSettings;

import com.fftw.bloomberg.aggregator.TradeWebConversionStrategy;
import com.fftw.bloomberg.types.TradingPlatform;

/**
 * TradeWeb specific implementation for converting FIX messages into CMF messages
 * 
 * 
 */
public class TradeWebFixClientApplication extends FeedAggregatorFixClientApplication
{
    
    public TradeWebFixClientApplication (String name, Properties config, EmailSettings emailSettings)
    {
        super(name, config, emailSettings, new TradeWebConversionStrategy());
    }

    @Override
    protected TradingPlatform getPlatform ()
    {
        return TradingPlatform.TradeWeb;
    }
}
