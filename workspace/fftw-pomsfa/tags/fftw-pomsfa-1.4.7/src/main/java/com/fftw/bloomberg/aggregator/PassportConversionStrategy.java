package com.fftw.bloomberg.aggregator;

import java.util.List;

import malbec.fer.mapping.DatabaseMapper;
import malbec.util.FuturesSymbolUtil;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.Symbol;

import com.fftw.bloomberg.cmfp.CmfTradeRecord;
import com.fftw.bloomberg.types.TradingPlatform;

public class PassportConversionStrategy extends AbstractConversionStrategy
{
    protected PassportConversionStrategy (DatabaseMapper dbm)
    {
        setPlatform(TradingPlatform.Passport);
        setDatabaseMapper(dbm);
    }

    @Override
    protected List<String> convertCommon (Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        List<String> errorMessages = super.convertCommon(message, tradeRecord);
        tradeRecord.setPrimeBroker("MSPB");
        tradeRecord.setBroker("MOGSNY");

        return errorMessages;
    }

    @Override
    protected void convertEquities (Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        super.convertEquities(message, tradeRecord);
        // Passport sends us the Bloomberg symbol. No need to do any
        // manipulation
        tradeRecord.setSecurityId(extractSymbol(message).getValue());
    }

    /**
     * Passport is sending us Bloomberg symbols.
     * 
     */
    @Override
    protected String populateFuturesSymbol (Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        Symbol bbSymbol = extractSymbol(message);
        tradeRecord.setSecurityId(bbSymbol.getValue());
        String bbRoot = FuturesSymbolUtil.extractSymbolRoot(bbSymbol.getValue());
        
        return bbRoot;
    }
    
    @Override
    protected void convertFutures (Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        super.convertFutures(message, tradeRecord);
    }

    @Override
    protected void convertOptions (Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        // TradeStation only supports Equity Options
        super.convertOptions(message, tradeRecord);
        // If we have to do a lookup on this product, we will need
        // to add a method to extract the root symbol.
        tradeRecord.setSecurityId(extractSymbol(message).getValue());
    }

}
