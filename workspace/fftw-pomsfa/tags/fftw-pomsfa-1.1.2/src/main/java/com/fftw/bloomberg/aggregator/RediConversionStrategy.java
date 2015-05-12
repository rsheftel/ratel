package com.fftw.bloomberg.aggregator;

import java.math.BigDecimal;

import quickfix.FieldNotFound;
import quickfix.field.LastPx;
import quickfix.field.MaturityMonthYear;
import quickfix.field.PutOrCall;
import quickfix.field.SecurityExchange;
import quickfix.field.StrikePrice;
import quickfix.field.Symbol;
import quickfix.fix42.ExecutionReport;

import com.fftw.bloomberg.cmfp.CmfTradeRecord;
import com.fftw.bloomberg.types.TradingPlatform;
import com.fftw.bloomberg.util.Fix2CmfUtil;

public class RediConversionStrategy extends AbstractConversionStrategy
{
    protected RediConversionStrategy()
    {
        setPlatform(TradingPlatform.REDI);
    }
    
    @Override
    protected void convertCommon (ExecutionReport message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        super.convertCommon(message, tradeRecord);
        // All accounts are DVP, and all DVP are Morgan Stanley - TradeStation
        tradeRecord.setPrimeBroker("MSPB");
        tradeRecord.setBroker("GOLDNY");
    }

    @Override
    protected void convertEquities (ExecutionReport message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        super.convertEquities(message, tradeRecord);
        // Security includes the exchange
        Symbol symbol = extractSymbol(message);
        // Default to NYSE
        SecurityExchange exchange = extractExchange(message, new SecurityExchange("N"));
        
        tradeRecord.setSecurityId(Fix2CmfUtil.equitySymbol(symbol, exchange));
    }

    @Override
    protected void convertFutures (ExecutionReport message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        super.convertFutures(message, tradeRecord);

        Symbol bbSymbol = extractSymbol(message);
        // We need to extract the root symbol
        MaturityMonthYear monthyear = message.get(new MaturityMonthYear());

        Symbol symbol = extractFuturesRootSymbol(bbSymbol, monthyear);
        
        tradeRecord.setProductCode(Fix2CmfUtil.mapFuturesProductCode(getPlatform(), symbol));

        tradeRecord.setSecurityId(bbSymbol.getValue());

        BigDecimal priceMultiplier = Fix2CmfUtil.mapFuturesPriceMultiplier(getPlatform(), symbol);
        if (priceMultiplier.equals(BigDecimal.ONE))
        {
            tradeRecord.setPriceQuote(message.get(new LastPx()).getValue());
        }
        else
        {
            // If we have a mapping (even if it is 1.000) we end up here
            BigDecimal price = BigDecimal.valueOf(message.get(new LastPx()).getValue());
            tradeRecord.setPriceQuote(priceMultiplier.multiply(price));
        }
    }

    @Override
    protected void convertOptions (ExecutionReport message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        // TradeStation only supports Equity Options
        super.convertOptions(message, tradeRecord);
        // Default to Chicago Board Options Exchange
        SecurityExchange exchange = extractExchange(message, new SecurityExchange("W"));
 
        String symbol = Fix2CmfUtil.optionsSymbol(extractSymbol(message), exchange, message
            .get(new MaturityMonthYear()), message.get(new PutOrCall()), message
            .get(new StrikePrice()));

        tradeRecord.setSecurityId(symbol);

    }

}
