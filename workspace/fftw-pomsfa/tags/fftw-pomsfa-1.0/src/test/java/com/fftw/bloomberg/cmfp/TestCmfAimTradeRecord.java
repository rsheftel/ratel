package com.fftw.bloomberg.cmfp;

import junit.framework.TestCase;

import com.fftw.bloomberg.types.TradingPlatform;

public class TestCmfAimTradeRecord extends TestCase
{

    public void testWireProtocol() {
        
        CmfAimTradeRecord tradeRecord = new CmfAimTradeRecord("01");
        
        tradeRecord.setPriceQuote(1234.4321);
        tradeRecord.setPriceQuoteDisplay(1);
        tradeRecord.setQuantity(9876.6789);
        tradeRecord.setSecurityIdFlag(1);
        tradeRecord.setSide(1);
        tradeRecord.setStatus(1);
        tradeRecord.setTradeSeqNum(1);
        tradeRecord.setTradingPlatform(TradingPlatform.TradeStation);
        
        String wireMessage = tradeRecord.getProtocolMessage();
        
        CmfTradeRecord newTradeRecord = CmfAimTradeRecord.createFromString(wireMessage);
        
        String newWireMessage = newTradeRecord.getProtocolMessage();
        
        assertEquals(wireMessage, newWireMessage);
        
    }
}
