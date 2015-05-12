package com.fftw.bloomberg.aggregator;

import java.util.List;

import malbec.fer.mapping.DatabaseMapper;
import malbec.util.FuturesSymbolUtil;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.MaturityMonthYear;
import quickfix.field.PutOrCall;
import quickfix.field.SecurityExchange;
import quickfix.field.StrikePrice;
import quickfix.field.Symbol;

import com.fftw.bloomberg.cmfp.CmfTradeRecord;
import com.fftw.bloomberg.types.TradingPlatform;
import com.fftw.bloomberg.util.Fix2CmfUtil;

public class RediConversionStrategy extends AbstractConversionStrategy
{
    public RediConversionStrategy (DatabaseMapper dbm)
    {
        setPlatform(TradingPlatform.REDI);
        setDatabaseMapper(dbm);
    }

    @Override
    protected List<String> convertCommon (Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        List<String> errorMessages = super.convertCommon(message, tradeRecord);
        // All accounts are DVP, and all DVP are Morgan Stanley - TradeStation
        tradeRecord.setPrimeBroker("MSPB");
        tradeRecord.setBroker("GOLDNY");

        return errorMessages;
    }

    @Override
    protected void convertEquities (Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        super.convertEquities(message, tradeRecord);
        // Security includes the exchange
        Symbol symbol = extractSymbol(message);
        // Default to NYSE
        SecurityExchange exchange = extractExchange(message, new SecurityExchange("N"));

        tradeRecord.setSecurityId(Fix2CmfUtil.equitySymbol(symbol, exchange));
    }

    /**
     * Goldman Sachs sends us Bloomberg Symbols in tag 55.
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
    
    /**
     * Goldman Sachs sends us Bloomberg Symbols in tag 55.
     * 
     * We need to extract the symbol and parse out the root to determine the 
     * yellow key.
     */
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
        // Default to Chicago Board Options Exchange
        SecurityExchange exchange = extractExchange(message, new SecurityExchange("W"));

        MaturityMonthYear monthYear = new MaturityMonthYear(message.getString(MaturityMonthYear.FIELD));
        PutOrCall putOrCall = new PutOrCall(message.getInt(PutOrCall.FIELD));
        StrikePrice strikePrice = new StrikePrice(message.getDouble(StrikePrice.FIELD));
        
        String symbol = Fix2CmfUtil.optionsSymbol(extractSymbol(message), exchange, monthYear, 
            putOrCall, strikePrice);

        tradeRecord.setSecurityId(symbol);
    }

}
