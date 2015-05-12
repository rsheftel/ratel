package com.fftw.bloomberg.aggregator;

import quickfix.FieldNotFound;

import com.fftw.bloomberg.cmfp.CmfMessage;
import com.fftw.bloomberg.types.TradingPlatform;

public interface ConversionStrategy
{

    /**
     * The current platform that this strategy works with.
     * 
     * @return
     */
    TradingPlatform getPlatform ();

    /**
     * This has the basic/generic algorithm to convert a FIX 4.2 message into a
     * CMF message.
     * 
     * @param message
     * @return
     */
    CmfMessage convertMessage (quickfix.fix42.ExecutionReport message) throws FieldNotFound;

}