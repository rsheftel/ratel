package com.fftw.bloomberg.aggregator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.FieldNotFound;
import quickfix.fix42.ExecutionReport;

import com.fftw.bloomberg.cmfp.CmfMessage;
import com.fftw.bloomberg.types.TradingPlatform;
import com.fftw.util.Emailer;

/**
 * Bloomberg's EMSX platform
 * 
 * There is probably not much to do here, since we don't need to send to
 * Bloomberg (as we just received from them).
 * 
 */
public class EmsxConversionStrategy extends AbstractConversionStrategy
{
    private final Logger log = LoggerFactory.getLogger("EmsxMessageLog");
    
    public EmsxConversionStrategy ()
    {
        setPlatform(TradingPlatform.EMSX);
    }

    /**
     * 
     */
    @Override
    public CmfMessage convertMessage (ExecutionReport message, Emailer mailer) throws FieldNotFound
    {
        // We are not sending these anywhere, just log it
        log.info("Received execution from Bloomberg: " + message);
        
        return null;
    }
  
}
