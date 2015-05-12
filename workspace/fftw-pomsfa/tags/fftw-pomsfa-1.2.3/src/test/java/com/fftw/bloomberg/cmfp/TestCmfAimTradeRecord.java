package com.fftw.bloomberg.cmfp;

import junit.framework.TestCase;

import com.fftw.bloomberg.types.TradingPlatform;

public class TestCmfAimTradeRecord extends TestCase
{
    private static final String CMF_RECORD = "00000221        198QF.CDSvEQ_500                                             QMF     200000000000000060000100000104100000020080313BEARNY                                                                                                                                                                                           TRADSTAT                                                                                              02                                                                                                                                                                                         00        CZN US                  0                                                                                                                            MSPB                                                                                                                              001600                                         00000000                                            ";

    public void testWireProtocol ()
    {

        CmfAimTradeRecord tradeRecord = new CmfAimTradeRecord("01");

        tradeRecord.setPriceQuote(1234.432112345);
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

//        System.out.println(wireMessage);
//        System.out.println(newWireMessage);
        assertEquals(wireMessage, newWireMessage);
        assertEquals(9876.67, newTradeRecord.getQuantity());
        assertEquals(1234.43211234, newTradeRecord.getPriceQuote());
    }

    public void testCreateFromString ()
    {
        CmfAimTradeRecord tradeRecord = (CmfAimTradeRecord)CmfAimTradeRecord.createFromString(CMF_RECORD);

        String wireMessage = tradeRecord.getProtocolMessage();

        assertEquals(wireMessage, CMF_RECORD);

        // Change the shortSell field
        tradeRecord.setShortSale(1);
        String changedWireMessage = tradeRecord.getProtocolMessage();
        System.out.println(changedWireMessage);
        System.out.println(CMF_RECORD);

        assertNotSame(changedWireMessage, CMF_RECORD);
    }

}
