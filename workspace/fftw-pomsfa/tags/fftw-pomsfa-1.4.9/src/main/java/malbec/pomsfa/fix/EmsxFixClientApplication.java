package malbec.pomsfa.fix;

import java.util.Properties;

import quickfix.FieldNotFound;
import quickfix.Message;

import malbec.util.EmailSettings;

import com.fftw.bloomberg.aggregator.EmsxConversionStrategy;
import com.fftw.bloomberg.types.TradingPlatform;

/**
 * EMSX specific implementation for converting FIX messages into CMF messages
 * 
 * 
 */
public class EmsxFixClientApplication extends FeedAggregatorFixClientApplication
{
    
    public EmsxFixClientApplication (String name, Properties config, EmailSettings emailSettings)
    {
        super(name, config, emailSettings, new EmsxConversionStrategy());
    }

    @Override
    protected TradingPlatform getPlatform ()
    {
        return TradingPlatform.EMSX;
    }

    /**
     * We do not send to Bloomberg, as we are receiving from Bloomberg.  This allows the execution
     * reports to be stored, but not sent.
     * 
     */
    @Override
    protected boolean alreadySent (TradingPlatform platform, Message message) throws FieldNotFound
    {
        return true;
    }
}
