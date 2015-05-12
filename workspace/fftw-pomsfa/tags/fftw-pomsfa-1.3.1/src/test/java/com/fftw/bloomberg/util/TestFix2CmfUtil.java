package com.fftw.bloomberg.util;

import junit.framework.TestCase;
import quickfix.field.MaturityMonthYear;
import quickfix.field.PutOrCall;
import quickfix.field.SecurityExchange;
import quickfix.field.StrikePrice;
import quickfix.field.Symbol;

import com.fftw.bloomberg.types.TradingPlatform;
import com.fftw.bloomberg.util.Fix2CmfUtil;

public class TestFix2CmfUtil extends TestCase
{
    
    public void testFutureSymbol ()
    {
    /*    //TradingPlatform platform, Symbol rootSymbol, MaturityMonthYear monthYear
        String futureSymbol = Fix2CmfUtil.futureSymbol(TradingPlatform.TradeStation,new Symbol("ZN"), new MaturityMonthYear(
            "200712"));

        assertEquals("TYZ7", futureSymbol);
        */
    }

    public void testOptionSymbol ()
    {/*
        String futureSymbol = Fix2CmfUtil.optionsSymbol(new Symbol("BBY"),
            new SecurityExchange("W"), new MaturityMonthYear("200712"), new PutOrCall(1),
            new StrikePrice(45.78));

        assertEquals("BBY US 12 C 45.78", futureSymbol);*/
    }
 
}
