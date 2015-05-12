package malbec.pomsfa.fix;

import java.util.Properties;

import malbec.fer.mapping.DatabaseMapper;
import malbec.util.EmailSettings;

import com.fftw.bloomberg.aggregator.RediConversionStrategy;
import com.fftw.bloomberg.types.TradingPlatform;

/**
 * Redi specific implementation for converting FIX messages into CMF messages
 * 
 * 
 */
public class GoldmanFixClientApplication extends FeedAggregatorFixClientApplication
{
    public GoldmanFixClientApplication (String name, Properties config, EmailSettings emailSettings, DatabaseMapper dbm)
    {
        super(name, config, emailSettings, new RediConversionStrategy(dbm));
    }

    @Override
    protected TradingPlatform getPlatform ()
    {
        return TradingPlatform.REDI;
    }
}
