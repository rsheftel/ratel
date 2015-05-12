package malbec.pomsfa.fix;

import java.util.Properties;

import malbec.fer.mapping.DatabaseMapper;
import malbec.util.EmailSettings;

import com.fftw.bloomberg.aggregator.TradingScreenConversionStrategy;
import com.fftw.bloomberg.types.TradingPlatform;

/**
 * TradingScreen specific implementation for converting FIX messages into CMF
 * messages
 * 
 */
public class TradingScreenFixClientApplication extends FeedAggregatorFixClientApplication
{
    // final private Logger log = LoggerFactory.getLogger(getClass());

    public TradingScreenFixClientApplication (String name, Properties config,
        EmailSettings emailSettings, DatabaseMapper dbm)
    {
        super(name, config, emailSettings, new TradingScreenConversionStrategy(dbm));
    }

    @Override
    protected TradingPlatform getPlatform ()
    {
        return TradingPlatform.TradingScreen;
    }

}
